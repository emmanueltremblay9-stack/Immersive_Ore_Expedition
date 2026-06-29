package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkRetrogenMarkerTest {
    @Test
    void missingMarkerNeedsRetrogen() {
        ChunkRetrogenMarker marker = ChunkRetrogenMarker.missing();

        assertFalse(marker.currentFor(1));
        assertTrue(marker.needsRetrogen(1));
    }

    @Test
    void processedMarkerPreventsDuplicateRetrogenForCurrentVersion() {
        ChunkRetrogenMarker marker = ChunkRetrogenMarker.processed(2);

        assertTrue(marker.currentFor(1));
        assertTrue(marker.currentFor(2));
        assertFalse(marker.needsRetrogen(2));
        assertTrue(marker.needsRetrogen(3));
    }

    @Test
    void negativeVersionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ChunkRetrogenMarker(-1, false));
    }

    @Test
    void processedMarkersAndTargetVersionsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> ChunkRetrogenMarker.processed(0));
        assertThrows(IllegalArgumentException.class, () -> ChunkRetrogenMarker.processed(1).currentFor(0));
        assertThrows(IllegalArgumentException.class, () -> ChunkRetrogenMarker.missing().needsRetrogen(0));
    }
}
