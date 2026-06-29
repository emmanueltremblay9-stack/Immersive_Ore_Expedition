package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResourcePolicyServiceTest {
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void approvesVanillaAndConfiguredCompatResources() {
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "deepslate_iron_ore")));
        assertTrue(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("minecraft", "ores/iron_ore")));
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
    void rejectsTokenMatchedUnknownResourceNamespaces() {
        ResourceRef fakeIron = ResourceRef.block("example", "iron_ore");

        ResourcePolicyDecision decision = policy.evaluate(fakeIron, scannerWithLoaded(fakeIron));

        assertFalse(policy.isApprovedResource(fakeIron.id()));
        assertFalse(decision.shouldUse());
        assertFalse(decision.shouldSkip());
    }

    @Test
    void acceptsLoadedCommonOreTagsWithoutOpeningUnknownResourceNamespaces() {
        ResourceRef commonIronTag = ResourceRef.blockTag("c", "ores/iron");

        ResourcePolicyDecision decision = policy.evaluate(commonIronTag, scannerWithLoaded(commonIronTag));

        assertTrue(decision.shouldUse());
        assertFalse(policy.isApprovedResource(ResourceLocation.fromNamespaceAndPath("example", "ores/iron")));
    }

    @Test
    void rejectsLoadedItemResourcesInsteadOfApprovingMaterialTokensAsGenericResources() {
        ResourceRef diamondItem = ResourceRef.item("minecraft", "diamond");

        ResourcePolicyDecision decision = policy.evaluate(diamondItem, scannerWithLoaded(diamondItem));

        assertFalse(decision.shouldUse());
        assertFalse(decision.shouldSkip());
    }

    @Test
    void rejectsLoadedFluidResourcesOutsideSpecializedCompatPolicies() {
        ResourceRef quartzFluid = ResourceRef.fluid("minecraft", "quartz_slurry");

        ResourcePolicyDecision decision = policy.evaluate(quartzFluid, scannerWithLoaded(quartzFluid));

        assertFalse(decision.shouldUse());
        assertFalse(decision.shouldSkip());
    }

    @Test
    void rejectsLoadedCommonNonBlockTagsInsteadOfTreatingOreTagsAsAnyRegistryType() {
        ResourceRef fluidOreTag = ResourceRef.fluidTag("c", "ores/iron");
        ResourceRef itemOreTag = ResourceRef.itemTag("c", "ores/iron");

        ResourcePolicyDecision fluidDecision = policy.evaluate(fluidOreTag, scannerWithLoaded(fluidOreTag));
        ResourcePolicyDecision itemDecision = policy.evaluate(itemOreTag, scannerWithLoaded(itemOreTag));

        assertFalse(fluidDecision.shouldUse());
        assertFalse(fluidDecision.shouldSkip());
        assertFalse(itemDecision.shouldUse());
        assertFalse(itemDecision.shouldSkip());
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

    @Test
    void optionalModReferencesUseLoadedModChecksInsteadOfResourceTokens() {
        ResourcePolicyDecision loaded = policy.evaluate(ResourceRef.mod("ae2"), scannerWithLoadedMod("ae2"));
        ResourcePolicyDecision missing = policy.evaluate(ResourceRef.mod("ae2"), scannerWithNoRuntimeResources());

        assertTrue(loaded.shouldUse());
        assertTrue(missing.shouldSkip());
        assertFalse(missing.shouldUse());
    }

    @Test
    void blankModReferencesAreRejectedAtConstruction() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> ResourceRef.mod(" "));

        assertEquals("modId must not be blank", error.getMessage());
    }

    @Test
    void missingResourceLoggingRequiresAResourceReference() {
        assertThrows(NullPointerException.class, () -> policy.logMissingResource(null, "missing from runtime"));
    }

    @Test
    void missingResourceLoggingRequiresAClearReason() {
        assertThrows(IllegalArgumentException.class, () -> policy.logMissingResource(
                ResourceRef.block("minecraft", "diamond_ore"),
                " "
        ));
    }

    private static LoadedResourceScanner scannerWithNoRuntimeResources() {
        return new TestScanner(Set.of(), "");
    }

    private static LoadedResourceScanner scannerWithOnlyDiamondOre() {
        return scannerWithLoaded(ResourceRef.block("minecraft", "diamond_ore"));
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded), "");
    }

    private static LoadedResourceScanner scannerWithLoadedMod(String modId) {
        return new TestScanner(Set.of(), modId);
    }

    private record TestScanner(Set<ResourceRef> loadedResources, String loadedModId) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return loadedModId.equals(modId);
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK, id));
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID, id));
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM, id));
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.BLOCK_TAG, id));
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.FLUID_TAG, id));
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return loadedResources.contains(new ResourceRef(ResourceType.ITEM_TAG, id));
        }
    }
}
