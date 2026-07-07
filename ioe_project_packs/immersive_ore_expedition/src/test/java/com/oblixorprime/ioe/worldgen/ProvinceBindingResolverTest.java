package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ProvinceBindingResolverTest {
    private static final ResourceLocation PLAINS = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
    private static final ResourceLocation DESERT = ResourceLocation.fromNamespaceAndPath("minecraft", "desert");
    private static final ResourceLocation MEADOW = ResourceLocation.fromNamespaceAndPath("minecraft", "meadow");

    @Test
    void defaultProvinceIsUsedWhenNoBindingMatches() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of("minecraft:desert=immersive_ore_expedition:arid"),
                false
        );

        assertEquals("immersive_ore_expedition:default", resolver.resolve(PLAINS).toString());
        assertEquals("immersive_ore_expedition:default", resolver.resolve(null).toString());
    }

    @Test
    void exactBiomeBindingResolvesConfiguredProvince() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of("minecraft:plains=immersive_ore_expedition:temperate_iron"),
                false
        );

        assertEquals("immersive_ore_expedition:temperate_iron", resolver.resolve(PLAINS).toString());
    }

    @Test
    void malformedBindingsAreIgnoredDeterministically() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of(
                        "not a binding",
                        "=immersive_ore_expedition:missing_biome",
                        "minecraft:plains=",
                        "minecraft:plains=example:wrong_namespace",
                        "minecraft:plains=immersive_ore_expedition:temperate_iron"
                ),
                false
        );

        assertEquals(1, resolver.bindingCount());
        assertEquals("immersive_ore_expedition:temperate_iron", resolver.resolve(PLAINS).toString());
    }

    @Test
    void unqualifiedProvinceIdsResolveToConsolidatedNamespace() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "default",
                List.of("minecraft:plains=temperate_iron"),
                false
        );

        assertEquals(ProvinceId.CONSOLIDATED_NAMESPACE, resolver.defaultProvince().namespace());
        assertEquals("immersive_ore_expedition:temperate_iron", resolver.resolve(PLAINS).toString());
    }

    @Test
    void oldSplitProvinceNamespacesAreRejectedByDefault() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "ioe_core:default",
                List.of("minecraft:plains=ioe_core:temperate_iron"),
                false
        );

        assertEquals("immersive_ore_expedition:default", resolver.defaultProvince().toString());
        assertEquals(0, resolver.bindingCount());
        assertEquals("immersive_ore_expedition:default", resolver.resolve(PLAINS).toString());
    }

    @Test
    void legacyProvinceNamespacesRequireExplicitOptIn() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of("minecraft:plains=ioe_core:temperate_iron"),
                true
        );

        assertEquals("ioe_core:temperate_iron", resolver.resolve(PLAINS).toString());
    }

    @Test
    void exactBindingsWinBeforeNamespaceBindings() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of(
                        "minecraft:*=immersive_ore_expedition:all_minecraft",
                        "minecraft:plains=immersive_ore_expedition:temperate_iron"
                ),
                false
        );

        assertEquals("immersive_ore_expedition:temperate_iron", resolver.resolve(PLAINS).toString());
        assertEquals("immersive_ore_expedition:all_minecraft", resolver.resolve(DESERT).toString());
    }

    @Test
    void firstMatchingRuleWinsWithinSameSpecificity() {
        ProvinceBindingResolver resolver = ProvinceBindingResolver.parse(
                "immersive_ore_expedition:default",
                List.of(
                        "minecraft:plains=immersive_ore_expedition:first",
                        "minecraft:plains=immersive_ore_expedition:second",
                        "minecraft:*=immersive_ore_expedition:namespace_first",
                        "minecraft:*=immersive_ore_expedition:namespace_second"
                ),
                false
        );

        assertEquals("immersive_ore_expedition:first", resolver.resolve(PLAINS).toString());
        assertEquals("immersive_ore_expedition:namespace_first", resolver.resolve(MEADOW).toString());
    }
}
