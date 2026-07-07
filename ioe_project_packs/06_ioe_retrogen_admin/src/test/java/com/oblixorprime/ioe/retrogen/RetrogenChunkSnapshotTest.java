package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RetrogenChunkSnapshotTest {
    @Test
    void rejectsNullMarkerBecauseMissingMarkerHasExplicitSentinel() {
        assertThrows(NullPointerException.class, () -> new RetrogenChunkSnapshot(
                new ChunkKey(0, 0),
                false,
                null
        ));
    }
}
