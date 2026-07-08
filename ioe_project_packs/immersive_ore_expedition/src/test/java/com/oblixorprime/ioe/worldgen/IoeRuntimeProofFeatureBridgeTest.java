package com.oblixorprime.ioe.worldgen;

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
}
