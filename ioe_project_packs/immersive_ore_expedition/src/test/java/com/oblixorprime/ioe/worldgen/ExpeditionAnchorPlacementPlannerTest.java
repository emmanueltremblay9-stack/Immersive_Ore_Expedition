package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceResourcePolicy;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
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

final class ExpeditionAnchorPlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);

    private final ExpeditionAnchorPlacementPlanner planner = ExpeditionAnchorPlacementPlanner.defaults();

    @Test
    void defaultConfigGatesKeepAnchorPlacementNoOp() {
        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                BlockPos.ZERO,
                SiteQuality.NORMAL
        );

        assertFalse(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
    }

    @Test
    void explicitlyEnabledRuntimeGateCanPlanKnownAnchorPlacement() {
        ResourceLocation biomeId = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        ResourceLocation provinceId = ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                "default"
        );

        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.COLLAPSED_SHAFT,
                new BlockPos(8, 64, 8),
                SiteQuality.RICH,
                biomeId,
                provinceId,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.NONE, plan.skipReason());
        assertEquals(Optional.of(biomeId), plan.biomeId());
        assertEquals(Optional.of(provinceId), plan.provinceId());
    }

    @Test
    void unknownAnchorKeyIsRejectedSafely() {
        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "unknown_anchor"),
                BlockPos.ZERO,
                SiteQuality.NORMAL,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.UNKNOWN_ANCHOR_TYPE, plan.skipReason());
    }

    @Test
    void knownAnchorKeysUseActiveNamespaceOnly() {
        ExpeditionAnchorPlacementRules rules = ExpeditionAnchorPlacementRules.defaults();

        assertFalse(rules.knownAnchorKeys().isEmpty());
        for (ResourceLocation anchorKey : rules.knownAnchorKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, anchorKey.getNamespace());
        }
    }

    @Test
    void legacySplitNamespacesAreRejectedAsAnchorDefaults() {
        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                ResourceLocation.fromNamespaceAndPath("ioe_expedition_worldgen", "tiny_vertical_mine_entrance"),
                BlockPos.ZERO,
                SiteQuality.NORMAL,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.INVALID_NAMESPACE, plan.skipReason());
    }

    @Test
    void nullOriginIsRejectedSafely() {
        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.MINER_CAMP,
                null,
                SiteQuality.NORMAL,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.NULL_ORIGIN, plan.skipReason());
    }

    @Test
    void nullSiteQualityIsRejectedSafely() {
        ExpeditionAnchorPlacementPlan plan = planner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.BURIED_SURVEY_MARKER,
                BlockPos.ZERO,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.NULL_SITE_QUALITY, plan.skipReason());
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithAnchorPlanningMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertTrue(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertEquals(IoeWorldgenFeatureKeys.anchorFeatureKeys(), registration.anchorPlacementPlanKeys());
        assertFalse(registration.configuredFeaturesRegistered());
        assertFalse(registration.placedFeaturesRegistered());
        assertFalse(registration.biomeModifiersRegistered());
    }

    @Test
    void existingOreLoadGeneratorPlanningBehaviorRemainsUnchanged() {
        OreLoadGenerator generator = new OreLoadGenerator();
        ResourcePolicyService policyService = new ResourcePolicyService();
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        LoadedResourceScanner scanner = scannerWithLoaded(iron);

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor(),
                iron,
                new BlockPos(16, 64, 0),
                scanner,
                policyService,
                ProvinceRuntimeIntegration.disabled(policyService, scanner)
        );

        assertTrue(plan.isPresent());
    }

    @Test
    void strictExclusionsRemainUnaffectedByAnchorPlacementPlanning() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        planner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR,
                BlockPos.ZERO,
                SiteQuality.NORMAL,
                ENABLED_GATES
        );

        ResourcePolicyDecision decision = ProvinceResourcePolicy.defaults().evaluate(tin);
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertFalse(decision.shouldUse());
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                SiteQuality.NORMAL
        );
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded));
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
