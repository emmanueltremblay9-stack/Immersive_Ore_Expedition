package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SubLavaGeodeGeneratorTest {
    private final SubLavaGeodeGenerator generator = new SubLavaGeodeGenerator();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void plansQuartzGeodeOnlyBelowValidNetherLavaLake() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                scanner,
                policy
        );

        assertTrue(plan.isPresent());
        assertEquals(quartz, plan.get().quartzResource());
        assertEquals(16, plan.get().blocksBelowLava());
        assertTrue(plan.get().requireSafeCrust());
        assertFalse(plan.get().randomNetherGeode());
        assertFalse(plan.get().hasAncientDebrisHeart());
    }

    @Test
    void addsAncientDebrisHeartOnlyWhenLoadedAndStillExtremelyRare() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        ResourceRef debris = ResourceRef.block("minecraft", "ancient_debris");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz, debris));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.of(debris),
                24,
                scanner,
                policy
        );

        assertTrue(plan.isPresent());
        assertEquals(debris, plan.get().ancientDebrisHeart().orElseThrow());
        assertEquals(0.005D, plan.get().ancientDebrisMotherlodeChance());
    }

    @Test
    void recordsSkippedAncientDebrisHeartWhenOptionalResourceIsMissing() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        ResourceRef debris = ResourceRef.block("minecraft", "ancient_debris");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.of(debris),
                24,
                scanner,
                policy
        );

        assertTrue(plan.isPresent());
        assertTrue(plan.get().ancientDebrisHeart().isEmpty());
        assertEquals(1, plan.get().skippedResources().size());
        assertTrue(plan.get().rejectedResources().isEmpty());
        assertFalse(plan.get().skippedResources().getFirst().reason().isBlank());
    }

    @Test
    void skipsMissingQuartzInsteadOfPlanningFallbackResource() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                scanner(Set.of()),
                policy
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void quartzResourceConfigGateCanDisablePrimaryQuartzPlanning() {
        ResourcePolicyDecision acceptedQuartz = ResourcePolicyDecision.use("loaded");
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");

        assertTrue(SubLavaGeodeGenerator.canUseQuartzResource(quartz, acceptedQuartz, true));
        assertFalse(SubLavaGeodeGenerator.canUseQuartzResource(quartz, acceptedQuartz, false));
        assertFalse(SubLavaGeodeGenerator.canUseQuartzResource(diamond, acceptedQuartz, true));
        assertFalse(SubLavaGeodeGenerator.canUseQuartzResource(quartz, ResourcePolicyDecision.reject("missing"), true));
    }

    @Test
    void rejectsApprovedLoadedNonQuartzResourceAsPrimaryGeodeResource() {
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");
        NetherGeodesTestScanner scanner = scanner(Set.of(diamond));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                diamond,
                Optional.empty(),
                16,
                scanner,
                policy
        );

        assertTrue(plan.isEmpty());
    }

    @Test
    void ignoresApprovedLoadedNonDebrisResourceAsAncientDebrisHeart() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz, diamond));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.of(diamond),
                24,
                scanner,
                policy
        );

        assertTrue(plan.isPresent());
        assertTrue(plan.get().ancientDebrisHeart().isEmpty());
    }

    @Test
    void randomGeodeAllowanceDoesNotDisableAnchoredPlanning() {
        assertTrue(SubLavaGeodeGenerator.canPlanAnchoredGeode(Level.NETHER, true, true, true));
        assertFalse(SubLavaGeodeGenerator.canPlanAnchoredGeode(Level.OVERWORLD, true, true, true));
        assertFalse(SubLavaGeodeGenerator.canPlanAnchoredGeode(Level.NETHER, false, true, true));
        assertFalse(SubLavaGeodeGenerator.canPlanAnchoredGeode(Level.NETHER, true, false, true));
    }

    @Test
    void planBelowLakeKeepsAnchoredPlansNonRandom() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz));

        Optional<SubLavaGeodePlan> plan = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                scanner,
                policy
        );

        assertTrue(plan.isPresent());
        assertFalse(plan.get().randomNetherGeode());
    }

    @Test
    void rejectsNonNetherOrInvalidDepthPlacement() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        NetherGeodesTestScanner scanner = scanner(Set.of(quartz));

        Optional<SubLavaGeodePlan> overworld = generator.planBelowLake(
                anchor(Level.OVERWORLD),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                scanner,
                policy
        );
        Optional<SubLavaGeodePlan> tooShallow = generator.planBelowLake(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                7,
                scanner,
                policy
        );

        assertTrue(overworld.isEmpty());
        assertTrue(tooShallow.isEmpty());
    }

    @Test
    void rejectsNonFiniteAncientDebrisMotherlodeChance() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                true,
                false,
                Double.NaN,
                List.of(),
                List.of()
        ));

        assertEquals("ancientDebrisMotherlodeChance must stay extremely rare", error.getMessage());
    }

    @Test
    void rejectsDirectPlansOutsideNether() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");

        assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.OVERWORLD),
                validLake(),
                quartz,
                Optional.empty(),
                16,
                true,
                false,
                0.005D,
                List.of(),
                List.of()
        ));
    }

    @Test
    void rejectsDirectPlansWithWeakLavaLakeSamples() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        LavaLakeAnchorSample weakLake = new LavaLakeAnchorSample(Level.NETHER, BlockPos.ZERO, 64, 0.59D, 4);

        assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.NETHER),
                weakLake,
                quartz,
                Optional.empty(),
                16,
                true,
                false,
                0.005D,
                List.of(),
                List.of()
        ));
    }

    @Test
    void rejectsDirectPlansWithNonQuartzPrimaryResource() {
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");

        assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.NETHER),
                validLake(),
                diamond,
                Optional.empty(),
                16,
                true,
                false,
                0.005D,
                List.of(),
                List.of()
        ));
    }

    @Test
    void rejectsDirectPlansWithNonDebrisHeartResource() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");
        ResourceRef diamond = ResourceRef.block("minecraft", "diamond_ore");

        assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.of(diamond),
                16,
                true,
                false,
                0.005D,
                List.of(),
                List.of()
        ));
    }

    @Test
    void rejectsDirectPlansOutsideConfiguredDepthBand() {
        ResourceRef quartz = ResourceRef.block("minecraft", "nether_quartz_ore");

        assertThrows(IllegalArgumentException.class, () -> new SubLavaGeodePlan(
                anchor(Level.NETHER),
                validLake(),
                quartz,
                Optional.empty(),
                7,
                true,
                false,
                0.005D,
                List.of(),
                List.of()
        ));
    }

    private static ExpeditionAnchorRef anchor(net.minecraft.resources.ResourceKey<Level> dimension) {
        return new ExpeditionAnchorRef(dimension, new BlockPos(0, 32, 0), "giant_lava_lake", SiteQuality.NORMAL);
    }

    private static LavaLakeAnchorSample validLake() {
        return new LavaLakeAnchorSample(Level.NETHER, new BlockPos(0, 64, 0), 64, 0.75D, 6);
    }

    private static NetherGeodesTestScanner scanner(Set<ResourceRef> resources) {
        return new NetherGeodesTestScanner(resources);
    }
}
