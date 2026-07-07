package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record OreLoadChamberPlacementPlan(
        OreLoadPlan sourcePlan,
        BlockPos chamberCenter,
        ResourceRef resource,
        SiteQuality siteQuality,
        ResourceLocation anchorType,
        boolean placementAllowed,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        Optional<ChamberMetadata> chamberMetadata
) {
    public OreLoadChamberPlacementPlan {
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        chamberMetadata = chamberMetadata == null ? Optional.empty() : chamberMetadata;
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed chamber placement plans must not carry a skip reason");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped chamber placement plans require a skip reason");
        }
        if (placementAllowed) {
            Objects.requireNonNull(sourcePlan, "sourcePlan");
            Objects.requireNonNull(chamberCenter, "chamberCenter");
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(siteQuality, "siteQuality");
            Objects.requireNonNull(anchorType, "anchorType");
            if (chamberMetadata.isEmpty()) {
                throw new IllegalArgumentException("Allowed chamber placement plans require chamber metadata");
            }
        }
    }

    public static OreLoadChamberPlacementPlan allowed(
            OreLoadPlan sourcePlan,
            ResourceLocation anchorType,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ChamberMetadata chamberMetadata
    ) {
        Objects.requireNonNull(sourcePlan, "sourcePlan");
        return new OreLoadChamberPlacementPlan(
                sourcePlan,
                sourcePlan.loadCenter(),
                sourcePlan.resource(),
                sourcePlan.quality(),
                anchorType,
                true,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.of(Objects.requireNonNull(chamberMetadata, "chamberMetadata"))
        );
    }

    public static OreLoadChamberPlacementPlan skipped(
            OreLoadPlan sourcePlan,
            ResourceLocation anchorType,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ChamberMetadata chamberMetadata
    ) {
        return new OreLoadChamberPlacementPlan(
                sourcePlan,
                sourcePlan == null ? null : sourcePlan.loadCenter(),
                sourcePlan == null ? null : sourcePlan.resource(),
                sourcePlan == null ? null : sourcePlan.quality(),
                anchorType,
                false,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(chamberMetadata)
        );
    }

    public record ChamberMetadata(
            ChamberShape shape,
            int horizontalRadius,
            int verticalHalfSize,
            int approximateVolume
    ) {
        public ChamberMetadata {
            Objects.requireNonNull(shape, "shape");
            if (horizontalRadius <= 0) {
                throw new IllegalArgumentException("horizontalRadius must be positive");
            }
            if (verticalHalfSize <= 0) {
                throw new IllegalArgumentException("verticalHalfSize must be positive");
            }
            if (approximateVolume <= 0) {
                throw new IllegalArgumentException("approximateVolume must be positive");
            }
        }
    }

    public enum ChamberShape {
        ROUGH_CHAMBER,
        VEIN_CLUSTER,
        MOTHERLODE_CORE
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NULL_ORE_LOAD_PLAN,
        NULL_RESOURCE,
        NULL_CENTER,
        NULL_SITE_QUALITY,
        UNKNOWN_OR_INVALID_ANCHOR,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        INVALID_CHAMBER_BOUNDS,
        INVALID_INPUT
    }
}
