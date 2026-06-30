package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceResourcePolicyResolverTest {
    private static final ProvinceId DEFAULT_PROVINCE = ProvinceId.parse("immersive_ore_expedition:default");
    private static final ResourceRef IRON = ResourceRef.block("minecraft", "iron_ore");

    @Test
    void emptyRulesLeaveResourceAllowedForExistingPolicyChecks() {
        ProvinceResourcePolicyResolver resolver = ProvinceResourcePolicyResolver.empty();

        ResourcePolicyDecision decision = resolver.evaluate(DEFAULT_PROVINCE, IRON);

        assertEquals(0, resolver.ruleCount());
        assertTrue(decision.shouldUse());
        assertTrue(decision.reason().contains("No configured province resource policy rule"));
    }

    @Test
    void allowDenyAndExcludeRulesMapToDeterministicDecisions() {
        ProvinceResourcePolicyResolver allow = ProvinceResourcePolicyResolver.parse(
                List.of("immersive_ore_expedition:default|minecraft:iron_ore|allow"),
                false
        );
        ProvinceResourcePolicyResolver deny = ProvinceResourcePolicyResolver.parse(
                List.of("immersive_ore_expedition:default|minecraft:iron_ore|deny"),
                false
        );
        ProvinceResourcePolicyResolver exclude = ProvinceResourcePolicyResolver.parse(
                List.of("immersive_ore_expedition:default|minecraft:iron_ore|exclude"),
                false
        );

        assertTrue(allow.evaluate(DEFAULT_PROVINCE, IRON).shouldUse());
        assertEquals(ResourcePolicyDecision.Action.REJECT, deny.evaluate(DEFAULT_PROVINCE, IRON).action());
        assertEquals(ResourcePolicyDecision.Action.REJECT, exclude.evaluate(DEFAULT_PROVINCE, IRON).action());
        assertTrue(exclude.evaluate(DEFAULT_PROVINCE, IRON).reason().contains("excludes"));
    }

    @Test
    void unqualifiedProvinceIdsResolveToConsolidatedNamespace() {
        ProvinceResourcePolicyResolver resolver = ProvinceResourcePolicyResolver.parse(
                List.of("default|minecraft:iron_ore|deny"),
                false
        );

        ResourcePolicyDecision decision = resolver.evaluate(DEFAULT_PROVINCE, IRON);

        assertEquals(1, resolver.ruleCount());
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertTrue(decision.reason().contains("immersive_ore_expedition:default"));
    }

    @Test
    void oldSplitNamespacesAreRejectedByDefaultForResourceRules() {
        ProvinceResourcePolicyResolver resolver = ProvinceResourcePolicyResolver.parse(
                List.of("ioe_core:default|minecraft:iron_ore|deny"),
                false
        );

        ResourcePolicyDecision decision = resolver.evaluate(DEFAULT_PROVINCE, IRON);

        assertEquals(0, resolver.ruleCount());
        assertTrue(decision.shouldUse());
    }

    @Test
    void legacyProvinceNamespacesRequireExplicitOptInForResourceRules() {
        ProvinceResourcePolicyResolver resolver = ProvinceResourcePolicyResolver.parse(
                List.of("ioe_core:default|minecraft:iron_ore|deny"),
                true
        );

        ResourcePolicyDecision decision = resolver.evaluate(ProvinceId.legacy("ioe_core", "default"), IRON);

        assertEquals(1, resolver.ruleCount());
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
    }

    @Test
    void malformedRulesAreIgnored() {
        ProvinceResourcePolicyResolver resolver = ProvinceResourcePolicyResolver.parse(
                Arrays.asList(
                        null,
                        "",
                        "default",
                        "default|minecraft:iron_ore",
                        "default|minecraft:iron_ore|maybe",
                        "default|iron_ore|deny",
                        "minecraft:iron_ore|deny"
                ),
                false
        );

        assertEquals(0, resolver.ruleCount());
        assertTrue(resolver.evaluate(DEFAULT_PROVINCE, IRON).shouldUse());
    }

    @Test
    void duplicateConflictingRulesUseFirstValidMatch() {
        ProvinceResourcePolicyResolver firstDeny = ProvinceResourcePolicyResolver.parse(
                List.of(
                        "default|minecraft:iron_ore|deny",
                        "default|minecraft:iron_ore|allow"
                ),
                false
        );
        ProvinceResourcePolicyResolver firstAllow = ProvinceResourcePolicyResolver.parse(
                List.of(
                        "default|minecraft:iron_ore|allow",
                        "default|minecraft:iron_ore|deny"
                ),
                false
        );

        assertEquals(ResourcePolicyDecision.Action.REJECT, firstDeny.evaluate(DEFAULT_PROVINCE, IRON).action());
        assertTrue(firstAllow.evaluate(DEFAULT_PROVINCE, IRON).shouldUse());
    }
}
