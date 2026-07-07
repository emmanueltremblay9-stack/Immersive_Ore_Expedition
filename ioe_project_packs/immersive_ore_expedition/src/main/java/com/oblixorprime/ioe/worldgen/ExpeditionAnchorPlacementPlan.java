package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record ExpeditionAnchorPlacementPlan(
        ResourceLocation anchorType,
        BlockPos origin,
        SiteQuality siteQuality,
        boolean placementAllowed,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId
) {
    public ExpeditionAnchorPlacementPlan {
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed anchor placement plans must not carry a skip reason");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped anchor placement plans require a skip reason");
        }
        if (placementAllowed) {
            Objects.requireNonNull(anchorType, "anchorType");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(siteQuality, "siteQuality");
        }
    }

    public static ExpeditionAnchorPlacementPlan allowed(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        return new ExpeditionAnchorPlacementPlan(
                anchorType,
                origin,
                siteQuality,
                true,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId)
        );
    }

    public static ExpeditionAnchorPlacementPlan skipped(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        return new ExpeditionAnchorPlacementPlan(
                anchorType,
                origin,
                siteQuality,
                false,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId)
        );
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        UNKNOWN_ANCHOR_TYPE,
        NULL_ORIGIN,
        NULL_SITE_QUALITY,
        INVALID_NAMESPACE,
        INVALID_INPUT
    }
}
