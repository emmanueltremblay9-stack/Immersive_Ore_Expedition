package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void placeholderRadiusCandidatesStayInsideRequestedRadius() {
        List<RetrogenChunkSnapshot> candidates = RetrogenController.placeholderRadiusCandidates(10, -4, 16);

        assertEquals(9, candidates.size());
        assertTrue(candidates.stream().allMatch(candidate -> candidate.key().withinRadius(10, -4, 16)));
    }
}
