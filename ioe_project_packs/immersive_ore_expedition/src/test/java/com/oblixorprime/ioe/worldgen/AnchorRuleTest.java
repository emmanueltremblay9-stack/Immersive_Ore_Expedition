package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnchorRuleTest {
    @Test
    void configValuesNormalizeInvertedDistanceRangeInsteadOfCrashingPlanning() {
        AnchorRule rule = AnchorRule.fromConfigValues(96, 16, true);

        assertEquals(96, rule.minDistance());
        assertEquals(96, rule.maxDistance());
        assertTrue(rule.requireTunnelConnection());
        assertFalse(rule.isDistanceAllowed(95));
        assertTrue(rule.isDistanceAllowed(96));
    }
}
