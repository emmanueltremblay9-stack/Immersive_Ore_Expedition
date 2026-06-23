package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public record OreLoadPlan(
        ExpeditionAnchorRef anchor,
        ResourceRef resource,
        BlockPos loadCenter,
        SiteQuality quality,
        boolean requiresTunnelConnection,
        int distanceFromAnchor
) {
    public OreLoadPlan {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(loadCenter, "loadCenter");
        Objects.requireNonNull(quality, "quality");
        if (distanceFromAnchor < 0) {
            throw new IllegalArgumentException("distanceFromAnchor must not be negative");
        }
    }
}
