package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record MeteoriticAe2GeodePlacementPlan(
        GeodeType geodeType,
        SourceSystem sourceSystem,
        ResourceRef primaryResource,
        Optional<ResourceRef> skyStoneCrustResource,
        Optional<ResourceRef> middleLayerResource,
        Optional<ResourceRef> crystalCoreResource,
        BlockPos origin,
        boolean placementAllowed,
        Decision decision,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        Optional<ResourceLocation> anchorType,
        Optional<GeodeLayerMetadata> layerMetadata
) {
    public MeteoriticAe2GeodePlacementPlan {
        skyStoneCrustResource = skyStoneCrustResource == null ? Optional.empty() : skyStoneCrustResource;
        middleLayerResource = middleLayerResource == null ? Optional.empty() : middleLayerResource;
        crystalCoreResource = crystalCoreResource == null ? Optional.empty() : crystalCoreResource;
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        anchorType = anchorType == null ? Optional.empty() : anchorType;
        layerMetadata = layerMetadata == null ? Optional.empty() : layerMetadata;
        if (placementAllowed && decision != Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Allowed meteoritic AE2 geode plans must use ALLOW_PLAN");
        }
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed meteoritic AE2 geode plans must not carry a skip reason");
        }
        if (!placementAllowed && decision == Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Skipped meteoritic AE2 geode plans must not use ALLOW_PLAN");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped meteoritic AE2 geode plans require a skip reason");
        }
        if (placementAllowed) {
            Objects.requireNonNull(geodeType, "geodeType");
            Objects.requireNonNull(sourceSystem, "sourceSystem");
            Objects.requireNonNull(primaryResource, "primaryResource");
            Objects.requireNonNull(origin, "origin");
            GeodeLayerMetadata metadata = layerMetadata.orElseThrow(
                    () -> new IllegalArgumentException("Allowed meteoritic AE2 geode plans require layer metadata")
            );
            if (!metadata.isValid()) {
                throw new IllegalArgumentException("Allowed meteoritic AE2 geode plans require valid layer metadata");
            }
        }
    }

    public static MeteoriticAe2GeodePlacementPlan allowed(
            GeodeType geodeType,
            SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            GeodeLayerMetadata layerMetadata
    ) {
        return new MeteoriticAe2GeodePlacementPlan(
                geodeType,
                sourceSystem,
                primaryResource,
                Optional.ofNullable(skyStoneCrustResource),
                Optional.ofNullable(middleLayerResource),
                Optional.ofNullable(crystalCoreResource),
                origin,
                true,
                Decision.ALLOW_PLAN,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.of(Objects.requireNonNull(layerMetadata, "layerMetadata"))
        );
    }

    public static MeteoriticAe2GeodePlacementPlan skipped(
            GeodeType geodeType,
            SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            Decision decision,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            GeodeLayerMetadata layerMetadata
    ) {
        return new MeteoriticAe2GeodePlacementPlan(
                geodeType,
                sourceSystem,
                primaryResource,
                Optional.ofNullable(skyStoneCrustResource),
                Optional.ofNullable(middleLayerResource),
                Optional.ofNullable(crystalCoreResource),
                origin,
                false,
                decision,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.ofNullable(layerMetadata)
        );
    }

    public record GeodeLayerMetadata(
            int buriedDepth,
            int crustRadius,
            int middleLayerRadius,
            int coreRadius,
            double rarityWeight,
            double densityWeight,
            boolean skyStoneCrustRequired,
            boolean middleLayerPlanned,
            boolean crystalCorePlanned,
            boolean requiresStructureAnchor
    ) {
        public boolean isValid() {
            return buriedDepth >= 0
                    && crustRadius > 0
                    && middleLayerRadius > 0
                    && coreRadius > 0
                    && crustRadius >= middleLayerRadius
                    && middleLayerRadius >= coreRadius
                    && Double.isFinite(rarityWeight)
                    && rarityWeight >= 0.0D
                    && Double.isFinite(densityWeight)
                    && densityWeight >= 0.0D;
        }
    }

    public enum GeodeType {
        BURIED_METEORITIC_AE2_GEODE,
        SKY_STONE_CRUSTED_CERTUS_GEODE
    }

    public enum SourceSystem {
        AE2_METEORITIC,
        AE2_SKY_STONE,
        AE2_CERTUS
    }

    public enum Decision {
        ALLOW_PLAN,
        SKIP_RUNTIME_DISABLED,
        SKIP_RESOURCE_NOT_LOADED,
        SKIP_RESOURCE_DENIED,
        SKIP_STRICT_EXCLUSION,
        SKIP_OPTIONAL_MOD_ABSENT,
        SKIP_INVALID_GEODE_TYPE,
        SKIP_FAKE_FLUIX_FORBIDDEN,
        SKIP_INVALID_LAYER_METADATA,
        SKIP_INVALID_INPUT
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NULL_RESOURCE,
        NULL_ORIGIN,
        INVALID_GEODE_TYPE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        OPTIONAL_MOD_ABSENT,
        FAKE_FLUIX_FORBIDDEN,
        INVALID_LAYER_METADATA,
        INVALID_INPUT
    }
}
