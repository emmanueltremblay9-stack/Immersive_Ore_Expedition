package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChunkKeyTest {
    @Test
    void radiusConversionUsesCeilingWithoutOverflow() {
        assertEquals(0, ChunkKey.radiusBlocksToChunks(0));
        assertEquals(1, ChunkKey.radiusBlocksToChunks(1));
        assertEquals(1, ChunkKey.radiusBlocksToChunks(16));
        assertEquals(2, ChunkKey.radiusBlocksToChunks(17));
        assertEquals(134217728, ChunkKey.radiusBlocksToChunks(Integer.MAX_VALUE));
    }

    @Test
    void withinRadiusUsesLongDistanceMath() {
        assertFalse(new ChunkKey(Integer.MIN_VALUE, 0).withinRadius(Integer.MAX_VALUE, 0, 16));
        assertFalse(new ChunkKey(0, Integer.MIN_VALUE).withinRadius(0, Integer.MAX_VALUE, 16));
    }
}
