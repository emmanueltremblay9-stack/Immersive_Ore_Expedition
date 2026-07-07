package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RuntimeWorldgenPlacementProofTest {
    private static final IoeWorldgenPlacementGates ENABLED_GATES =
            new IoeWorldgenPlacementGates(true, false, false);
    private static final IoeWorldgenPlacementGates ENABLED_DIAGNOSTIC_GATES =
            new IoeWorldgenPlacementGates(true, false, true);

    private final RuntimeWorldgenPlacementProof proof = new RuntimeWorldgenPlacementProof();
    private final ResourcePolicyService policyService = new ResourcePolicyService();

    @Test
    void defaultConfigGateKeepsRuntimePlacementProofNoOp() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                BlockPos.ZERO,
                SiteQuality.NORMAL,
                iron,
                scannerWithLoaded(iron),
                policyService,
                IoeWorldgenPlacementGates.fromConfig()
        );

        assertFalse(result.placementPathAllowed());
        assertFalse(result.blockPlaced());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.RUNTIME_WORLDGEN_DISABLED, result.skipReason());
    }

    @Test
    void enabledGateAllowsPlacementProofOnlyForLoadedApprovedBlockResources() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        assertTrue(result.placementPathAllowed());
        assertFalse(result.blockPlaced());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.NONE, result.skipReason());
        assertEquals(iron, result.proofResource().orElseThrow());
        assertEquals(ResourcePolicyDecision.Action.USE, result.resourceDecision().orElseThrow().action());
    }

    @Test
    void enabledGateDoesNotBypassMissingResourceValidation() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                iron,
                scannerWithNothingLoaded(),
                policyService,
                ENABLED_GATES
        );

        assertFalse(result.placementPathAllowed());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.RESOURCE_NOT_LOADED, result.skipReason());
        assertTrue(result.resourceDecision().orElseThrow().shouldSkip());
    }

    @Test
    void enabledGateDoesNotBypassResourcePolicyDenial() {
        ResourceRef dirt = ResourceRef.block("minecraft", "dirt");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                dirt,
                scannerWithLoaded(dirt),
                policyService,
                ENABLED_GATES
        );

        assertFalse(result.placementPathAllowed());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.RESOURCE_DENIED_BY_POLICY, result.skipReason());
        assertFalse(result.resourceDecision().orElseThrow().shouldUse());
    }

    @Test
    void strictExclusionsWinBeforeRuntimePlacementProof() {
        ResourceRef tin = ResourceRef.block("minecraft", "tin_ore");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                tin,
                scannerWithLoaded(tin),
                policyService,
                ENABLED_GATES
        );

        assertFalse(result.placementPathAllowed());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.STRICT_EXCLUSION, result.skipReason());
    }

    @Test
    void blockTagsAreNotSubstitutedIntoProofBlocks() {
        ResourceRef ironTag = ResourceRef.blockTag("c", "ores/iron");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                ironTag,
                scannerWithLoaded(ironTag),
                policyService,
                ENABLED_GATES
        );

        assertFalse(result.placementPathAllowed());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.UNSUPPORTED_PROOF_RESOURCE, result.skipReason());
    }

    @Test
    void diagnosticsRemainOptIn() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RuntimeWorldgenPlacementProofResult quiet = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );
        RuntimeWorldgenPlacementProofResult diagnostic = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_DIAGNOSTIC_GATES
        );

        assertFalse(quiet.diagnosticsEnabled());
        assertTrue(diagnostic.diagnosticsEnabled());
    }

    @Test
    void invalidAnchorPreventsResourcePlacementProof() {
        ResourceRef iron = ResourceRef.block("minecraft", "iron_ore");

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                ResourceLocation.fromNamespaceAndPath("ioe_expedition_worldgen", "tiny_vertical_mine_entrance"),
                new BlockPos(4, 64, 4),
                SiteQuality.NORMAL,
                iron,
                scannerWithLoaded(iron),
                policyService,
                ENABLED_GATES
        );

        assertFalse(result.placementPathAllowed());
        assertEquals(RuntimeWorldgenPlacementProofResult.SkipReason.ANCHOR_PLANNING_REJECTED, result.skipReason());
        assertTrue(result.anchorPlan().isPresent());
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(Set.of(loaded));
    }

    private static LoadedResourceScanner scannerWithNothingLoaded() {
        return new TestScanner(Set.of());
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
