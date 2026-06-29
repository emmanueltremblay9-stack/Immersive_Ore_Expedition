package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Objects;

public final class GiantLavaLakeDetector {
    public boolean isValidAnchor(WorldGenLevel level, BlockPos sampleCenter) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(sampleCenter, "sampleCenter");
        IoeNetherGeodesMod.LOGGER.debug("Skipping direct lava-lake scan at {}; alpha planning accepts explicit sample reports only.", sampleCenter);
        return false;
    }

    public boolean isValidAnchor(LavaLakeAnchorSample sample) {
        Objects.requireNonNull(sample, "sample");
        return isValidAnchor(
                IoeNetherGeodesConfig.enabled(),
                IoeNetherGeodesConfig.requireGiantLavaLakeAbove(),
                sample
        );
    }

    static boolean isValidAnchor(boolean enabled, boolean requireGiantLavaLakeAbove, LavaLakeAnchorSample sample) {
        Objects.requireNonNull(sample, "sample");
        if (!enabled || !Level.NETHER.equals(sample.dimension())) {
            return false;
        }
        if (!requireGiantLavaLakeAbove) {
            return true;
        }
        return sample.sampleRadius() >= IoeNetherGeodesConfig.lavaSampleRadius()
                && sample.lavaCoverage() >= IoeNetherGeodesConfig.minimumLavaCoverage()
                && sample.minimumLavaDepth() >= IoeNetherGeodesConfig.minimumLavaDepth();
    }
}
