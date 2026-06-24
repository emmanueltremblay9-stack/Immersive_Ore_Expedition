package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record LavaLakeAnchorSample(
        ResourceKey<Level> dimension,
        BlockPos center,
        int sampleRadius,
        double lavaCoverage,
        int minimumLavaDepth
) {
    public LavaLakeAnchorSample {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(center, "center");
        if (sampleRadius < 1) {
            throw new IllegalArgumentException("sampleRadius must be positive");
        }
        if (lavaCoverage < 0.0D || lavaCoverage > 1.0D) {
            throw new IllegalArgumentException("lavaCoverage must be between 0 and 1");
        }
        if (minimumLavaDepth < 0) {
            throw new IllegalArgumentException("minimumLavaDepth must not be negative");
        }
    }
}
