package com.oblixorprime.ioe.retrogen;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PersistentRetrogenStateTest {
    @Test
    void markerModelCarriesPersistentChunkMetadataWithoutWorldReferences() {
        ResourceLocation dimension = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
        PersistentRetrogenChunkMarker marker = new PersistentRetrogenChunkMarker(
                4,
                -7,
                Optional.of(dimension),
                PersistentRetrogenChunkMarker.MarkerStatus.QUEUED,
                RetrogenMode.ADMIN_RADIUS,
                3,
                11,
                0,
                PersistentRetrogenChunkMarker.FailureReason.NONE,
                Optional.empty(),
                0
        );

        assertEquals(new ChunkKey(4, -7), marker.key());
        assertEquals(Optional.of(dimension), marker.dimensionId());
        assertEquals(PersistentRetrogenChunkMarker.MarkerStatus.QUEUED, marker.status());
        assertEquals(RetrogenMode.ADMIN_RADIUS, marker.mode());
        assertTrue(marker.isQueuedLike());
    }

    @Test
    void queuedMarkersRequirePositiveVersionAndQueueSequence() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new PersistentRetrogenChunkMarker(
                0,
                0,
                Optional.empty(),
                PersistentRetrogenChunkMarker.MarkerStatus.QUEUED,
                RetrogenMode.ADMIN_RADIUS,
                0,
                0,
                0,
                PersistentRetrogenChunkMarker.FailureReason.NONE,
                Optional.empty(),
                0
        ));

        assertEquals("queued retrogen markers require a positive marker version and queue sequence", error.getMessage());
    }

    @Test
    void stateTracksQueuedProcessedSkippedAndFailedCounts() {
        ChunkKey queued = new ChunkKey(0, 0);
        ChunkKey processed = new ChunkKey(1, 0);
        ChunkKey skipped = new ChunkKey(2, 0);
        ChunkKey failed = new ChunkKey(3, 0);
        PersistentRetrogenState state = PersistentRetrogenState.empty(2, 4)
                .withMarker(PersistentRetrogenChunkMarker.queued(queued, RetrogenMode.ADMIN_RADIUS, 2, 1))
                .withMarker(PersistentRetrogenChunkMarker.queued(processed, RetrogenMode.ADMIN_RADIUS, 2, 2).processed(3))
                .withMarker(PersistentRetrogenChunkMarker.queued(skipped, RetrogenMode.ADMIN_RADIUS, 2, 4)
                        .skipped(PersistentRetrogenChunkMarker.FailureReason.INVALID_RADIUS, "outside command radius", 5))
                .withMarker(PersistentRetrogenChunkMarker.queued(failed, RetrogenMode.ADMIN_RADIUS, 2, 6)
                        .failed(PersistentRetrogenChunkMarker.FailureReason.STORE_ERROR, "save failed", 7));

        PersistentRetrogenState.StatusSnapshot snapshot = state.statusSnapshot();

        assertEquals(1, snapshot.queuedChunks());
        assertEquals(1, snapshot.processedChunks());
        assertEquals(1, snapshot.skippedChunks());
        assertEquals(1, snapshot.failedChunks());
        assertTrue(state.isQueued(queued));
        assertTrue(state.hasProcessed(processed, 2));
        assertFalse(state.hasProcessed(processed, 3));
    }

    @Test
    void replacingMarkerKeepsChunkIdentityUnique() {
        ChunkKey key = new ChunkKey(8, 9);
        PersistentRetrogenState state = PersistentRetrogenState.empty(1, 1)
                .withMarker(PersistentRetrogenChunkMarker.queued(key, RetrogenMode.ADMIN_RADIUS, 1, 1))
                .withMarker(PersistentRetrogenChunkMarker.queued(key, RetrogenMode.ADMIN_RADIUS, 1, 2).processed(3));

        assertEquals(1, state.markers().size());
        assertTrue(state.hasProcessed(key, 1));
        assertFalse(state.isQueued(key));
    }

    @Test
    void pauseAndResumeMetadataIsDeterministic() {
        PersistentRetrogenState paused = PersistentRetrogenState.empty(5, 2)
                .withPaused(true, RetrogenMode.ADMIN_RADIUS);
        PersistentRetrogenState resumed = paused.withPaused(false, RetrogenMode.CLUE_PLUS_POCKET);

        assertTrue(paused.paused());
        assertEquals(RetrogenMode.ADMIN_RADIUS, paused.mode());
        assertFalse(resumed.paused());
        assertEquals(RetrogenMode.CLUE_PLUS_POCKET, resumed.mode());
        assertEquals(5, resumed.statusSnapshot().markerVersion());
        assertEquals(2, resumed.statusSnapshot().maxChunksPerTick());
    }

    @Test
    void safetyRulesRejectInvalidRequestsDeterministically() {
        ChunkKey key = new ChunkKey(0, 0);
        PersistentRetrogenState processedState = PersistentRetrogenState.empty(1, 1)
                .withMarker(PersistentRetrogenChunkMarker.queued(key, RetrogenMode.ADMIN_RADIUS, 1, 1).processed(2));
        PersistentRetrogenState queuedState = PersistentRetrogenState.empty(1, 1)
                .withMarker(PersistentRetrogenChunkMarker.queued(key, RetrogenMode.ADMIN_RADIUS, 1, 1));

        assertEquals(RetrogenSafetyRules.Decision.SKIP_INVALID_INPUT,
                RetrogenSafetyRules.evaluateQueueRequest(null, key, RetrogenMode.ADMIN_RADIUS, 0, true, 1));
        assertEquals(RetrogenSafetyRules.Decision.SKIP_INVALID_RADIUS,
                RetrogenSafetyRules.evaluateQueueRequest(PersistentRetrogenState.empty(1, 1), key,
                        RetrogenMode.ADMIN_RADIUS, RetrogenController.MAX_ADMIN_RADIUS_BLOCKS + 1, true, 1));
        assertEquals(RetrogenSafetyRules.Decision.SKIP_DISABLED,
                RetrogenSafetyRules.evaluateQueueRequest(PersistentRetrogenState.empty(1, 1), key,
                        RetrogenMode.OFF, 0, true, 1));
        assertEquals(RetrogenSafetyRules.Decision.SKIP_UNLOADED_CHUNK,
                RetrogenSafetyRules.evaluateQueueRequest(PersistentRetrogenState.empty(1, 1), key,
                        RetrogenMode.ADMIN_RADIUS, 0, false, 1));
        assertEquals(RetrogenSafetyRules.Decision.SKIP_ALREADY_PROCESSED,
                RetrogenSafetyRules.evaluateQueueRequest(processedState, key, RetrogenMode.ADMIN_RADIUS, 0, true, 1));
        assertEquals(RetrogenSafetyRules.Decision.SKIP_ALREADY_QUEUED,
                RetrogenSafetyRules.evaluateQueueRequest(queuedState, key, RetrogenMode.ADMIN_RADIUS, 0, true, 1));
        assertEquals(RetrogenSafetyRules.Decision.QUEUE_ALLOWED,
                RetrogenSafetyRules.evaluateQueueRequest(PersistentRetrogenState.empty(1, 1), key,
                        RetrogenMode.ADMIN_RADIUS, 0, true, 1));
    }

    @Test
    void stateRejectsInvalidThrottleMetadata() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new PersistentRetrogenState(List.of(), true, RetrogenMode.OFF, 1, 0, 1));

        assertEquals("maxChunksPerTick must be positive", error.getMessage());
    }
}
