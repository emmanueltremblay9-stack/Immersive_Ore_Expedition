package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrogenControllerTest {
    @Test
    void offModeDoesNotQueueWork() {
        RetrogenController controller = new RetrogenController(1, 1);

        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.OFF, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), false, ChunkRetrogenMarker.missing())
        ));

        assertFalse(result.started());
        assertEquals(0, result.acceptedChunks());
        assertEquals(0, controller.status().queuedChunks());
    }

    @Test
    void adminRadiusSkipsMarkedAndOutOfRadiusChunks() {
        RetrogenController controller = new RetrogenController(1, 4);

        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), true, ChunkRetrogenMarker.missing()),
                new RetrogenChunkSnapshot(new ChunkKey(1, 0), true, ChunkRetrogenMarker.processed(1)),
                new RetrogenChunkSnapshot(new ChunkKey(3, 0), true, ChunkRetrogenMarker.missing())
        ));

        assertTrue(result.started());
        assertEquals(1, result.acceptedChunks());
        assertEquals(1, result.skippedAlreadyMarked());
        assertEquals(1, result.skippedOutOfRadius());
        assertEquals(1, controller.status().queuedChunks());
    }

    @Test
    void unexploredModeSkipsExploredChunks() {
        RetrogenController controller = new RetrogenController(1, 4);

        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.UNEXPLORED_CHUNKS_ONLY, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), true, ChunkRetrogenMarker.missing()),
                new RetrogenChunkSnapshot(new ChunkKey(1, 0), false, ChunkRetrogenMarker.missing())
        ));

        assertTrue(result.started());
        assertEquals(1, result.acceptedChunks());
        assertEquals(1, result.skippedExplored());
    }

    @Test
    void tickBatchMarksChunksAndHonorsMaxChunksPerTick() {
        RetrogenController controller = new RetrogenController(2, 1);
        controller.startAdminRadiusRetrogen(0, 0, 32, RetrogenMode.CLUE_PLUS_POCKET, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), true, ChunkRetrogenMarker.missing()),
                new RetrogenChunkSnapshot(new ChunkKey(1, 0), true, ChunkRetrogenMarker.missing())
        ));

        List<RetrogenQueueEntry> firstBatch = controller.tickBatch();

        assertEquals(1, firstBatch.size());
        assertTrue(controller.markerFor(new ChunkKey(0, 0)).currentFor(2));
        assertEquals(1, controller.status().queuedChunks());
        assertFalse(controller.status().paused());
    }

    @Test
    void completedQueueReturnsToOffMode() {
        RetrogenController controller = new RetrogenController(1, 4);
        controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.CLUE_PLUS_POCKET, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), true, ChunkRetrogenMarker.missing())
        ));

        List<RetrogenQueueEntry> processed = controller.tickBatch();

        assertEquals(1, processed.size());
        assertEquals(0, controller.status().queuedChunks());
        assertTrue(controller.status().paused());
        assertEquals(RetrogenMode.OFF, controller.status().mode());
    }

    @Test
    void persistentStatusMirrorsProcessedQueueStateWithoutChangingStatusShape() {
        RetrogenController controller = new RetrogenController(1, 4);
        controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(new ChunkKey(0, 0), false, ChunkRetrogenMarker.missing())
        ));

        assertEquals(1, controller.status().queuedChunks());
        assertEquals(1, controller.persistentStatus().queuedChunks());

        controller.tickBatch();

        assertEquals(0, controller.status().queuedChunks());
        assertEquals(1, controller.persistentStatus().processedChunks());
        assertEquals(0, controller.persistentStatus().queuedChunks());
    }

    @Test
    void processedInternalMarkersAreNotRequeuedByFreshPlaceholderSnapshots() {
        RetrogenController controller = new RetrogenController(1, 4);
        ChunkKey key = new ChunkKey(0, 0);

        RetrogenStartResult first = controller.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));
        List<RetrogenQueueEntry> processed = controller.tickBatch();
        RetrogenStartResult second = controller.startAdminRadiusRetrogen(0, 0, 0, RetrogenMode.ADMIN_RADIUS, List.of(
                new RetrogenChunkSnapshot(key, false, ChunkRetrogenMarker.missing())
        ));

        assertTrue(first.started());
        assertEquals(1, processed.size());
        assertTrue(controller.markerFor(key).currentFor(1));
        assertFalse(second.started());
        assertEquals(0, second.acceptedChunks());
        assertEquals(1, second.skippedAlreadyMarked());
        assertEquals(0, controller.status().queuedChunks());
    }

    @Test
    void placeholderRadiusCandidatesStayInsideRequestedRadius() {
        List<RetrogenChunkSnapshot> candidates = RetrogenController.placeholderRadiusCandidates(10, -4, 16);

        assertEquals(9, candidates.size());
        assertTrue(candidates.stream().allMatch(candidate -> candidate.key().withinRadius(10, -4, 16)));
    }

    @Test
    void placeholderRadiusCandidatesRejectRadiiAboveCommandCap() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> RetrogenController.placeholderRadiusCandidates(0, 0, RetrogenController.MAX_ADMIN_RADIUS_BLOCKS + 1));

        assertEquals("radiusBlocks must not exceed 1024", error.getMessage());
    }

    @Test
    void placeholderRadiusCandidatesClampExtremeChunkBounds() {
        List<RetrogenChunkSnapshot> candidates = RetrogenController.placeholderRadiusCandidates(Integer.MAX_VALUE, 0, 16);

        assertEquals(6, candidates.size());
        assertTrue(candidates.stream().anyMatch(candidate -> candidate.key().equals(new ChunkKey(Integer.MAX_VALUE, 0))));
        assertTrue(candidates.stream().allMatch(candidate -> candidate.key().withinRadius(Integer.MAX_VALUE, 0, 16)));
    }

    @Test
    void startAdminRadiusRetrogenRejectsRadiiAboveCommandCap() {
        RetrogenController controller = new RetrogenController(1, 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> controller.startAdminRadiusRetrogen(
                0,
                0,
                RetrogenController.MAX_ADMIN_RADIUS_BLOCKS + 1,
                RetrogenMode.ADMIN_RADIUS,
                List.of(new RetrogenChunkSnapshot(new ChunkKey(0, 0), false, ChunkRetrogenMarker.missing()))
        ));

        assertEquals("radiusBlocks must not exceed 1024", error.getMessage());
    }

    @Test
    void startAdminRadiusRetrogenSkipsNullCandidateSnapshots() {
        RetrogenController controller = new RetrogenController(1, 4);
        List<RetrogenChunkSnapshot> candidates = new ArrayList<>();
        candidates.add(null);
        candidates.add(new RetrogenChunkSnapshot(new ChunkKey(0, 0), false, ChunkRetrogenMarker.missing()));

        RetrogenStartResult result = controller.startAdminRadiusRetrogen(0, 0, 16, RetrogenMode.ADMIN_RADIUS, candidates);

        assertTrue(result.started());
        assertEquals(1, result.acceptedChunks());
        assertEquals(1, result.skippedInvalidCandidates());
        assertEquals(1, controller.status().queuedChunks());
    }
}
