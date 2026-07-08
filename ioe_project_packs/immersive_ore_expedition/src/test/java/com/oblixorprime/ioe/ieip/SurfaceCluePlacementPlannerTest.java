package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.worldgen.IoeWorldgenBootstrap;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import com.oblixorprime.ioe.worldgen.IoeWorldgenRegistration;
import com.oblixorprime.ioe.worldgen.WorldgenBiomeProvinceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SurfaceCluePlacementPlannerTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, true, false);
    private static final IoeWorldgenPlacementGates DISABLED_GATES =
            IoeWorldgenPlacementGates.disabled();
    private static final BlockPos ORIGIN = new BlockPos(32, 70, 48);
    private static final ResourceLocation ANCHOR_TYPE =
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "tiny_vertical_mine_entrance");

    private final ResourcePolicyService policyService = new ResourcePolicyService();
    private final IeMineralOutcropFeature outcropFeature = new IeMineralOutcropFeature();
    private final IpReservoirSeepFeature seepFeature = new IpReservoirSeepFeature();

    @Test
    void defaultConfigGatesProduceNoOpSurfaceCluePlan() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                ORIGIN,
                3
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_RUNTIME_DISABLED, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED, plan.skipReason());
    }

    @Test
    void enabledRuntimeGateCanPlanIeMineralClue() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                ORIGIN,
                3,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP, plan.sourceSystem());
        assertEquals(SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP, plan.clueType());
        assertEquals(lead, plan.clueResource());
        assertEquals(ORIGIN, plan.origin());
        assertEquals(Optional.of(ANCHOR_TYPE), plan.anchorType());
        assertEquals(3, plan.clueMetadata().orElseThrow().clueSize());
        assertFalse(plan.clueMetadata().orElseThrow().rendersFullUndergroundDeposit());
    }

    @Test
    void ieMineralClueUsesFirstLoadedAllowedDepositResource() {
        ResourceRef missingSilver = ResourceRef.block("immersiveengineering", "silver_ore");
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:mixed", List.of(missingSilver, lead)),
                ORIGIN,
                3,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(lead, plan.clueResource());
    }

    @Test
    void enabledRuntimeGateCanPlanIpReservoirSeepClue() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_PETROLEUM),
                Set.of(crudeOil)
        );

        SurfaceCluePlacementPlan plan = planner.planReservoirSeepClue(
                new IpReservoirRef("ip:crude_oil", crudeOil, false),
                ORIGIN,
                null,
                null,
                ANCHOR_TYPE,
                ENABLED_GATES
        );

        assertTrue(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.SourceSystem.IP_RESERVOIR_SEEP, plan.sourceSystem());
        assertEquals(SurfaceCluePlacementPlan.ClueType.IP_POCKET_LAKE, plan.clueType());
        assertEquals(crudeOil, plan.clueResource());
        assertTrue(plan.clueMetadata().orElseThrow().pocketLake());
        assertFalse(plan.clueMetadata().orElseThrow().gasVent());
        assertFalse(plan.clueMetadata().orElseThrow().rendersFullUndergroundDeposit());
    }

    @Test
    void optionalIeIpAbsenceIsHandledSafely() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(Set.of(), Set.of(lead));

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                ORIGIN,
                3,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_OPTIONAL_MOD_ABSENT, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.OPTIONAL_MOD_ABSENT, plan.skipReason());
    }

    @Test
    void unloadedResourceIsSkippedSafely() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of()
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                ORIGIN,
                3,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_NOT_LOADED, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.RESOURCE_NOT_LOADED, plan.skipReason());
    }

    @Test
    void policyDeniedResourceIsSkippedSafely() {
        ResourceRef dirt = ResourceRef.block("minecraft", "dirt");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(dirt)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:dirt", List.of(dirt)),
                ORIGIN,
                3,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_RESOURCE_DENIED, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY, plan.skipReason());
    }

    @Test
    void strictExclusionsWin() {
        ResourceRef tin = ResourceRef.block("immersiveengineering", "tin_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(tin)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:tin", List.of(tin)),
                ORIGIN,
                3,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_STRICT_EXCLUSION, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.STRICT_EXCLUSION, plan.skipReason());
    }

    @Test
    void nullOriginIsRejectedSafely() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                null,
                3,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.NULL_ORIGIN, plan.skipReason());
    }

    @Test
    void nullResourceIsRejectedSafely() {
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of()
        );

        SurfaceCluePlacementPlan plan = planner.planSurfaceClue(
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP,
                null,
                ORIGIN,
                null,
                null,
                null,
                metadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.NULL_RESOURCE, plan.skipReason());
    }

    @Test
    void invalidClueTypeIsRejectedSafely() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planSurfaceClue(
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                SurfaceCluePlacementPlan.ClueType.IP_SEEP,
                lead,
                ORIGIN,
                null,
                null,
                null,
                metadata(),
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_INVALID_CLUE_TYPE, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.INVALID_CLUE_TYPE, plan.skipReason());
    }

    @Test
    void nullMetadataIsRejectedSafely() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );

        SurfaceCluePlacementPlan plan = planner.planSurfaceClue(
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP,
                lead,
                ORIGIN,
                null,
                null,
                null,
                null,
                ENABLED_GATES
        );

        assertFalse(plan.placementAllowed());
        assertEquals(SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT, plan.decision());
        assertEquals(SurfaceCluePlacementPlan.SkipReason.INVALID_INPUT, plan.skipReason());
    }

    @Test
    void biomeProvinceMetadataFromV11ContextIsPreserved() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");
        SurfaceCluePlacementPlanner planner = planner(
                Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING),
                Set.of(lead)
        );
        WorldgenBiomeProvinceContext context = WorldgenBiomeProvinceContext.resolved(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "temperate_iron"),
                false
        );

        SurfaceCluePlacementPlan plan = planner.planMineralOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                ORIGIN,
                3,
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
    void existingIeMineralOutcropFeatureBehaviorRemainsUnchanged() {
        ResourceRef lead = ResourceRef.block("immersiveengineering", "lead_ore");

        Optional<MineralOutcropPlan> plan = outcropFeature.planOutcropClue(
                new IeMineralDepositRef("ie:lead", List.of(lead)),
                3,
                new ProspectingTestScanner(Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING), Set.of(lead)),
                policyService
        );

        assertTrue(plan.isPresent());
        assertEquals(lead, plan.get().clueResource());
        assertFalse(plan.get().rendersFullDeposit());
    }

    @Test
    void existingIpReservoirSeepFeatureBehaviorRemainsUnchanged() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");

        Optional<ReservoirSeepPlan> plan = seepFeature.planSeepClue(
                new IpReservoirRef("ip:crude_oil", crudeOil, false),
                new ProspectingTestScanner(Set.of(ProspectingCompatGates.IMMERSIVE_PETROLEUM), Set.of(crudeOil)),
                policyService
        );

        assertTrue(plan.isPresent());
        assertEquals(crudeOil, plan.get().clueFluid());
        assertFalse(plan.get().rendersFullReservoir());
    }

    @Test
    void scaffoldBootstrapRemainsScaffoldOnlyWithSurfaceClueMetadata() {
        IoeWorldgenRegistration registration = IoeWorldgenBootstrap.bootstrap();

        assertFalse(registration.scaffoldOnly());
        assertTrue(registration.runtimePlacementNoOp());
        assertTrue(registration.anchorPlacementPlanningReady());
        assertTrue(registration.oreLoadChamberPlacementPlanningReady());
        assertTrue(registration.randomOreSuppressionPlanningReady());
        assertTrue(registration.liveBiomeProvinceBindingPlanningReady());
        assertTrue(registration.ieIpSurfaceCluePlacementPlanningReady());
        assertTrue(registration.configuredFeaturesRegistered());
        assertTrue(registration.placedFeaturesRegistered());
        assertTrue(registration.biomeModifiersRegistered());
    }

    @Test
    void noLegacyNamespaceIsIntroduced() {
        SurfaceCluePlacementPlan plan = SurfaceCluePlacementPlan.skipped(
                SurfaceCluePlacementPlan.ClueType.IE_MINERAL_OUTCROP,
                SurfaceCluePlacementPlan.SourceSystem.IE_MINERAL_OUTCROP,
                null,
                ORIGIN,
                SurfaceCluePlacementPlan.Decision.SKIP_INVALID_INPUT,
                SurfaceCluePlacementPlan.SkipReason.NULL_RESOURCE,
                null,
                null,
                ANCHOR_TYPE,
                null
        );

        assertEquals(ImmersiveOreExpeditionMod.MODID, plan.anchorType().orElseThrow().getNamespace());
    }

    private SurfaceCluePlacementPlanner planner(Set<String> mods, Set<ResourceRef> resources) {
        ProspectingTestScanner scanner = new ProspectingTestScanner(mods, resources);
        return new SurfaceCluePlacementPlanner(outcropFeature, seepFeature, scanner, policyService);
    }

    private static SurfaceCluePlacementPlan.ClueMetadata metadata() {
        return new SurfaceCluePlacementPlan.ClueMetadata(1.0D, 1, false, false, false);
    }
}
