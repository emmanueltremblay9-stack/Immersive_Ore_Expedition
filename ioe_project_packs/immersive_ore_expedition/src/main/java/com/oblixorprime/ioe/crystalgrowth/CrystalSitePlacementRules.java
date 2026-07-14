package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;

final class CrystalSitePlacementRules {
    private CrystalSitePlacementRules() {
    }

    static CrystalSitePlacementPlan validateDirect(
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            CrystalSitePlacementPlan.SiteType siteType,
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null || scanner == null || policyService == null || sourceSystem == null) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_RUNTIME_DISABLED,
                    CrystalSitePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED);
        }
        if (origin == null) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.NULL_ORIGIN);
        }
        if (primaryResource == null) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.NULL_RESOURCE);
        }
        if (!CrystalGrowthSiteRules.canPlanAnchoredSite(anchor)) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.INVALID_INPUT);
        }
        if (isFakeFluix(primaryResource) || isFakeFluix(shellResource)) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_FAKE_FLUIX_FORBIDDEN,
                    CrystalSitePlacementPlan.SkipReason.FAKE_FLUIX_FORBIDDEN);
        }
        if (!isValidSiteType(sourceSystem, siteType) || !isValidResourceShape(sourceSystem, primaryResource, shellResource)) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_SITE_TYPE,
                    CrystalSitePlacementPlan.SkipReason.INVALID_SITE_TYPE);
        }
        if (!optionalModLoaded(sourceSystem, scanner)) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT,
                    CrystalSitePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT);
        }
        CrystalSitePlacementPlan rejectedPrimary = validateResource(primaryResource, sourceSystem, siteType,
                primaryResource, shellResource, origin, scanner, policyService);
        if (rejectedPrimary != null) {
            return rejectedPrimary;
        }
        if (shellResource != null) {
            return validateResource(shellResource, sourceSystem, siteType,
                    primaryResource, shellResource, origin, scanner, policyService);
        }
        return null;
    }

    static boolean isValidSiteType(
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            CrystalSitePlacementPlan.SiteType siteType
    ) {
        if (sourceSystem == null || siteType == null) {
            return false;
        }
        return switch (sourceSystem) {
            case VANILLA_AMETHYST -> siteType == CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE;
            case AE2_CERTUS -> siteType == CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE;
            case GEORE -> siteType == CrystalSitePlacementPlan.SiteType.GEORE_GROWTH_SITE;
        };
    }

    private static CrystalSitePlacementPlan validateResource(
            ResourceRef resource,
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            CrystalSitePlacementPlan.SiteType siteType,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        if (policyService.isExcludedResource(resource.id())) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_STRICT_EXCLUSION,
                    CrystalSitePlacementPlan.SkipReason.STRICT_EXCLUSION);
        }
        if (!scanner.isPresent(resource)) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    CrystalSitePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
        if (decision.shouldSkip()) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED,
                    CrystalSitePlacementPlan.SkipReason.RESOURCE_NOT_LOADED);
        }
        if (!decision.shouldUse()) {
            return skipped(siteType, sourceSystem, primaryResource, shellResource, origin,
                    CrystalSitePlacementPlan.Decision.SKIP_RESOURCE_DENIED,
                    CrystalSitePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY);
        }
        return null;
    }

    private static boolean isValidResourceShape(
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef shellResource
    ) {
        return switch (sourceSystem) {
            case VANILLA_AMETHYST -> isBlock(primaryResource)
                    && isNamespace(primaryResource, "minecraft")
                    && primaryResource.id().getPath().contains("amethyst")
                    && shellResource == null;
            case AE2_CERTUS -> isBlock(primaryResource)
                    && isAe2Namespace(primaryResource)
                    && CrystalGrowthCompatGates.isNativeBuddingCertusPath(primaryResource.id().getPath())
                    && isValidAe2Shell(shellResource);
            case GEORE -> isBlock(primaryResource)
                    && isNamespace(primaryResource, CrystalGrowthCompatGates.GEORE)
                    && shellResource == null;
        };
    }

    private static boolean isValidAe2Shell(ResourceRef shellResource) {
        if (!IoeCrystalGrowthConfig.skyStoneCrustAroundGeodes()) {
            return shellResource == null || isSkyStoneShell(shellResource);
        }
        return isSkyStoneShell(shellResource);
    }

    private static boolean isSkyStoneShell(ResourceRef shellResource) {
        return isBlock(shellResource)
                && isAe2Namespace(shellResource)
                && isSkyStonePath(shellResource.id().getPath());
    }

    private static boolean optionalModLoaded(
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            LoadedResourceScanner scanner
    ) {
        return switch (sourceSystem) {
            case VANILLA_AMETHYST -> true;
            case AE2_CERTUS -> CrystalGrowthCompatGates.ae2CrystalProcessingStackEnabled(scanner);
            case GEORE -> CrystalGrowthCompatGates.georeEnabled(scanner);
        };
    }

    private static boolean isFakeFluix(ResourceRef resource) {
        return resource != null && resource.id().getPath().contains("fluix");
    }

    private static boolean isBlock(ResourceRef resource) {
        return resource != null && resource.type() == ResourceType.BLOCK;
    }

    private static boolean isAe2Namespace(ResourceRef resource) {
        return isNamespace(resource, CrystalGrowthCompatGates.AE2) || isNamespace(resource, "appeng");
    }

    private static boolean isNamespace(ResourceRef resource, String namespace) {
        return resource != null && namespace.equals(resource.id().getNamespace());
    }

    private static boolean isSkyStonePath(String path) {
        return "sky_stone_block".equals(path);
    }

    private static CrystalSitePlacementPlan skipped(
            CrystalSitePlacementPlan.SiteType siteType,
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            CrystalSitePlacementPlan.Decision decision,
            CrystalSitePlacementPlan.SkipReason skipReason
    ) {
        return CrystalSitePlacementPlan.skipped(
                siteType,
                sourceSystem,
                primaryResource,
                shellResource,
                origin,
                decision,
                skipReason,
                null,
                null,
                null,
                null
        );
    }
}
