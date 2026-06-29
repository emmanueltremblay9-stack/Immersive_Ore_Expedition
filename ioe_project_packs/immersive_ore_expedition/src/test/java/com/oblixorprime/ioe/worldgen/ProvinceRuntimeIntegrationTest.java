package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
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
                "ioe_core",
                false,
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
                scannerWithLoaded(iron)
        );
        ProvinceRuntimeIntegration denied = enabledIntegration(
                denyingProvincePolicy("vanilla"),
                scannerWithLoaded(iron)
        );

        assertTrue(allowed.evaluateOreLoadResource(anchor(), iron).shouldUse());
        assertFalse(denied.evaluateOreLoadResource(anchor(), iron).shouldUse());
    }

    @Test
    void strictExclusionsOverrideEnabledRuntimeIntegration() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");
        ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                true,
                "ioe_core",
                false,
                ProvinceResourcePolicy.defaults(),
                resourcePolicyService,
                scannerWithLoaded(tin)
        );

        ResourcePolicyDecision decision = integration.evaluateOreLoadResource(anchor(), tin);

        assertFalse(decision.shouldUse());
        assertEquals(ResourcePolicyDecision.Action.REJECT, decision.action());
        assertTrue(decision.reason().contains("explicitly excluded"));
    }

    @Test
    void oldSplitProvinceNamespacesAreRejectedByDefault() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = new ProvinceRuntimeIntegration(
                true,
                "ioe_core",
                false,
                ProvinceResourcePolicy.defaults(),
                resourcePolicyService,
                scannerWithLoaded(iron)
        );

        ResourcePolicyDecision decision = integration.evaluateOreLoadResource(anchor(), iron);

        assertFalse(decision.shouldUse());
        assertTrue(decision.reason().contains(ProvinceId.CONSOLIDATED_NAMESPACE));
    }

    @Test
    void decisionsAreDeterministicForIdenticalInputs() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");
        ProvinceRuntimeIntegration integration = enabledIntegration(
                ProvinceResourcePolicy.defaults(),
                scannerWithLoaded(iron)
        );

        ResourcePolicyDecision first = integration.evaluateOreLoadResource(anchor(), iron);
        ResourcePolicyDecision second = integration.evaluateOreLoadResource(anchor(), iron);

        assertEquals(first, second);
    }

    private ProvinceRuntimeIntegration enabledIntegration(
            ProvinceResourcePolicy provincePolicy,
            LoadedResourceScanner scanner
    ) {
        return new ProvinceRuntimeIntegration(
                true,
                ProvinceId.CONSOLIDATED_NAMESPACE,
                false,
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
