package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.worldgen.IoeWorldgenBootstrap;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import com.oblixorprime.ioe.worldgen.IoeWorldgenRegistration;
import com.oblixorprime.ioe.worldgen.WorldgenBiomeProvinceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrystalSitePlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);
    private static final BlockPos ORIGIN = new BlockPos(48, 42, 64);
    private static final ResourceLocation ANCHOR_TYPE =
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "crystal_growth_chamber");

    private final ResourcePolicyService policyService = new ResourcePolicyService();
    private final AmethystGrowthSiteProvider amethystProvider = new AmethystGrowthSiteProvider();
    private final Ae2CertusSiteProvider ae2Provider = new Ae2CertusSiteProvider();
    private final GeOreSiteProvider geOreProvider = new GeOreSiteProvider();

    @Test
    void defaultConfigGatesProduceNoOpCrystalSitePlan() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));

        CrystalSitePlacementPlan plan = planner.planAmethystSite(anchor(), amethyst, ORIGIN);

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_RUNTIME_DISABLED, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
    }

    @Test
    void enabledRuntimeGateCanPlanAmethystGrowthSite() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));

        CrystalSitePlacementPlan plan = planner.planAmethystSite(
                anchor(),
                amethyst,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE, plan.siteType());
        assertEquals(CrystalSitePlacementPlan.SourceSystem.VANILLA_AMETHYST, plan.sourceSystem());
        assertEquals(amethyst, plan.primaryResource());
        assertTrue(plan.shellResource().isEmpty());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
        assertTrue(plan.siteMetadata().orElseThrow().renewableSite());
        assertFalse(plan.siteMetadata().orElseThrow().disablesFreeGeOreWorldgen());
    }

    @Test
    void enabledRuntimeGateCanPlanAe2CertusGrowthSite() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalSitePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        CrystalSitePlacementPlan plan = planner.planAe2CertusSite(
                anchor(),
                certus,
                crust,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE, plan.siteType());
        assertEquals(CrystalSitePlacementPlan.SourceSystem.AE2_CERTUS, plan.sourceSystem());
        assertEquals(certus, plan.primaryResource());
        assertEquals(Optional.of(crust), plan.shellResource());
        assertTrue(plan.siteMetadata().orElseThrow().renewableSite());
    }

    @Test
    void enabledRuntimeGateCanPlanGeOreGrowthSite() {
        ResourceRef diamondGeOre = ResourceRef.block("geore", "diamond_geore");
        CrystalSitePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.GEORE),
                Set.of(diamondGeOre)
        );

        CrystalSitePlacementPlan plan = planner.planGeOreSite(
                anchor(),
                diamondGeOre,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.SiteType.GEORE_GROWTH_SITE, plan.siteType());
        assertEquals(CrystalSitePlacementPlan.SourceSystem.GEORE, plan.sourceSystem());
        assertEquals(diamondGeOre, plan.primaryResource());
        assertTrue(plan.siteMetadata().orElseThrow().disablesFreeGeOreWorldgen());
        assertFalse(plan.siteMetadata().orElseThrow().renewableSite());
    }

    @Test
    void optionalAe2AbsenceIsHandledSafely() {
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(certus, crust));

        CrystalSitePlacementPlan plan = planner.planAe2CertusSite(
                anchor(),
                certus,
                crust,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT, plan.skipReason());
    }

    @Test
    void optionalGeOreAbsenceIsHandledSafely() {
        ResourceRef diamondGeOre = ResourceRef.block("geore", "diamond_geore");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(diamondGeOre));

        CrystalSitePlacementPlan plan = planner.planGeOreSite(
                anchor(),
                diamondGeOre,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT, plan.skipReason());
    }

    @Test
    void unloadedResourceIsSkippedSafely() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of());

        CrystalSitePlacementPlan plan = planner.planAmethystSite(
                anchor(),
                amethyst,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void policyDeniedResourceIsSkippedSafely() {
        ResourceRef dirtGeOre = ResourceRef.block("geore", "dirt_geore");
        CrystalSitePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.GEORE),
                Set.of(dirtGeOre)
        );

        CrystalSitePlacementPlan plan = planner.planGeOreSite(
                anchor(),
                dirtGeOre,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_RESOURCE_DENIED, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void strictExclusionsWin() {
        ResourceRef tinGeOre = ResourceRef.block("geore", "tin_geore");
        CrystalSitePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.GEORE),
                Set.of(tinGeOre)
        );

        CrystalSitePlacementPlan plan = planner.planGeOreSite(
                anchor(),
                tinGeOre,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_STRICT_EXCLUSION, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
    }

    @Test
    void nullOriginIsRejectedSafely() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));

        CrystalSitePlacementPlan plan = planner.planAmethystSite(
                anchor(),
                amethyst,
                null,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.SkipReason.NULL_ORIGIN, plan.skipReason());
    }

    @Test
    void nullResourceIsRejectedSafely() {
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of());

        CrystalSitePlacementPlan plan = planner.planCrystalSite(
                CrystalSitePlacementPlan.SourceSystem.VANILLA_AMETHYST,
                CrystalSitePlacementPlan.SiteType.AMETHYST_GROWTH_SITE,
                anchor(),
                null,
                null,
                ORIGIN,
                null,
                null,
                null,
                metadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.SkipReason.NULL_RESOURCE, plan.skipReason());
    }

    @Test
    void invalidSiteTypeIsRejectedSafely() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));

        CrystalSitePlacementPlan plan = planner.planCrystalSite(
                CrystalSitePlacementPlan.SourceSystem.VANILLA_AMETHYST,
                CrystalSitePlacementPlan.SiteType.AE2_CERTUS_GROWTH_SITE,
                anchor(),
                amethyst,
                null,
                ORIGIN,
                null,
                null,
                null,
                metadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_INVALID_SITE_TYPE, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.INVALID_SITE_TYPE, plan.skipReason());
    }

    @Test
    void fakeFluixOreIsForbidden() {
        ResourceRef fluix = ResourceRef.block("ae2", "fluix_ore");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        CrystalSitePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(fluix, crust)
        );

        CrystalSitePlacementPlan plan = planner.planAe2CertusSite(
                anchor(),
                fluix,
                crust,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(CrystalSitePlacementPlan.Decision.SKIP_FAKE_FLUIX_FORBIDDEN, plan.decision());
        assertEquals(CrystalSitePlacementPlan.SkipReason.FAKE_FLUIX_FORBIDDEN, plan.skipReason());
    }

    @Test
    void biomeProvinceMetadataFromV11ContextIsPreserved() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceContext.resolved(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "crystal_fields"),
                false
        );

        CrystalSitePlacementPlan plan = planner.planAmethystSite(
                anchor(),
                amethyst,
                ORIGIN,
                context.biomeId().orElse(null),
                context.provinceId().orElse(null),
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(context.biomeId(), plan.biomeId());
        assertEquals(context.provinceId(), plan.provinceId());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
    }

    @Test
    void existingCrystalGrowthProviderBehaviorRemainsUnchanged() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        ResourceRef certus = ResourceRef.block("ae2", "budding_certus_quartz");
        ResourceRef crust = ResourceRef.block("ae2", "sky_stone");
        ResourceRef diamondGeOre = ResourceRef.block("geore", "diamond_geore");

        assertTrue(amethystProvider.planSite(anchor(), amethyst, scanner(Set.of(), Set.of(amethyst)), policyService).isPresent());
        assertTrue(ae2Provider.planCertusSite(
                anchor(),
                certus,
                Optional.of(crust),
                scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus, crust)),
                policyService
        ).isPresent());
        assertTrue(geOreProvider.planGeOreSite(
                anchor(),
                diamondGeOre,
                scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of(diamondGeOre)),
                policyService
        ).isPresent());

        GeOreWorldgenPolicy geOreWorldgenPolicy = new GeOreWorldgenPolicy(
                scanner(Set.of(CrystalGrowthCompatGates.GEORE), Set.of())
        );
        assertTrue(geOreWorldgenPolicy.shouldDisableFreeGeOreWorldgen());
        assertTrue(geOreWorldgenPolicy.mustAnchorAllGeOreSites());
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithCrystalSiteMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertTrue(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertTrue(registration.oreLoadChamberPlacementPlanningReady());
        assertTrue(registration.randomOreSuppressionPlanningReady());
        assertTrue(registration.liveBiomeProvinceBindingPlanningReady());
        assertTrue(registration.ieIpSurfaceCluePlacementPlanningReady());
        assertTrue(registration.crystalAe2SitePlacementPlanningReady());
        assertFalse(registration.configuredFeaturesRegistered());
        assertFalse(registration.placedFeaturesRegistered());
        assertFalse(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroduced() {
        ResourceRef amethyst = ResourceRef.block("minecraft", "amethyst_block");
        CrystalSitePlacementPlanner planner = planner(Set.of(), Set.of(amethyst));

        CrystalSitePlacementPlan plan = planner.planAmethystSite(
                anchor(),
                amethyst,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, plan.anchorType().orElseThrow().getNamespace());
        for (ResourceLocation featureKey : IoeWorldgenFeatureKeys.allFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
        }
    }

    private CrystalSitePlacementPlanner planner(Set<String> mods, Set<ResourceRef> resources) {
        return new CrystalSitePlacementPlanner(
                amethystProvider,
                ae2Provider,
                geOreProvider,
                scanner(mods, resources),
                policyService
        );
    }

    private static CrystalGrowthTestScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new CrystalGrowthTestScanner(mods, resources);
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, ORIGIN, "crystal_growth_chamber", SiteQuality.NORMAL);
    }

    private static CrystalSitePlacementPlan.SiteMetadata metadata() {
        return new CrystalSitePlacementPlan.SiteMetadata(1.0D, 1.0D, 3, 7, true, false, true);
    }
}
