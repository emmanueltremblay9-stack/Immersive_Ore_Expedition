package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceBiomeRuleTest {
    private static final ProvinceId PROVINCE = ProvinceId.parse("temperate_iron");
    private static final ResourceLocation PLAINS = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
    private static final ResourceLocation DESERT = ResourceLocation.fromNamespaceAndPath("minecraft", "desert");
    private static final ResourceLocation BADLANDS = ResourceLocation.fromNamespaceAndPath("minecraft", "badlands");
    private static final ResourceLocation IS_OVERWORLD = ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld");
    private static final ResourceLocation IS_HOT = ResourceLocation.fromNamespaceAndPath("minecraft", "is_hot");
    private static final ResourceLocation NO_SURFACE_CLUES =
            ResourceLocation.fromNamespaceAndPath("immersive_ore_expedition", "no_surface_clues");

    @Test
    void allowBiomeIdsAndTagsCanMatchProvince() {
        ProvinceBiomeRule rule = new ProvinceBiomeRule(
                PROVINCE,
                List.of(PLAINS),
                List.of(IS_OVERWORLD),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        assertTrue(rule.matches(PLAINS, Set.of()));
        assertTrue(rule.matches(ResourceLocation.fromNamespaceAndPath("minecraft", "forest"), Set.of(IS_OVERWORLD)));
    }

    @Test
    void denyAndExcludeListsRejectBeforeAllowLists() {
        ProvinceBiomeRule excluded = new ProvinceBiomeRule(
                PROVINCE,
                List.of(PLAINS, BADLANDS),
                List.of(IS_OVERWORLD),
                List.of(DESERT),
                List.of(IS_HOT),
                List.of(BADLANDS),
                List.of(NO_SURFACE_CLUES)
        );

        assertFalse(excluded.matches(DESERT, Set.of(IS_OVERWORLD)));
        assertFalse(excluded.matches(ResourceLocation.fromNamespaceAndPath("minecraft", "savanna"), Set.of(IS_HOT)));
        assertFalse(excluded.matches(BADLANDS, Set.of(IS_OVERWORLD)));
        assertFalse(excluded.matches(PLAINS, Set.of(IS_OVERWORLD, NO_SURFACE_CLUES)));
    }

    @Test
    void unmatchedBiomeIsRejectedWhenAllowListIsConfigured() {
        ProvinceBiomeRule rule = new ProvinceBiomeRule(
                PROVINCE,
                List.of(PLAINS),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        ProvinceMatchDiagnostic diagnostic = rule.diagnose(DESERT, Set.of());

        assertFalse(diagnostic.matched());
        assertTrue(diagnostic.summary().contains("temperate_iron"));
        assertTrue(diagnostic.reasons().getFirst().contains("did not match"));
    }

    @Test
    void emptyAllowListsAcceptBiomeUnlessDeniedOrExcluded() {
        ProvinceBiomeRule rule = new ProvinceBiomeRule(
                PROVINCE,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        assertTrue(rule.matches(DESERT, Set.of()));
    }
}
