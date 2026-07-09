package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorIndex;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeRuntimeProofFeatureBridgeTest {
    @Test
    void defaultConfigKeepsRegisteredProofFeatureNoOpAndQuiet() {
        IoeRuntimeProofFeatureGates gates = IoeRuntimeProofFeatureGates.fromConfig();

        assertFalse(IoeWorldgenConfig.runtimeProofFeatureEnabled());
        assertFalse(IoeWorldgenConfig.runtimeProofFeatureDiagnostics());
        assertFalse(gates.runtimeProofFeatureEnabled());
        assertFalse(gates.diagnosticsEnabled());
        assertTrue(gates.shouldNoOpRuntimeProofFeature());
    }

    @Test
    void enabledBridgeStillRequiresRuntimePlacementGate() {
        IoeRuntimeProofFeatureGates enabledBridge = new IoeRuntimeProofFeatureGates(true, false);
        IoeWorldgenPlacementGates disabledPlacement = new IoeWorldgenPlacementGates(false, false, false);
        IoeWorldgenPlacementGates enabledPlacement = new IoeWorldgenPlacementGates(true, false, false);

        assertFalse(IoeRuntimeProofFeatureBridge.shouldInvokeProof(enabledBridge, disabledPlacement));
        assertTrue(IoeRuntimeProofFeatureBridge.shouldInvokeProof(enabledBridge, enabledPlacement));
    }

    @Test
    void enabledBiomeProofFeatureInvocationCanPlaceWhenRuntimeGatesAllowIt() {
        IoeRuntimeProofFeatureGates enabledBridge = new IoeRuntimeProofFeatureGates(true, false);
        IoeWorldgenPlacementGates enabledPlacement = new IoeWorldgenPlacementGates(true, true, false);

        assertTrue(IoeRuntimeProofFeatureBridge.shouldInvokeProof(enabledBridge, enabledPlacement));
        assertTrue(IoeRuntimeProofFeatureBridge.shouldPlaceBlocksFromBiomeInvocation(
                enabledBridge,
                enabledPlacement
        ));
    }

    @Test
    void disabledBridgePreventsProofEvenWhenRuntimePlacementGateIsEnabled() {
        IoeRuntimeProofFeatureGates disabledBridge = IoeRuntimeProofFeatureGates.disabled();
        IoeWorldgenPlacementGates enabledPlacement = new IoeWorldgenPlacementGates(true, false, false);

        assertFalse(IoeRuntimeProofFeatureBridge.shouldInvokeProof(disabledBridge, enabledPlacement));
    }

    @Test
    void bridgeUsesTinyVerticalMineEntranceFeatureId() {
        assertEquals(IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE, IoeRuntimeProofFeatureBridge.featureId());
    }

    @Test
    void defaultOffRuntimeProofResultCannotRecordAPlayableSiteThroughBridgeSeam() {
        RuntimeWorldgenPlacementProof proof = new RuntimeWorldgenPlacementProof();
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ResourceRef proofResource = RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE;

        RuntimeWorldgenPlacementProofResult result = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(8, 72, 8),
                SiteQuality.NORMAL,
                proofResource,
                scannerWithLoaded(proofResource),
                new ResourcePolicyService(),
                IoeWorldgenPlacementGates.disabled()
        );

        assertFalse(RuntimeWorldgenProofSiteRecorder.placeAndRecordProvenSite(
                Level.OVERWORLD,
                result,
                () -> true,
                index::recordPlacedProof
        ));
        assertTrue(index.sites().isEmpty());
        assertTrue(index.diagnosticSites().isEmpty());
    }

    @Test
    void enabledRuntimeBridgeSeamRecordsOnlyAfterSafeFootprintPlacement() {
        RuntimeWorldgenPlacementProof proof = new RuntimeWorldgenPlacementProof();
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ResourceRef proofResource = RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE;

        RuntimeWorldgenPlacementProofResult ready = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(8, 72, 8),
                SiteQuality.NORMAL,
                proofResource,
                scannerWithLoaded(proofResource),
                new ResourcePolicyService(),
                new IoeWorldgenPlacementGates(true, false, false)
        );

        assertTrue(RuntimeWorldgenProofSiteRecorder.placeAndRecordProvenSite(
                Level.OVERWORLD,
                ready,
                () -> true,
                index::recordPlacedProof
        ));
        assertEquals(1, index.sites().size());
        assertTrue(index.sites().stream().allMatch(site -> site.placementState() == ExpeditionSitePlacementState.PROVEN));
    }

    @Test
    void enabledRuntimeBridgeSeamDoesNotRecordWhenFootprintPlacementFails() {
        RuntimeWorldgenPlacementProof proof = new RuntimeWorldgenPlacementProof();
        ExpeditionLocatorIndex index = new ExpeditionLocatorIndex();
        ResourceRef proofResource = RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE;

        RuntimeWorldgenPlacementProofResult ready = proof.evaluateAnchorProof(
                IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE,
                new BlockPos(8, 72, 8),
                SiteQuality.NORMAL,
                proofResource,
                scannerWithLoaded(proofResource),
                new ResourcePolicyService(),
                new IoeWorldgenPlacementGates(true, false, false)
        );

        assertFalse(RuntimeWorldgenProofSiteRecorder.placeAndRecordProvenSite(
                Level.OVERWORLD,
                ready,
                () -> false,
                index::recordPlacedProof
        ));
        assertTrue(index.sites().isEmpty());
        assertTrue(index.diagnosticSites().isEmpty());
    }

    private static LoadedResourceScanner scannerWithLoaded(ResourceRef loaded) {
        return new TestScanner(loaded);
    }

    private record TestScanner(ResourceRef loaded) implements LoadedResourceScanner {
        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public boolean blockExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.BLOCK && loaded.id().equals(id);
        }

        @Override
        public boolean fluidExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.FLUID && loaded.id().equals(id);
        }

        @Override
        public boolean itemExists(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.ITEM && loaded.id().equals(id);
        }

        @Override
        public boolean blockTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.BLOCK_TAG && loaded.id().equals(id);
        }

        @Override
        public boolean fluidTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.FLUID_TAG && loaded.id().equals(id);
        }

        @Override
        public boolean itemTagHasValues(ResourceLocation id) {
            return loaded != null && loaded.type() == ResourceType.ITEM_TAG && loaded.id().equals(id);
        }
    }
}
