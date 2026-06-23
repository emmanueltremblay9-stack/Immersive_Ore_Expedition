package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourcePolicyServiceTest {
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void approvesVanillaAndConfiguredCompatResources() {
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "deepslate_iron_ore")));
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ore_bauxite")));
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("ae2", "damaged_budding_quartz")));
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("draconicevolution", "draconium_ore")));
    }

    @Test
    void rejectsExplicitlyExcludedResources() {
        assertFalse(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "tin_ore")));
        assertFalse(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("forestry", "apatite_ore")));
        assertFalse(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "platinum_block")));
        assertFalse(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "black_quartz_ore")));
    }

    @Test
    void skipsApprovedMissingResourcesWithoutSubstitution() {
        ResourcePolicyDecision decision = policy.evaluate(
                ResourceRef.block("minecraft", "diamond_ore"),
                scannerWithNoRuntimeResources()
        );

        assertTrue(decision.shouldSkip());
        assertFalse(decision.shouldUse());
    }

    @Test
    void usesApprovedLoadedResources() {
        ResourcePolicyDecision decision = policy.evaluate(
                ResourceRef.block("minecraft", "diamond_ore"),
                scannerWithOnlyDiamondOre()
        );

        assertTrue(decision.shouldUse());
    }

    private static LoadedResourceScanner scannerWithNoRuntimeResources() {
        return new TestScanner(false);
    }

    private static LoadedResourceScanner scannerWithOnlyDiamondOre() {
        return new TestScanner(true);
    }

    private record TestScanner(boolean diamondOreLoaded) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return diamondOreLoaded && id.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "diamond_ore"));
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return false;
        }
    }
}
