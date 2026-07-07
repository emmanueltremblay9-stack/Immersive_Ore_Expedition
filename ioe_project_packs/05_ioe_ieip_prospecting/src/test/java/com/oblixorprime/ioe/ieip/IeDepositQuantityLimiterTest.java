package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IeDepositQuantityLimiterTest {
    private final IeDepositQuantityLimiter limiter = new IeDepositQuantityLimiter();

    @Test
    void plansDefaultIeDepositQuantityReduction() {
        Optional<DepositQuantityLimitPlan> normal = limiter.planQuantityLimit("ie:bauxite", 100, false, scannerWithIeLoaded());
        Optional<DepositQuantityLimitPlan> hard = limiter.planQuantityLimit("ie:bauxite", 100, true, scannerWithIeLoaded());

        assertTrue(normal.isPresent());
        assertEquals(10, normal.get().scaledQuantity());
        assertEquals(0.10D, normal.get().multiplier());
        assertTrue(hard.isPresent());
        assertEquals(5, hard.get().scaledQuantity());
        assertEquals(0.05D, hard.get().multiplier());
    }

    @Test
    void keepsTinyPositiveDepositsAtOneWhenMultiplierWouldFloorToZero() {
        Optional<DepositQuantityLimitPlan> plan = limiter.planQuantityLimit("ie:nickel", 3, false, scannerWithIeLoaded());

        assertTrue(plan.isPresent());
        assertEquals(1, plan.get().scaledQuantity());
    }

    @Test
    void skipsQuantityPlanWhenImmersiveEngineeringIsMissing() {
        ProspectingTestScanner scanner = new ProspectingTestScanner(Set.of(), Set.of());

        Optional<DepositQuantityLimitPlan> plan = limiter.planQuantityLimit("ie:bauxite", 100, false, scanner);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectsNonFiniteMultipliersInsteadOfProducingPositiveQuantities() {
        assertEquals(0, IeDepositQuantityLimiter.scaleDepositQuantity(100, Double.NaN));
        assertEquals(0, IeDepositQuantityLimiter.scaleDepositQuantity(100, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new DepositQuantityLimitPlan(
                "ie:bauxite",
                100,
                1,
                Double.NaN,
                false
        ));
    }

    private static ProspectingTestScanner scannerWithIeLoaded() {
        return new ProspectingTestScanner(Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING), Set.<ResourceRef>of());
    }
}
