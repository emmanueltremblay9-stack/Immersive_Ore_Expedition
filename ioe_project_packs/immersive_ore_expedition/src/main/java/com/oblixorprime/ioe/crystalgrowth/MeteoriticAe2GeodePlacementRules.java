package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;

final class MeteoriticAe2GeodePlacementRules {
    private MeteoriticAe2GeodePlacementRules() {
    }

    static MeteoriticAe2GeodePlacementPlan validateDirect(
            MeteoriticAe2GeodePlacementPlan.GeodeType geodeType,
            MeteoriticAe2GeodePlacementPlan.SourceSystem sourceSystem,
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null || scanner == null || policyService == null || sourceSystem == null) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (placementGates.shouldNoOpRuntimePlacement() || !IoeCrystalGrowthConfig.meteoriticGeodeEnabled()) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RUNTIME_DISABLED,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED);
        }
        if (origin == null) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.NULL_ORIGIN);
        }
        if (primaryResource == null) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.NULL_RESOURCE);
        }
        if (!CrystalGrowthSiteRules.canPlanAnchoredSite(anchor)) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (isFakeFluix(primaryResource)
                || isFakeFluix(skyStoneCrustResource)
                || isFakeFluix(middleLayerResource)
                || isFakeFluix(crystalCoreResource)) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_FAKE_FLUIX_FORBIDDEN,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.FAKE_FLUIX_FORBIDDEN);
        }
        if (requiresSkyStoneCrust(layerMetadata) && skyStoneCrustResource == null) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        if (!isValidGeodeType(geodeType) || !isValidResourceShape(primaryResource, skyStoneCrustResource,
                middleLayerResource, crystalCoreResource, layerMetadata)) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_GEODE_TYPE,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_GEODE_TYPE);
        }
        if (layerMetadata == null || !layerMetadata.isValid()) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_LAYER_METADATA,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_LAYER_METADATA);
        }
        if (IoeCrystalGrowthConfig.meteoriticGeodeRequiresAe2()
                && !CrystalGrowthCompatGates.ae2CrystalProcessingStackEnabled(scanner)) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT);
        }

        MeteoriticAe2GeodePlacementPlan rejectedPrimary = validateResource(primaryResource, geodeType, sourceSystem,
                primaryResource, skyStoneCrustResource, middleLayerResource, crystalCoreResource, origin,
                layerMetadata, scanner, policyService);
        if (rejectedPrimary != null) {
            return rejectedPrimary;
        }
        if (skyStoneCrustResource != null) {
            MeteoriticAe2GeodePlacementPlan rejectedCrust = validateResource(skyStoneCrustResource, geodeType,
                    sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource, crystalCoreResource,
                    origin, layerMetadata, scanner, policyService);
            if (rejectedCrust != null) {
                return rejectedCrust;
            }
        }
        if (middleLayerResource != null) {
            MeteoriticAe2GeodePlacementPlan rejectedMiddle = validateResource(middleLayerResource, geodeType,
                    sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource, crystalCoreResource,
                    origin, layerMetadata, scanner, policyService);
            if (rejectedMiddle != null) {
                return rejectedMiddle;
            }
        }
        if (crystalCoreResource != null) {
            return validateResource(crystalCoreResource, geodeType, sourceSystem, primaryResource,
                    skyStoneCrustResource, middleLayerResource, crystalCoreResource, origin, layerMetadata, scanner,
                    policyService);
        }
        return null;
    }

    private static MeteoriticAe2GeodePlacementPlan validateResource(
            ResourceRef resource,
            MeteoriticAe2GeodePlacementPlan.GeodeType geodeType,
            MeteoriticAe2GeodePlacementPlan.SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        if (policyService.isExcludedResource(resource.id())) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.STRICT_EXCLUSION);
        }
        if (!scanner.isPresent(resource)) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
        if (decision.shouldSkip()) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        if (!decision.shouldUse()) {
            return skipped(geodeType, sourceSystem, primaryResource, skyStoneCrustResource, middleLayerResource,
                    crystalCoreResource, origin, layerMetadata,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
        }
        return null;
    }

    private static boolean isValidGeodeType(MeteoriticAe2GeodePlacementPlan.GeodeType geodeType) {
        return geodeType == MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE
                || geodeType == MeteoriticAe2GeodePlacementPlan.GeodeType.SKY_STONE_CRUSTED_CERTUS_GEODE;
    }

    private static boolean isValidResourceShape(
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata
    ) {
        boolean crustRequired = requiresSkyStoneCrust(layerMetadata);
        if (!isAe2CertusBlock(primaryResource)) {
            return false;
        }
        if (crustRequired && !isSkyStoneBlock(skyStoneCrustResource)) {
            return false;
        }
        if (skyStoneCrustResource != null && !isSkyStoneBlock(skyStoneCrustResource)) {
            return false;
        }
        if (middleLayerResource != null) {
            return false;
        }
        return crystalCoreResource == null || isAe2CertusBlock(crystalCoreResource);
    }

    private static boolean requiresSkyStoneCrust(
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata
    ) {
        return IoeCrystalGrowthConfig.skyStoneCrustAroundGeodes()
                || (layerMetadata != null && layerMetadata.skyStoneCrustRequired());
    }

    private static boolean isFakeFluix(ResourceRef resource) {
        return resource != null && resource.id().getPath().contains("fluix");
    }

    private static boolean isAe2CertusBlock(ResourceRef resource) {
        return isAe2Block(resource)
                && CrystalGrowthCompatGates.isNativeBuddingCertusPath(resource.id().getPath());
    }

    private static boolean isSkyStoneBlock(ResourceRef resource) {
        return isAe2Block(resource) && isSkyStonePath(resource.id().getPath());
    }

    private static boolean isAe2Block(ResourceRef resource) {
        return resource != null
                && resource.type() == ResourceType.BLOCK
                && (CrystalGrowthCompatGates.AE2.equals(resource.id().getNamespace())
                || "appeng".equals(resource.id().getNamespace()));
    }

    private static boolean isSkyStonePath(String path) {
        return "sky_stone_block".equals(path);
    }

    private static MeteoriticAe2GeodePlacementPlan skipped(
            MeteoriticAe2GeodePlacementPlan.GeodeType geodeType,
            MeteoriticAe2GeodePlacementPlan.SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata,
            MeteoriticAe2GeodePlacementPlan.Decision decision,
            MeteoriticAe2GeodePlacementPlan.SkipReason skipReason
    ) {
        return MeteoriticAe2GeodePlacementPlan.skipped(
                geodeType,
                sourceSystem,
                primaryResource,
                skyStoneCrustResource,
                middleLayerResource,
                crystalCoreResource,
                origin,
                decision,
                skipReason,
                null,
                null,
                null,
                layerMetadata
        );
    }
}
