package com.oblixorprime.ioe.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceResourcePolicyTest {
    @Test
    void defaultPolicyAllowsKnownResourceCategories() {
        ProvinceResourcePolicy policy = ProvinceResourcePolicy.defaults();

        assertTrue(policy.evaluate(ResourceRef.block("minecraft", "diamond_ore")).shouldUse());
        assertTrue(policy.evaluate(ResourceRef.block("immersiveengineering", "ore_bauxite")).shouldUse());
        assertTrue(policy.evaluate(ResourceRef.blockTag("c", "ores/iron")).shouldUse());
    }

    @Test
    void strictExclusionsOverrideAllowedCategories() {
        ProvinceResourcePolicy policy = ProvinceResourcePolicy.defaults();

        assertFalse(policy.evaluate(ResourceRef.block("minecraft", "tin_ore")).shouldUse());
        assertFalse(policy.evaluate(ResourceRef.block("forestry", "copper_ore")).shouldUse());
        assertFalse(policy.evaluate(ResourceRef.block("minecraft", "black_quartz_ore")).shouldUse());
    }

    @Test
    void categoryDenyListOverridesAllowList() {
        ProvinceResourcePolicy policy = new ProvinceResourcePolicy(
                Set.of("vanilla"),
                Set.of("vanilla"),
                Set.of(),
                true
        );

        ResourcePolicyDecision decision = policy.evaluate(ResourceRef.block("minecraft", "diamond_ore"));

        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertTrue(decision.reason().contains("denied"));
    }

    @Test
    void unknownCategoriesAreRejectedWhenAllowListIsNotEmpty() {
        ProvinceResourcePolicy policy = ProvinceResourcePolicy.defaults();

        ResourcePolicyDecision decision = policy.evaluate(ResourceRef.block("example", "iron_ore"));

        assertEquals("unknown", ProvinceResourcePolicy.categoryFor(ResourceRef.block("example", "iron_ore")));
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
    }

    @Test
    void diagnosticLineIncludesResourceCategoryAndDecision() {
        ProvinceResourcePolicy policy = ProvinceResourcePolicy.defaults();
        ResourceRef resource = ResourceRef.block("minecraft", "diamond_ore");
        ResourcePolicyDecision decision = policy.evaluate(resource);

        String diagnostic = policy.diagnosticLine(resource, decision);

        assertTrue(diagnostic.contains("resource=minecraft:diamond_ore"));
        assertTrue(diagnostic.contains("category=vanilla"));
        assertTrue(diagnostic.contains("action=USE"));
    }
}
