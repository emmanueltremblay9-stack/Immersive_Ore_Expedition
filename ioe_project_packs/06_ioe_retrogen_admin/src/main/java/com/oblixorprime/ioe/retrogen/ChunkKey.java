package com.oblixorprime.ioe.retrogen;

public record ChunkKey(int x, int z) {
    public static int radiusBlocksToChunks(int radiusBlocks) {
        if (radiusBlocks < 0) {
            throw new IllegalArgumentException("radiusBlocks must not be negative");
        }
        return Math.max(0, (radiusBlocks + 15) / 16);
    }

    public boolean withinRadius(int centerChunkX, int centerChunkZ, int radiusBlocks) {
        int radiusChunks = radiusBlocksToChunks(radiusBlocks);
        return Math.abs(x - centerChunkX) <= radiusChunks && Math.abs(z - centerChunkZ) <= radiusChunks;
    }
}
