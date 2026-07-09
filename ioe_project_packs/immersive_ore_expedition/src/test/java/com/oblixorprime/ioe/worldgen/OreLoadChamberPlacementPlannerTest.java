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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreLoadChamberPlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);

    private final OreLoadChamberPlacementPlanner planner = OreLoadChamberPlacementPlanner.defaults();
    private final ResourcePolicyService policyService = new ResourcePolicyService();

    @Test
    void defaultConfigGatesKeepOreLoadChamberPlacementNoOp() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.NORMAL),
                scannerWithLoaded(iron),
                policyService
        );

        assertFalse(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
        assertTrue(plan.chamberMetadata().isPresent());
    }

    @Test
    void explicitlyEnabledRuntimeGateCanPlanValidOreLoadChamber() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ResourceLocation biomeId = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        ResourceLocation provinceId = ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                "default"
        );

        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.NORMAL),
                scannerWithLoaded(iron),
                policyService,
                biomeId,
                provinceId,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.NONE, plan.skipReason());
        assertEquals(ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "tiny_vertical_mine_entrance"), plan.anchorType());
        assertEquals(iron, plan.resource());
        assertEquals(Optional.of(biomeId), plan.biomeId());
        assertEquals(Optional.of(provinceId), plan.provinceId());
        assertTrue(plan.chamberMetadata().isPresent());
    }

    @Test
    void nullOreLoadPlanIsRejectedSafely() {
        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                null,
                scannerWithNothingLoaded(),
                policyService,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.NULL_ORE_LOAD_PLAN, plan.skipReason());
    }

    @Test
    void invalidNonBlockResourceCannotBecomeOreLoadPlan() {
        ResourceRef ironItem = ResourceRef.item("minecraft", "iron_ingot");

        assertThrows(IllegalArgumentException.class, () -> oreLoadPlan(ironItem, SiteQuality.NORMAL));
    }

    @Test
    void unloadedResourceIsRejectedSafely() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.NORMAL),
                scannerWithNothingLoaded(),
                policyService,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void strictExclusionsRemainEnforced() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                oreLoadPlan(tin, SiteQuality.NORMAL),
                scannerWithLoaded(tin),
                policyService,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
    }

    @Test
    void resourceDeniedByPolicyIsRejectedSafely() {
        ResourceRef dirt = ResourceRef.block("minecraft", "dirt");

        OreLoadChamberPlacementPlan plan = planner.planChamberPlacement(
                oreLoadPlan(dirt, SiteQuality.NORMAL),
                scannerWithLoaded(dirt),
                policyService,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(OreLoadChamberPlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void siteQualityProducesDeterministicChamberMetadata() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        OreLoadChamberPlacementPlan first = planner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.MOTHERLODE),
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );
        OreLoadChamberPlacementPlan second = planner.planChamberPlacement(
                oreLoadPlan(iron, SiteQuality.MOTHERLODE),
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        assertEquals(first.chamberMetadata(), second.chamberMetadata());
        assertEquals(
                OreLoadChamberPlacementPlan.ChamberShape.MOTHERLODE_CORE,
                first.chamberMetadata().orElseThrow().shape()
        );
        assertEquals(5, first.chamberMetadata().orElseThrow().horizontalRadius());
        assertEquals(3, first.chamberMetadata().orElseThrow().verticalHalfSize());
    }

    @Test
    void v8AnchorPlanningBehaviorRemainsUnchanged() {
        ExpeditionAnchorPlacementPlanner anchorPlanner = ExpeditionAnchorPlacementPlanner.defaults();

        ExpeditionAnchorPlacementPlan defaultPlan = anchorPlanner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                BlockPos.ZERO,
                SiteQuality.NORMAL
        );
        ExpeditionAnchorPlacementPlan enabledPlan = anchorPlanner.planAnchorPlacement(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                BlockPos.ZERO,
                SiteQuality.NORMAL,
                ENABLED_GATES
        );

        assertFalse(defaultPlan.placementAllowed());
        assertEquals(ExpeditionAnchorPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, defaultPlan.skipReason());
        assertTrue(enabledPlan.placementAllowed());
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
    void scaffoldBootstrapRemainsScaffoldOnlyWithChamberPlanningMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertTrue(registration.oreLoadChamberPlacementPlanningReady());
        assertEquals(IoeWorldgenFeatureKeys.oreLoadChamberFeatureKeys(), registration.oreLoadChamberPlacementPlanKeys());
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
    }

    @Test
    void oreLoadChamberPlanningDoesNotIntroduceLegacyNamespaces() {
        OreLoadChamberPlacementRules rules = OreLoadChamberPlacementRules.defaults();

        assertEquals(IoeWorldgenFeatureKeys.oreLoadChamberFeatureKeys(), rules.knownChamberKeys());
        for (ResourceLocation chamberKey : rules.knownChamberKeys()) {
            assertEquals(ImmersiveOreExpeditionMod.MODID, chamberKey.getNamespace());
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
