package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GiantLavaLakeDetectorTest {
    private final GiantLavaLakeDetector detector = new GiantLavaLakeDetector();

    @Test
    void acceptsOnlyLargeDeepNetherLavaLakeSamples() {
        assertTrue(detector.isValidAnchor(new LavaLakeAnchorSample(Level.NETHER, BlockPos.ZERO, 64, 0.60D, 4)));
        assertFalse(detector.isValidAnchor(new LavaLakeAnchorSample(Level.NETHER, BlockPos.ZERO, 64, 0.59D, 4)));
        assertFalse(detector.isValidAnchor(new LavaLakeAnchorSample(Level.NETHER, BlockPos.ZERO, 64, 0.60D, 3)));
        assertFalse(detector.isValidAnchor(new LavaLakeAnchorSample(Level.OVERWORLD, BlockPos.ZERO, 64, 0.90D, 8)));
    }
}
