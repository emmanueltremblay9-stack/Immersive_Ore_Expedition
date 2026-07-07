package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeWorldgenPlacementGatesTest {
    @Test
    void configBackedGatesKeepRuntimeWorldgenOffByDefault() {
        IoeWorldgenPlacementGates gates = IoeWorldgenPlacementGates.fromConfig();

        assertFalse(gates.runtimeWorldgenEnabled());
        assertTrue(gates.shouldNoOpRuntimePlacement());
        assertFalse(gates.provinceRuntimeIntegrationEnabled());
        assertFalse(gates.diagnosticsEnabled());
        assertFalse(IoeWorldgenConfig.runtimePlacementEnabled());
        assertFalse(IoeWorldgenConfig.runtimePlacementDiagnostics());
    }

    @Test
    void disabledRuntimePlacementPreventsProvinceRuntimeUse() {
        IoeWorldgenPlacementGates gates = new IoeWorldgenPlacementGates(false, true, true);

        assertTrue(gates.shouldNoOpRuntimePlacement());
        assertFalse(gates.mayEvaluateProvinceRuntimeIntegration());
    }

    @Test
    void enabledRuntimePlacementCanUseEnabledProvinceRuntime() {
        IoeWorldgenPlacementGates gates = new IoeWorldgenPlacementGates(true, true, false);

        assertFalse(gates.shouldNoOpRuntimePlacement());
        assertTrue(gates.mayEvaluateProvinceRuntimeIntegration());
    }
}
