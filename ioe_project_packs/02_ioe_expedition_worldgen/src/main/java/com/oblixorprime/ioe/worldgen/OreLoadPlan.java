package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
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
        if (!ExpeditionStructureRegistry.isEnabledStructureId(anchor.anchorType())) {
            throw new IllegalArgumentException("Ore-load plans require a known expedition structure anchor");
        }
        if (resource.type() != ResourceType.BLOCK && resource.type() != ResourceType.BLOCK_TAG) {
            throw new IllegalArgumentException("Ore-load resources must be block resources or block tags");
        }
        int actualDistance = anchor.pos().distManhattan(loadCenter);
        if (distanceFromAnchor != actualDistance) {
            throw new IllegalArgumentException("distanceFromAnchor must match anchor-to-load Manhattan distance");
        }
        if (!AnchorRule.fromConfig().isDistanceAllowed(distanceFromAnchor)) {
            throw new IllegalArgumentException("distanceFromAnchor must stay inside the configured anchor distance window");
        }
    }
}
