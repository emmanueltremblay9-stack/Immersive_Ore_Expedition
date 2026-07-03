package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RandomOreSuppressionPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);

    private final RandomOreSuppressionPlanner planner = RandomOreSuppressionPlanner.defaults();
    private final ResourcePolicyService policyService = new ResourcePolicyService();

    @Test
    void defaultConfigGatesProduceNoOpDefaultAllowPlan() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                iron,
                scannerWithLoaded(iron),
                policyService
        );

        assertFalse(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.NO_OP_DISABLED, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
        assertEquals(1.0D, plan.originalDensityMultiplier());
        assertEquals(1.0D, plan.effectiveDensityMultiplier());
        assertFalse(plan.strictExclusionApplied());
        assertFalse(plan.resourceDeniedByPolicy());
    }

    @Test
    void explicitlyEnabledRuntimeGateCanScaleRandomOreDensity() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        assertTrue(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.SCALE_DENSITY, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.NONE, plan.skipReason());
        assertEquals(1.0D, plan.originalDensityMultiplier());
        assertEquals(new OreSuppressionPolicy().densityMultiplier(), plan.effectiveDensityMultiplier());
        assertTrue(plan.resourceLoaded());
    }

    @Test
    void strictExclusionsWinOverAllowPolicy() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                tin,
                scannerWithLoaded(tin),
                policyService,
                ENABLED_GATES
        );

        assertTrue(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.STRICT_EXCLUSION, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
        assertTrue(plan.strictExclusionApplied());
        assertEquals(0.0D, plan.effectiveDensityMultiplier());
    }

    @Test
    void unloadedResourceIsSkippedSafely() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                iron,
                scannerWithNothingLoaded(),
                policyService,
                ENABLED_GATES
        );

        assertTrue(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.SKIP_UNLOADED_RESOURCE, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
        assertFalse(plan.resourceLoaded());
        assertTrue(plan.resourceDeniedByPolicy());
    }

    @Test
    void policyDeniedResourceIsRejectedSafely() {
        ResourceRef dirt = ResourceRef.block("minecraft", "dirt");

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                dirt,
                scannerWithLoaded(dirt),
                policyService,
                ENABLED_GATES
        );

        assertTrue(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.POLICY_DENIED, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
        assertTrue(plan.resourceLoaded());
        assertTrue(plan.resourceDeniedByPolicy());
    }

    @Test
    void acceptedLoadedResourceUsesExistingSuppressionPolicy() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        OreSuppressionPolicy suppressionPolicy = new OreSuppressionPolicy();

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        if (suppressionPolicy.densityMultiplier() == 1.0D) {
            assertEquals(RandomOreSuppressionPlan.Decision.ALLOW_ORIGINAL, plan.decision());
        } else {
            assertEquals(RandomOreSuppressionPlan.Decision.SCALE_DENSITY, plan.decision());
        }
        assertEquals(suppressionPolicy.densityMultiplier(), plan.effectiveDensityMultiplier());
        assertEquals(RandomOreSuppressionPlan.SourceReason.ORE_SUPPRESSION_POLICY, plan.sourceReason());
    }

    @Test
    void invalidDensityMultiplierIsHandledSafelyWhenConstructed() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        RandomOreSuppressionRules rules = new RandomOreSuppressionRules();

        RandomOreSuppressionPlan plan = rules.planApprovedLoadedResource(
                iron,
                true,
                Double.NaN,
                null,
                null
        );

        assertFalse(plan.decisionProduced());
        assertEquals(RandomOreSuppressionPlan.Decision.INVALID_INPUT, plan.decision());
        assertEquals(RandomOreSuppressionPlan.SkipReason.INVALID_DENSITY_MULTIPLIER, plan.skipReason());
        assertEquals(1.0D, plan.effectiveDensityMultiplier());
    }

    @Test
    void optionalBiomeAndProvinceMetadataIsPreserved() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ResourceLocation biomeId = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        ResourceLocation provinceId = ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                "default"
        );

        RandomOreSuppressionPlan plan = planner.planRandomOreSuppression(
                iron,
                scannerWithLoaded(iron),
                policyService,
                biomeId,
                provinceId,
                ENABLED_GATES
        );

        assertEquals(Optional.of(biomeId), plan.biomeId());
        assertEquals(Optional.of(provinceId), plan.provinceId());
    }

    @Test
    void v9OreLoadChamberPlanningBehaviorRemainsUnchanged() {
        OreLoadChamberPlacementPlanner chamberPlanner = OreLoadChamberPlacementPlanner.defaults();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        OreLoadChamberPlacementPlan plan = chamberPlanner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.NORMAL),
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.NONE, plan.skipReason());
    }

    @Test
    void existingOreLoadGeneratorPlanningBehaviorRemainsUnchanged() {
        OreLoadGenerator generator = new OreLoadGenerator();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithLoaded(iron);

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor(SiteQuality.NORMAL),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                ProvinceRuntimeIntegration.disabled(policyService, scanner)
        );

        assertTrue(plan.isPresent());
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithSuppressionPlanningMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertTrue(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertTrue(registration.oreLoadChamberPlacementPlanningReady());
        assertTrue(registration.randomOreSuppressionPlanningReady());
        assertFalse(registration.configuredFeaturesRegistered());
        assertFalse(registration.placedFeaturesRegistered());
        assertFalse(registration.biomeModifiersRegistered());
    }

    @Test
    void randomOreSuppressionPlanningDoesNotIntroduceLegacyNamespaces() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.futureFeatureKeys().isEmpty());
        for (ResourceLocation featureKey : registration.futureFeatureKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, featureKey.getNamespace());
        }
    }

    private static OreLoadPlan oreLoadPlan(ResourceRef resource, SiteQuality quality) {
        ExpeditionAnchorRef anchor = anchor(quality);
        BlockPos center = new BlockPos(16, 64, 0);
        return new OreLoadPlan(
                anchor,
                resource,
                center,
                quality,
                false,
                anchor.pos().distManhattan(center)
        );
    }

    private static ExpeditionAnchorRef anchor(SiteQuality quality) {
        return new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                quality
        );
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded));
    }

    private static LoadedResourceScanner scannerWithNothingLoaded() {
        return new TestScanner(Set.of());
    }

    private record TestScanner(Set<ResourceRef> loadedResources) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK, id));
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID, id));
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM, id));
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK_TAG, id));
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID_TAG, id));
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM_TAG, id));
        }
    }
}
