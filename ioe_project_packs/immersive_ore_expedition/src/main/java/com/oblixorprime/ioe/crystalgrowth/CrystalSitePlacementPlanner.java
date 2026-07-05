package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public final class CrystalSitePlacementPlanner {
    private final AmethystGrowthSiteProvider amethystProvider;
    private final Ae2CertusSiteProvider ae2Provider;
    private final GeOreSiteProvider geOreProvider;
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;
    private final GeOreWorldgenPolicy geOreWorldgenPolicy;

    public CrystalSitePlacementPlanner() {
        this(
                new AmethystGrowthSiteProvider(),
                new Ae2CertusSiteProvider(),
                new GeOreSiteProvider(),
                LoadedResourceScanner.runtime(),
                new ResourcePolicyService()
        );
    }

    public CrystalSitePlacementPlanner(
            AmethystGrowthSiteProvider amethystProvider,
            Ae2CertusSiteProvider ae2Provider,
            GeOreSiteProvider geOreProvider,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        this.amethystProvider = Objects.requireNonNull(amethystProvider, "amethystProvider");
        this.ae2Provider = Objects.requireNonNull(ae2Provider, "ae2Provider");
        this.geOreProvider = Objects.requireNonNull(geOreProvider, "geOreProvider");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
        this.geOreWorldgenPolicy = new GeOreWorldgenPolicy(scanner);
    }

    public CrystalSitePlacementPlan planAmethystSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            BlockPos origin
    ) {
        return planAmethystSite(
                anchor,
                primaryResource,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public CrystalSitePlacementPlan planAmethystSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        CrystalSitePlacementPlan rejected = CrystalSitePlacementRules.validateDirect(
                CrystalSitePlacementPlan.SourceSystem.VANILLA_AMETHYST,
                CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE,
                anchor,
                primaryResource,
                null,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolveAnchorType(anchorType, anchor,
                    IoeWorldgenFeatureKeys.AMETHYST_GROWTH_SITE));
        }
        Optional<CrystalGrowthSitePlan> plan = amethystProvider.planSite(anchor, primaryResource, scanner, policyService);
        return fromProviderPlan(
                plan,
                CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE,
                CrystalSitePlacementPlan.SourceSystem.VANILLA_AMETHYST,
                primaryResource,
                null,
                origin,
                biomeId,
                provinceId,
                resolveAnchorType(anchorType, anchor, IoeWorldgenFeatureKeys.AMETHYST_GROWTH_SITE)
        );
    }

    public CrystalSitePlacementPlan planAe2CertusSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin
    ) {
        return planAe2CertusSite(
                anchor,
                primaryResource,
                shellResource,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public CrystalSitePlacementPlan planAe2CertusSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        CrystalSitePlacementPlan rejected = CrystalSitePlacementRules.validateDirect(
                CrystalSitePlacementPlan.SourceSystem.AE2_CERTUS,
                CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE,
                anchor,
                primaryResource,
                shellResource,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolveAnchorType(anchorType, anchor,
                    IoeWorldgenFeatureKeys.AE2_CERTUS_GROWTH_SITE));
        }
        Optional<CrystalGrowthSitePlan> plan = ae2Provider.planCertusSite(
                anchor,
                primaryResource,
                Optional.ofNullable(shellResource),
                scanner,
                policyService
        );
        return fromProviderPlan(
                plan,
                CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE,
                CrystalSitePlacementPlan.SourceSystem.AE2_CERTUS,
                primaryResource,
                shellResource,
                origin,
                biomeId,
                provinceId,
                resolveAnchorType(anchorType, anchor, IoeWorldgenFeatureKeys.AE2_CERTUS_GROWTH_SITE)
        );
    }

    public CrystalSitePlacementPlan planGeOreSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            BlockPos origin
    ) {
        return planGeOreSite(
                anchor,
                primaryResource,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public CrystalSitePlacementPlan planGeOreSite(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        CrystalSitePlacementPlan rejected = CrystalSitePlacementRules.validateDirect(
                CrystalSitePlacementPlan.SourceSystem.GEORE,
                CrystalSitePlacementPlan.SiteType.GEORE_GROWTH_SITE,
                anchor,
                primaryResource,
                null,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolveAnchorType(anchorType, anchor,
                    IoeWorldgenFeatureKeys.GEORE_GROWTH_SITE));
        }
        Optional<CrystalGrowthSitePlan> plan = geOreProvider.planGeOreSite(anchor, primaryResource, scanner, policyService);
        return fromProviderPlan(
                plan,
                CrystalSitePlacementPlan.SiteType.GEORE_GROWTH_SITE,
                CrystalSitePlacementPlan.SourceSystem.GEORE,
                primaryResource,
                null,
                origin,
                biomeId,
                provinceId,
                resolveAnchorType(anchorType, anchor, IoeWorldgenFeatureKeys.GEORE_GROWTH_SITE)
        );
    }

    public CrystalSitePlacementPlan planCrystalSite(
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            CrystalSitePlacementPlan.SiteType siteType,
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            CrystalSitePlacementPlan.SiteMetadata siteMetadata,
            IoeWorldgenPlacementGates placementGates
    ) {
        CrystalSitePlacementPlan rejected = CrystalSitePlacementRules.validateDirect(
                sourceSystem,
                siteType,
                anchor,
                primaryResource,
                shellResource,
                origin,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolveAnchorType(anchorType, anchor, null));
        }
        if (siteMetadata == null) {
            return CrystalSitePlacementPlan.skipped(
                    siteType,
                    sourceSystem,
                    primaryResource,
                    shellResource,
                    origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    resolveAnchorType(anchorType, anchor, null),
                    null
            );
        }
        return CrystalSitePlacementPlan.allowed(
                siteType,
                sourceSystem,
                primaryResource,
                shellResource,
                origin,
                biomeId,
                provinceId,
                resolveAnchorType(anchorType, anchor, null),
                siteMetadata
        );
    }

    private CrystalSitePlacementPlan fromProviderPlan(
            Optional<CrystalGrowthSitePlan> providerPlan,
            CrystalSitePlacementPlan.SiteType fallbackSiteType,
            CrystalSitePlacementPlan.SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType
    ) {
        if (providerPlan.isEmpty()) {
            return CrystalSitePlacementPlan.skipped(
                    fallbackSiteType,
                    sourceSystem,
                    primaryResource,
                    shellResource,
                    origin,
                    CrystalSitePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    CrystalSitePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    anchorType,
                    null
            );
        }
        CrystalGrowthSitePlan plan = providerPlan.get();
        return CrystalSitePlacementPlan.allowed(
                toPlacementSiteType(plan.siteType()),
                sourceSystem,
                plan.coreResource(),
                plan.outerCrustResource().orElse(null),
                origin,
                biomeId,
                provinceId,
                anchorType,
                metadataFor(plan)
        );
    }

    private CrystalSitePlacementPlan.SiteMetadata metadataFor(CrystalGrowthSitePlan plan) {
        int radius = switch (plan.siteType()) {
            case AMETHYST -> 4;
            case AE2_CERTUS -> 5;
            case GEORE -> 3;
            case METEORITIC_GEODE -> throw new IllegalArgumentException("Meteoritic geode planning is out of v13 scope");
        };
        boolean renewableSite = plan.siteType() == CrystalGrowthSiteType.AMETHYST
                || plan.siteType() == CrystalGrowthSiteType.AE2_CERTUS;
        boolean disablesFreeGeOreWorldgen = plan.siteType() == CrystalGrowthSiteType.GEORE
                && (plan.disablesFreeGeOreWorldgen() || geOreWorldgenPolicy.shouldDisableFreeGeOreWorldgen());
        return new CrystalSitePlacementPlan.SiteMetadata(
                1.0D,
                1.0D,
                radius,
                radius == 0 ? 0 : radius * 2 + 1,
                renewableSite,
                disablesFreeGeOreWorldgen,
                plan.structureAnchored()
        );
    }

    private static CrystalSitePlacementPlan.SiteType toPlacementSiteType(CrystalGrowthSiteType siteType) {
        return switch (siteType) {
            case AMETHYST -> CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE;
            case AE2_CERTUS -> CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE;
            case GEORE -> CrystalSitePlacementPlan.SiteType.GEORE_GROWTH_SITE;
            case METEORITIC_GEODE -> throw new IllegalArgumentException("Meteoritic geode planning is out of v13 scope");
        };
    }

    private static ResourceLocation resolveAnchorType(
            ResourceLocation anchorType,
            ExpeditionAnchorRef anchor,
            ResourceLocation fallbackAnchorType
    ) {
        if (anchorType != null) {
            return anchorType;
        }
        if (anchor != null && !anchor.anchorType().isBlank()) {
            return ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, anchor.anchorType());
        }
        return fallbackAnchorType;
    }

    private static CrystalSitePlacementPlan withContext(
            CrystalSitePlacementPlan plan,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType
    ) {
        return CrystalSitePlacementPlan.skipped(
                plan.siteType(),
                plan.sourceSystem(),
                plan.primaryResource(),
                plan.shellResource().orElse(null),
                plan.origin(),
                plan.decision(),
                plan.skipReason(),
                biomeId,
                provinceId,
                anchorType,
                plan.siteMetadata().orElse(null)
        );
    }
}
