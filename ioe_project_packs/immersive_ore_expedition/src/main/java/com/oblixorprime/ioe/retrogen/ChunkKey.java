package com.oblixorprime.ioe.retrogen;

public record ChunkKey(int x, int z) {
    public static int radiusBlocksToChunks(int radiusBlocks) {
        if (radiusBlocks < 0) {
            throw new IllegalArgumentException("radiusBlocks must not be negative");
        }
        return (int) ((radiusBlocks + 15L) / 16L);
    }

    public boolean withinRadius(int centerChunkX, int centerChunkZ, int radiusBlocks) {
        int radiusChunks = radiusBlocksToChunks(radiusBlocks);
        long dx = (long) x - centerChunkX;
        long dz = (long) z - centerChunkZ;
        return Math.abs(dx) <= radiusChunks && Math.abs(dz) <= radiusChunks;
    }
}
