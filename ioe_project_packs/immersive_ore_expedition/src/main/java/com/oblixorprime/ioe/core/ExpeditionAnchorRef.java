package com.oblixorprime.ioe.core;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record ExpeditionAnchorRef(ResourceKey<Level> dimension, BlockPos pos, String anchorType, SiteQuality quality) {
    public ExpeditionAnchorRef {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(anchorType, "anchorType");
        Objects.requireNonNull(quality, "quality");
        if (anchorType.isBlank()) {
            throw new IllegalArgumentException("anchorType must not be blank");
        }
    }
}
