package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OreSuppressionPolicyTest {
    @Test
    void defaultPolicyDrasticallyReducesRandomOreCounts() {
        OreSuppressionPolicy policy = new OreSuppressionPolicy();

        assertEquals(3, policy.scaledRandomOreCount(100));
        assertEquals(0, policy.scaledRandomOreCount(1));
        assertTrue(policy.requiresExpeditionAnchorForMajorLoad());
        assertTrue(policy.allowsTinyScrapOutsideProvinces());
    }
}
