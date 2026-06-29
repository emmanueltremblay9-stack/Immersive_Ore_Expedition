package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceRuleValidatorTest {
    @Test
    void separatesUsableSkippedAndRejectedProvinceResources() {
        ResourceRef loadedIron = ResourceRef.block("minecraft", "iron_ore");
        ResourceRef missingDiamond = ResourceRef.block("minecraft", "diamond_ore");
        ResourceRef rejectedTin = ResourceRef.block("minecraft", "tin_ore");
        ProvinceRule rule = new ProvinceRule(
                ProvinceId.of("ioe_expedition_worldgen", "temperate_iron"),
                List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld")),
                List.of(loadedIron, missingDiamond, rejectedTin),
                List.of(ExpeditionStructureRegistry.TINY_VERTICAL_MINE_ENTRANCE)
        );

        ProvinceValidationResult result = new ProvinceRuleValidator(new ResourcePolicyService(), scannerWithLoaded(loadedIron)).validate(rule);

        assertTrue(result.hasUsableResources());
        assertEquals(List.of(loadedIron), result.usableResources());
        assertEquals(1, result.skippedResources().size());
        assertEquals(1, result.rejectedResources().size());
        assertFalse(result.rejectedResources().getFirst().reason().isBlank());
    }

    @Test
    void rejectsLoadedResourcesWhenProvinceHasNoEnabledAnchorStructure() {
        ResourceRef loadedIron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRule rule = new ProvinceRule(
                ProvinceId.of("ioe_expedition_worldgen", "unanchored_iron"),
                List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld")),
                List.of(loadedIron),
                List.of(ResourceLocation.fromNamespaceAndPath("example", "random_spot"))
        );

        ProvinceValidationResult result = new ProvinceRuleValidator(new ResourcePolicyService(), scannerWithLoaded(loadedIron)).validate(rule);

        assertFalse(result.hasUsableResources());
        assertTrue(result.usableResources().isEmpty());
        assertEquals(1, result.rejectedResources().size());
        assertFalse(result.rejectedResources().getFirst().reason().isBlank());
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new LoadedResourceScanner() {
            @Override
            public boolean isModLoaded(String modId) {
                return false;
            }

            @Override
            public boolean blockExists(ResourceLocation id) {
                return loaded.id().equals(id);
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
        };
    }
}
