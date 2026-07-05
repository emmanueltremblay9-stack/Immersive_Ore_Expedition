package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

final class NetherSubLavaGeodePlacementRules {
    private static final ResourceRef NETHER_QUARTZ_ORE = ResourceRef.block("minecraft", "nether_quartz_ore");
    private static final ResourceRef ANCIENT_DEBRIS = ResourceRef.block("minecraft", "ancient_debris");

    private NetherSubLavaGeodePlacementRules() {
    }

    static NetherSubLavaGeodePlacementPlan validateDirect(
            NetherSubLavaGeodePlacementPlan.GeodeType geodeType,
            NetherSubLavaGeodePlacementPlan.SourceSystem sourceSystem,
            ExpeditionAnchorRef anchor,
            LavaLakeAnchorSample lavaLake,
            ResourceRef quartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            NetherSubLavaGeodePlacementPlan.DepthMetadata depthMetadata,
            NetherSubLavaGeodePlacementPlan.LayerMetadata layerMetadata,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null || scanner == null || policyService == null || sourceSystem == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (placementGates.shouldNoOpRuntimePlacement() || !IoeNetherGeodesConfig.enabled()) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_RUNTIME_DISABLED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED);
        }
        if (origin == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    NetherSubLavaGeodePlacementPlan.SkipReason.NULL_ORIGIN);
        }
        if (quartzResource == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    NetherSubLavaGeodePlacementPlan.SkipReason.NULL_RESOURCE);
        }
        if (anchor == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (lavaLake == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_LAVA_ANCHOR,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_LAVA_ANCHOR);
        }
        if (!Level.NETHER.equals(anchor.dimension()) || !Level.NETHER.equals(lavaLake.dimension())) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_NOT_NETHER,
                    NetherSubLavaGeodePlacementPlan.SkipReason.NOT_NETHER);
        }
        if (lavaLakeAnchorMetadata == null
                || !lavaLakeAnchorMetadata.isValid()
                || !new GiantLavaLakeDetector().isValidAnchor(lavaLake)) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_LAVA_ANCHOR,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_LAVA_ANCHOR);
        }
        if (depthMetadata == null
                || !depthMetadata.isValid()
                || depthMetadata.blocksBelowLava() < IoeNetherGeodesConfig.minBlocksBelowLava()
                || depthMetadata.blocksBelowLava() > IoeNetherGeodesConfig.maxBlocksBelowLava()) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_DEPTH,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_DEPTH);
        }
        if (!isValidGeodeType(geodeType)) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_GEODE_TYPE,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_GEODE_TYPE);
        }
        if (layerMetadata == null || !layerMetadata.isValid()) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_LAYER_METADATA,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_LAYER_METADATA);
        }
        if (policyService.isExcludedResource(quartzResource.id())
                || (ancientDebrisHeartResource != null
                && policyService.isExcludedResource(ancientDebrisHeartResource.id()))) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                    NetherSubLavaGeodePlacementPlan.SkipReason.STRICT_EXCLUSION);
        }
        boolean heartRequested = geodeType == NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART
                || layerMetadata.ancientDebrisHeartEnabled()
                || ancientDebrisHeartResource != null;
        if (heartRequested
                && (!IoeNetherGeodesConfig.ancientDebrisExtremeRare()
                || !layerMetadata.ancientDebrisHeartEnabled())) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_ANCIENT_DEBRIS_HEART_DISABLED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.ANCIENT_DEBRIS_HEART_DISABLED);
        }
        if (heartRequested && ancientDebrisHeartResource == null) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }

        NetherSubLavaGeodePlacementPlan rejectedQuartz = validateResource(quartzResource, geodeType, sourceSystem,
                quartzResource, ancientDebrisHeartResource, origin, lavaLakeAnchorMetadata, depthMetadata,
                layerMetadata, scanner, policyService);
        if (rejectedQuartz != null) {
            return rejectedQuartz;
        }
        if (heartRequested) {
            NetherSubLavaGeodePlacementPlan rejectedHeart = validateResource(ancientDebrisHeartResource, geodeType,
                    sourceSystem, quartzResource, ancientDebrisHeartResource, origin, lavaLakeAnchorMetadata,
                    depthMetadata, layerMetadata, scanner, policyService);
            if (rejectedHeart != null) {
                return rejectedHeart;
            }
        }
        if (!NETHER_QUARTZ_ORE.equals(quartzResource)
                || (heartRequested && !ANCIENT_DEBRIS.equals(ancientDebrisHeartResource))) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_GEODE_TYPE,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_GEODE_TYPE);
        }
        return null;
    }

    private static NetherSubLavaGeodePlacementPlan validateResource(
            ResourceRef resource,
            NetherSubLavaGeodePlacementPlan.GeodeType geodeType,
            NetherSubLavaGeodePlacementPlan.SourceSystem sourceSystem,
            ResourceRef quartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            NetherSubLavaGeodePlacementPlan.DepthMetadata depthMetadata,
            NetherSubLavaGeodePlacementPlan.LayerMetadata layerMetadata,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        if (policyService.isExcludedResource(resource.id())) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                    NetherSubLavaGeodePlacementPlan.SkipReason.STRICT_EXCLUSION);
        }
        if (!scanner.isPresent(resource)) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
        if (decision.shouldSkip()) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        if (!decision.shouldUse()) {
            return skipped(geodeType, sourceSystem, quartzResource, ancientDebrisHeartResource, origin,
                    lavaLakeAnchorMetadata, depthMetadata, layerMetadata,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                    NetherSubLavaGeodePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
        }
        return null;
    }

    private static boolean isValidGeodeType(NetherSubLavaGeodePlacementPlan.GeodeType geodeType) {
        return geodeType == NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE
                || geodeType == NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE_WITH_ANCIENT_DEBRIS_HEART;
    }

    private static NetherSubLavaGeodePlacementPlan skipped(
            NetherSubLavaGeodePlacementPlan.GeodeType geodeType,
            NetherSubLavaGeodePlacementPlan.SourceSystem sourceSystem,
            ResourceRef quartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            NetherSubLavaGeodePlacementPlan.DepthMetadata depthMetadata,
            NetherSubLavaGeodePlacementPlan.LayerMetadata layerMetadata,
            NetherSubLavaGeodePlacementPlan.Decision decision,
            NetherSubLavaGeodePlacementPlan.SkipReason skipReason
    ) {
        return NetherSubLavaGeodePlacementPlan.skipped(
                geodeType,
                sourceSystem,
                quartzResource,
                ancientDebrisHeartResource,
                origin,
                decision,
                skipReason,
                null,
                null,
                null,
                null,
                lavaLakeAnchorMetadata,
                depthMetadata,
                layerMetadata
        );
    }
}
