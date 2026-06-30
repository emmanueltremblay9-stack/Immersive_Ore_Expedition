package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceResourcePolicy;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProvinceRuntimeIntegrationTest {
    private final OreLoadGenerator generator = new OreLoadGenerator();
    private final ResourcePolicyService resourcePolicyService = new ResourcePolicyService();

    @Test
    void disabledIntegrationLeavesOreLoadPlanningOnExistingPolicy() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                false,
                ProvinceBindingResolver.parse(
                        "ioe_core:default",
                        Set.of("minecraft:plains=ioe_core:temperate_iron"),
                        false
                ),
                denyingProvincePolicy("vanilla"),
                resourcePolicyService,
                scannerWithLoaded(iron)
        );

        Optional<OreLoadPlan> plan = generator.planAnchoredOreLoad(
                anchor(),
                iron,
                new BlockPos(16, 64, 0),
                scannerWithLoaded(iron),
                resourcePolicyService,
                integration
        );

        assertTrue(plan.isPresent());
    }

    @Test
    void enabledIntegrationUsesProvincePolicyDecisions() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration allowed = enabledIntegration(
                ProvinceResourcePolicy.defaults(),
                scannerWithLoaded(iron),
                ProvinceBindingResolver.defaults()
        );
        ProvinceRuntimeIntegration denied = enabledIntegration(
                denyingProvincePolicy("vanilla"),
                scannerWithLoaded(iron),
                ProvinceBindingResolver.defaults()
        );

        assertTrue(allowed.evaluateOreLoadResource(anchor(), iron).shouldUse());
        assertFalse(denied.evaluateOreLoadResource(anchor(), iron).shouldUse());
    }

    @Test
    void defaultProvinceIsUsedWhenNoBiomeBindingMatches() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = enabledIntegration(
                ProvinceResourcePolicy.defaults(),
                scannerWithLoaded(iron),
                ProvinceBindingResolver.parse(
                        "immersive_ore_expedition:default",
                        Set.of("minecraft:desert=immersive_ore_expedition:arid"),
                        false
                )
        );

        ResourcePolicyDecision decision = integration.evaluateOreLoadResource(
                anchor(),
                iron,
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains")
        );

        assertTrue(decision.shouldUse());
        assertTrue(decision.reason().contains("immersive_ore_expedition:default"));
    }

    @Test
    void exactBiomeBindingFeedsResolvedProvinceIntoRuntimeDecision() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = enabledIntegration(
                ProvinceResourcePolicy.defaults(),
                scannerWithLoaded(iron),
                ProvinceBindingResolver.parse(
                        "immersive_ore_expedition:default",
                        Set.of("minecraft:plains=temperate_iron"),
                        false
                )
        );

        ResourcePolicyDecision decision = integration.evaluateOreLoadResource(
                anchor(),
                iron,
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains")
        );

        assertTrue(decision.shouldUse());
        assertTrue(decision.reason().contains("immersive_ore_expedition:temperate_iron"));
    }

    @Test
    void strictExclusionsOverrideEnabledRuntimeIntegration() {
        List<ResourceRef> strictExclusions = List.of(
                ResourceRef.block("minecraft", "apatite_ore"),
                ResourceRef.block("minecraft", "tin_ore"),
                ResourceRef.block("forestry", "copper_ore"),
                ResourceRef.block("minecraft", "platinum_ore"),
                ResourceRef.block("minecraft", "osmium_ore"),
                ResourceRef.block("minecraft", "tungsten_ore"),
                ResourceRef.block("minecraft", "black_quartz_ore"),
                ResourceRef.block("minecraft", "uraninite_ore"),
                ResourceRef.block("minecraft", "monazite_ore")
        );

        for (ResourceRef resource : strictExclusions) {
            ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                    true,
                    ProvinceBindingResolver.parse(
                            "ioe_core:default",
                            Set.of("minecraft:plains=ioe_core:temperate_iron"),
                            false
                    ),
                    ProvinceResourcePolicy.defaults(),
                    resourcePolicyService,
                    scannerWithLoaded(resource)
            );

            ResourcePolicyDecision decision = integration.evaluateOreLoadResource(anchor(), resource);

            assertFalse(decision.shouldUse());
            assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
            assertTrue(decision.reason().contains("explicitly excluded"));
        }
    }

    @Test
    void oldSplitProvinceNamespacesAreRejectedByDefault() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                true,
                ProvinceBindingResolver.parse(
                        "ioe_core:default",
                        Set.of("minecraft:plains=ioe_core:temperate_iron"),
                        false
                ),
                ProvinceResourcePolicy.defaults(),
                resourcePolicyService,
                scannerWithLoaded(iron)
        );

        ResourcePolicyDecision decision = integration.evaluateOreLoadResource(
                anchor(),
                iron,
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains")
        );

        assertTrue(decision.shouldUse());
        assertTrue(decision.reason().contains("immersive_ore_expedition:default"));
    }

    @Test
    void decisionsAreDeterministicForIdenticalInputs() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = enabledIntegration(
                ProvinceResourcePolicy.defaults(),
                scannerWithLoaded(iron),
                ProvinceBindingResolver.parse(
                        "immersive_ore_expedition:default",
                        Set.of("minecraft:plains=temperate_iron"),
                        false
                )
        );

        ResourceLocation biome = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        ResourcePolicyDecision first = integration.evaluateOreLoadResource(anchor(), iron, biome);
        ResourcePolicyDecision second = integration.evaluateOreLoadResource(anchor(), iron, biome);

        assertEquals(first, second);
    }

    private ProvinceRuntimeIntegration enabledIntegration(
            ProvinceResourcePolicy provincePolicy,
            LoadedResourceScanner scanner,
            ProvinceBindingResolver bindingResolver
    ) {
        return new ProvinceRuntimeIntegration(
                true,
                bindingResolver,
                provincePolicy,
                resourcePolicyService,
                scanner
        );
    }

    private static ProvinceResourcePolicy denyingProvincePolicy(String category) {
        return new ProvinceResourcePolicy(
                ProvinceResourcePolicy.DEFAULT_ALLOWED_CATEGORIES,
                Set.of(category),
                Set.copyOf(ResourcePolicyService.STRICT_EXCLUDED_RESOURCE_NAMES),
                false
        );
    }

    private static ExpeditionAnchorRef anchor() {
        return new ExpeditionAnchorRef(
                Level.OVERWORLD,
                new BlockPos(0, 64, 0),
                "tiny_vertical_mine_entrance",
                SiteQuality.NORMAL
        );
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded));
    }

    private record TestScanner(Set<ResourceRef> loadedResources) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
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
