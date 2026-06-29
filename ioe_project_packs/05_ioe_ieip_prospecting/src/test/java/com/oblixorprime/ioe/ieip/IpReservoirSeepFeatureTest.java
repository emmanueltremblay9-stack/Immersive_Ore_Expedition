package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IpReservoirSeepFeatureTest {
    private final IpReservoirSeepFeature feature = new IpReservoirSeepFeature();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void createsPocketLakeForLoadedReservoirFluid() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");
        IpReservoirRef reservoir = new IpReservoirRef("ip:crude_oil", crudeOil, false);

        Optional<ReservoirSeepPlan> plan = feature.planSeepClue(reservoir, scanner(Set.of(crudeOil)), policy);

        assertTrue(plan.isPresent());
        assertEquals(crudeOil, plan.get().clueFluid());
        assertEquals(12, plan.get().maxSurfaceFluidBlocks());
        assertTrue(plan.get().pocketLake());
        assertFalse(plan.get().vent());
        assertFalse(plan.get().rendersFullReservoir());
    }

    @Test
    void createsVentForGasLikeReservoirFluid() {
        ResourceRef naturalGas = ResourceRef.fluid("immersivepetroleum", "natural_gas");
        IpReservoirRef reservoir = new IpReservoirRef("ip:natural_gas", naturalGas, true);

        Optional<ReservoirSeepPlan> plan = feature.planSeepClue(reservoir, scanner(Set.of(naturalGas)), policy);

        assertTrue(plan.isPresent());
        assertFalse(plan.get().pocketLake());
        assertTrue(plan.get().vent());
    }

    @Test
    void skipsMissingFluidInsteadOfPlanningFallbackSeep() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");
        IpReservoirRef reservoir = new IpReservoirRef("ip:crude_oil", crudeOil, false);

        Optional<ReservoirSeepPlan> plan = feature.planSeepClue(reservoir, scanner(Set.of()), policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void skipsWhenImmersivePetroleumIsMissing() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");
        IpReservoirRef reservoir = new IpReservoirRef("ip:crude_oil", crudeOil, false);
        ProspectingTestScanner scanner = new ProspectingTestScanner(Set.of(), Set.of(crudeOil));

        Optional<ReservoirSeepPlan> plan = feature.planSeepClue(reservoir, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsNonFluidSeepResources() {
        ResourceRef leadOre = ResourceRef.block("immersiveengineering", "lead_ore");

        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef("ip:not_fluid", leadOre, false));
    }

    @Test
    void rejectsLoadedNonPetroleumFluidInsteadOfPlanningFakeSeep() {
        ResourceRef fakeFluid = ResourceRef.fluid("example", "liquid_gold");

        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef("ip:fake", fakeFluid, false));
    }

    @Test
    void rejectsFluidTagsInsteadOfPlanningAmbiguousSeeps() {
        ResourceRef crudeOilTag = ResourceRef.fluidTag("immersivepetroleum", "crude_oil");

        assertThrows(IllegalArgumentException.class, () -> new IpReservoirRef("ip:crude_oil", crudeOilTag, false));
    }

    @Test
    void rejectsNonFluidSeepPlanResources() {
        ResourceRef oilBlock = ResourceRef.block("immersivepetroleum", "crude_oil");

        assertThrows(IllegalArgumentException.class, () -> new ReservoirSeepPlan(
                "ip:crude_oil",
                oilBlock,
                12,
                true,
                false,
                false
        ));
    }

    @Test
    void rejectsFullReservoirRenderPlans() {
        ResourceRef crudeOil = ResourceRef.fluid("immersivepetroleum", "crude_oil");

        assertThrows(IllegalArgumentException.class, () -> new ReservoirSeepPlan(
                "ip:crude_oil",
                crudeOil,
                12,
                true,
                false,
                true
        ));
    }

    private static ProspectingTestScanner scanner(Set<ResourceRef> resources) {
        return new ProspectingTestScanner(Set.of(ProspectingCompatGates.IMMERSIVE_PETROLEUM), resources);
    }
}
