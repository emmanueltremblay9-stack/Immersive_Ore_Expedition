package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreSuppressionPolicyTest {
    @Test
    void defaultPolicyEliminatesRandomOreCounts() {
        OreSuppressionPolicy policy = new OreSuppressionPolicy();

        assertEquals(0, policy.scaledRandomOreCount(100));
        assertEquals(0, policy.scaledRandomOreCount(1));
        assertTrue(policy.requiresExpeditionAnchorForMajorLoad());
        assertFalse(policy.allowsTinyScrapOutsideProvinces());
    }
}
