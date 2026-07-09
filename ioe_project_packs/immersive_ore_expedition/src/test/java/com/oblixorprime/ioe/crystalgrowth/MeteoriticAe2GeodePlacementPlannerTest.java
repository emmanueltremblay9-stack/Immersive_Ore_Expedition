package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
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

final class MeteoriticAe2GeodePlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);
    private static final BlockPos ORIGIN = new BlockPos(96, 32, -48);
    private static final ResourceLocation ANCHOR_TYPE =
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "meteoritic_ae2_geode");
    private static final Set<String> LEGACY_SPLIT_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    private final ResourcePolicyService policyService = new ResourcePolicyService();
    private final Ae2CertusSiteProvider ae2Provider = new Ae2CertusSiteProvider();

    @Test
    void defaultConfigGatesProduceNoOpMeteoriticAe2GeodePlan() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                ORIGIN
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RUNTIME_DISABLED, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
    }

    @Test
    void enabledRuntimeGateCanPlanMeteoriticAe2Geode() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        ResourceRef middle = certusMiddleLayer();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust, middle)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                middle,
                certus,
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE, plan.geodeType());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SourceSystem.AE2_METEORITIC, plan.sourceSystem());
        assertEquals(certus, plan.primaryResource());
        assertEquals(Optional.of(crust), plan.skyStoneCrustResource());
        assertEquals(Optional.of(middle), plan.middleLayerResource());
        assertEquals(Optional.of(certus), plan.crystalCoreResource());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
        assertTrue(plan.layerMetadata().orElseThrow().skyStoneCrustRequired());
        assertTrue(plan.layerMetadata().orElseThrow().requiresStructureAnchor());
    }

    @Test
    void optionalAe2AbsenceIsHandledSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(Set.of(), Set.of(certus, crust));

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT, plan.skipReason());
    }

    @Test
    void missingSkyStoneCrustResourceIsHandledSafelyWhenRequired() {
        ResourceRef certus = certus();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                null,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void unloadedResourceIsSkippedSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void policyDeniedResourceIsSkippedSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        ResourceRef deniedMiddleLayer = ResourceRef.block("ae2", "decorative_layer");
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust, deniedMiddleLayer)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                deniedMiddleLayer,
                certus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_RESOURCE_DENIED, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void strictExclusionsWin() {
        ResourceRef excludedCertus = ResourceRef.block("ae2", "tin_certus_quartz");
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(excludedCertus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                excludedCertus,
                crust,
                null,
                excludedCertus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_STRICT_EXCLUSION, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
    }

    @Test
    void nullOriginIsRejectedSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                null,
                certus,
                null,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.NULL_ORIGIN, plan.skipReason());
    }

    @Test
    void nullResourceIsRejectedSafely() {
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                null,
                crust,
                null,
                null,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.NULL_RESOURCE, plan.skipReason());
    }

    @Test
    void invalidGeodeTypeIsRejectedSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                null,
                MeteoriticAe2GeodePlacementPlan.SourceSystem.AE2_METEORITIC,
                anchor(),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                MeteoriticAe2GeodePlacementPlanner.defaultLayerMetadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_GEODE_TYPE, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_GEODE_TYPE, plan.skipReason());
    }

    @Test
    void invalidLayerMetadataIsRejectedSafely() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata invalidMetadata =
                new MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata(
                        32,
                        2,
                        5,
                        3,
                        1.0D,
                        1.0D,
                        true,
                        true,
                        true,
                        true
                );
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE,
                MeteoriticAe2GeodePlacementPlan.SourceSystem.AE2_METEORITIC,
                anchor(),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                invalidMetadata,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_LAYER_METADATA, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_LAYER_METADATA, plan.skipReason());
    }

    @Test
    void fakeFluixOreIsForbidden() {
        ResourceRef fluix = ResourceRef.block("ae2", "fluix_ore");
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(fluix, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                fluix,
                crust,
                null,
                fluix,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(MeteoriticAe2GeodePlacementPlan.Decision.SKIP_FAKE_FLUIX_FORBIDDEN, plan.decision());
        assertEquals(MeteoriticAe2GeodePlacementPlan.SkipReason.FAKE_FLUIX_FORBIDDEN, plan.skipReason());
    }

    @Test
    void biomeProvinceMetadataFromV11ContextIsPreserved() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceContext.resolved(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "meteor_fields"),
                false
        );
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                null,
                certus,
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
    void existingAe2CertusSiteProviderBehaviorRemainsUnchanged() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();

        Optional<CrystalGrowthSitePlan> plan = ae2Provider.planCertusSite(
                anchor(),
                certus,
                Optional.of(crust),
                scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus, crust)),
                policyService
        );

        assertTrue(plan.isPresent());
        assertEquals(CrystalGrowthSiteType.AE2_CERTUS, plan.get().siteType());
        assertEquals(certus, plan.get().coreResource());
        assertEquals(crust, plan.get().outerCrustResource().orElseThrow());
        assertTrue(plan.get().meteoriticVariant());
    }

    @Test
    void v13CrystalSitePlacementPlannerBehaviorRemainsUnchanged() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        CrystalSitePlacementPlanner planner = new CrystalSitePlacementPlanner(
                new AmethystGrowthSiteProvider(),
                ae2Provider,
                new GeOreSiteProvider(),
                scanner(Set.of(CrystalGrowthCompatGates.AE2), Set.of(certus, crust)),
                policyService
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
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithMeteoriticAe2GeodeMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.crystalAe2SitePlacementPlanningReady());
        assertTrue(registration.meteoriticAe2GeodePlacementPlanningReady());
        assertTrue(registration.futureFeatureKeys().contains(IoeWorldgenFeatureKeys.METEORITIC_AE2_GEODE));
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroduced() {
        ResourceRef certus = certus();
        ResourceRef crust = skyStone();
        MeteoriticAe2GeodePlacementPlanner planner = planner(
                Set.of(CrystalGrowthCompatGates.AE2),
                Set.of(certus, crust)
        );

        MeteoriticAe2GeodePlacementPlan plan = planner.planMeteoriticAe2Geode(
                anchor(),
                certus,
                crust,
                null,
                certus,
                ORIGIN,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, plan.anchorType().orElseThrow().getNamespace());
        for (ResourceLocation featureKey : IoeWorldgenFeatureKeys.allFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
            assertFalse(LEGACY_SPLIT_NAMESPACES.contains(featureKey.getNamespace()));
        }
    }

    private MeteoriticAe2GeodePlacementPlanner planner(Set<String> mods, Set<ResourceRef> resources) {
        return new MeteoriticAe2GeodePlacementPlanner(
                ae2Provider,
                scanner(mods, resources),
                policyService
        );
    }

    private static CrystalGrowthTestScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new CrystalGrowthTestScanner(mods, resources);
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(Level.OVERWORLD, ORIGIN, "meteoritic_ae2_geode", SiteQuality.NORMAL);
    }

    private static ResourceRef certus() {
        return ResourceRef.block("ae2", "budding_certus_quartz");
    }

    private static ResourceRef skyStone() {
        return ResourceRef.block("ae2", "sky_stone");
    }

    private static ResourceRef certusMiddleLayer() {
        return ResourceRef.block("ae2", "certus_quartz_block");
    }
}
