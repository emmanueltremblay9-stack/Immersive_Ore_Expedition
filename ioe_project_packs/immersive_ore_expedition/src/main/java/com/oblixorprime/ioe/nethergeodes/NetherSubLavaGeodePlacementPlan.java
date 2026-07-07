package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public record NetherSubLavaGeodePlacementPlan(
        GeodeType geodeType,
        SourceSystem sourceSystem,
        ResourceRef primaryQuartzResource,
        Optional<ResourceRef> ancientDebrisHeartResource,
        BlockPos origin,
        boolean placementAllowed,
        Decision decision,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        Optional<ResourceLocation> anchorType,
        Optional<ResourceKey<Level>> dimension,
        Optional<LavaLakeAnchorMetadata> lavaLakeAnchorMetadata,
        Optional<DepthMetadata> depthMetadata,
        Optional<LayerMetadata> layerMetadata
) {
    private static final ResourceRef NETHER_QUARTZ_ORE = ResourceRef.block("minecraft", "nether_quartz_ore");
    private static final ResourceRef ANCIENT_DEBRIS = ResourceRef.block("minecraft", "ancient_debris");

    public NetherSubLavaGeodePlacementPlan {
        ancientDebrisHeartResource = ancientDebrisHeartResource == null
                ? Optional.empty()
                : ancientDebrisHeartResource;
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        anchorType = anchorType == null ? Optional.empty() : anchorType;
        dimension = dimension == null ? Optional.empty() : dimension;
        lavaLakeAnchorMetadata = lavaLakeAnchorMetadata == null ? Optional.empty() : lavaLakeAnchorMetadata;
        depthMetadata = depthMetadata == null ? Optional.empty() : depthMetadata;
        layerMetadata = layerMetadata == null ? Optional.empty() : layerMetadata;

        if (placementAllowed && decision != Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Allowed Nether sub-lava geode plans must use ALLOW_PLAN");
        }
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed Nether sub-lava geode plans must not carry a skip reason");
        }
        if (!placementAllowed && decision == Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Skipped Nether sub-lava geode plans must not use ALLOW_PLAN");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped Nether sub-lava geode plans require a skip reason");
        }
        if (placementAllowed) {
            Objects.requireNonNull(geodeType, "geodeType");
            Objects.requireNonNull(sourceSystem, "sourceSystem");
            Objects.requireNonNull(primaryQuartzResource, "primaryQuartzResource");
            Objects.requireNonNull(origin, "origin");
            if (!NETHER_QUARTZ_ORE.equals(primaryQuartzResource)) {
                throw new IllegalArgumentException("Allowed Nether sub-lava geode plans require minecraft:nether_quartz_ore");
            }
            if (ancientDebrisHeartResource.filter(ANCIENT_DEBRIS::equals).isEmpty()
                    && ancientDebrisHeartResource.isPresent()) {
                throw new IllegalArgumentException("Ancient Debris hearts require minecraft:ancient_debris");
            }
            ResourceKey<Level> plannedDimension = dimension.orElseThrow(
                    () -> new IllegalArgumentException("Allowed Nether sub-lava geode plans require a dimension")
            );
            if (!Level.NETHER.equals(plannedDimension)) {
                throw new IllegalArgumentException("Nether sub-lava geode plans must stay in the Nether");
            }
            LavaLakeAnchorMetadata lavaMetadata = lavaLakeAnchorMetadata.orElseThrow(
                    () -> new IllegalArgumentException("Allowed Nether sub-lava geode plans require lava lake metadata")
            );
            DepthMetadata plannedDepthMetadata = depthMetadata.orElseThrow(
                    () -> new IllegalArgumentException("Allowed Nether sub-lava geode plans require depth metadata")
            );
            LayerMetadata plannedLayerMetadata = layerMetadata.orElseThrow(
                    () -> new IllegalArgumentException("Allowed Nether sub-lava geode plans require layer metadata")
            );
            if (!lavaMetadata.isValid()) {
                throw new IllegalArgumentException("Allowed Nether sub-lava geode plans require valid lava lake metadata");
            }
            if (!plannedDepthMetadata.isValid()) {
                throw new IllegalArgumentException("Allowed Nether sub-lava geode plans require valid depth metadata");
            }
            if (!plannedLayerMetadata.isValid()) {
                throw new IllegalArgumentException("Allowed Nether sub-lava geode plans require valid layer metadata");
            }
            if (geodeType == GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART) {
                if (ancientDebrisHeartResource.isEmpty() || !plannedLayerMetadata.ancientDebrisHeartEnabled()) {
                    throw new IllegalArgumentException("Ancient Debris heart plans require enabled heart metadata");
                }
            } else if (ancientDebrisHeartResource.isPresent() || plannedLayerMetadata.ancientDebrisHeartEnabled()) {
                throw new IllegalArgumentException("Base Nether quartz geode plans must not carry Ancient Debris heart metadata");
            }
        }
    }

    public static NetherSubLavaGeodePlacementPlan allowed(
            GeodeType geodeType,
            SourceSystem sourceSystem,
            ResourceRef primaryQuartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            ResourceKey<Level> dimension,
            LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            DepthMetadata depthMetadata,
            LayerMetadata layerMetadata
    ) {
        return new NetherSubLavaGeodePlacementPlan(
                geodeType,
                sourceSystem,
                primaryQuartzResource,
                Optional.ofNullable(ancientDebrisHeartResource),
                origin,
                true,
                Decision.ALLOW_PLAN,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.ofNullable(dimension),
                Optional.of(Objects.requireNonNull(lavaLakeAnchorMetadata, "lavaLakeAnchorMetadata")),
                Optional.of(Objects.requireNonNull(depthMetadata, "depthMetadata")),
                Optional.of(Objects.requireNonNull(layerMetadata, "layerMetadata"))
        );
    }

    public static NetherSubLavaGeodePlacementPlan skipped(
            GeodeType geodeType,
            SourceSystem sourceSystem,
            ResourceRef primaryQuartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            Decision decision,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            ResourceKey<Level> dimension,
            LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            DepthMetadata depthMetadata,
            LayerMetadata layerMetadata
    ) {
        return new NetherSubLavaGeodePlacementPlan(
                geodeType,
                sourceSystem,
                primaryQuartzResource,
                Optional.ofNullable(ancientDebrisHeartResource),
                origin,
                false,
                decision,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.ofNullable(dimension),
                Optional.ofNullable(lavaLakeAnchorMetadata),
                Optional.ofNullable(depthMetadata),
                Optional.ofNullable(layerMetadata)
        );
    }

    public record LavaLakeAnchorMetadata(
            int sampleRadius,
            double lavaCoverage,
            int minimumLavaDepth,
            boolean giantLavaLakeRequired
    ) {
        public boolean isValid() {
            return sampleRadius > 0
                    && Double.isFinite(lavaCoverage)
                    && lavaCoverage >= 0.0D
                    && lavaCoverage <= 1.0D
                    && minimumLavaDepth >= 0;
        }
    }

    public record DepthMetadata(
            int blocksBelowLava,
            int minBlocksBelowLava,
            int maxBlocksBelowLava
    ) {
        public boolean isValid() {
            return minBlocksBelowLava >= 0
                    && maxBlocksBelowLava >= 0
                    && minBlocksBelowLava <= maxBlocksBelowLava
                    && blocksBelowLava >= minBlocksBelowLava
                    && blocksBelowLava <= maxBlocksBelowLava;
        }
    }

    public record LayerMetadata(
            int crustRadius,
            int shellRadius,
            int coreRadius,
            boolean ancientDebrisHeartEnabled,
            double ancientDebrisHeartChance,
            boolean quartzShellPlanned,
            boolean quartzCorePlanned,
            boolean requiresSafeCrust
    ) {
        public boolean isValid() {
            return crustRadius > 0
                    && shellRadius > 0
                    && coreRadius > 0
                    && crustRadius >= shellRadius
                    && shellRadius >= coreRadius
                    && Double.isFinite(ancientDebrisHeartChance)
                    && ancientDebrisHeartChance >= 0.0D
                    && ancientDebrisHeartChance <= 0.05D;
        }
    }

    public enum GeodeType {
        SUB_LAVA_QUARTZ_GEODE,
        SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART
    }

    public enum SourceSystem {
        VANILLA_NETHER_QUARTZ,
        VANILLA_ANCIENT_DEBRIS,
        IOE_NETHER_GEODE
    }

    public enum Decision {
        ALLOW_PLAN,
        SKIP_RUNTIME_DISABLED,
        SKIP_NOT_NETHER,
        SKIP_INVALID_LAVA_ANCHOR,
        SKIP_INVALID_DEPTH,
        SKIP_RESOURCE_NOT_LOADED,
        SKIP_RESOURCE_DENIED,
        SKIP_STRICT_EXCLUSION,
        SKIP_ANCIENT_DEBRIS_HEART_DISABLED,
        SKIP_INVALID_GEODE_TYPE,
        SKIP_INVALID_LAYER_METADATA,
        SKIP_INVALID_INPUT
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NOT_NETHER,
        NULL_RESOURCE,
        NULL_ORIGIN,
        INVALID_LAVA_ANCHOR,
        INVALID_DEPTH,
        INVALID_GEODE_TYPE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        ANCIENT_DEBRIS_HEART_DISABLED,
        INVALID_LAYER_METADATA,
        INVALID_INPUT
    }
}
