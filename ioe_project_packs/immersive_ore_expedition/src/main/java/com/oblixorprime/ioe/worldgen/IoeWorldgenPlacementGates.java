package com.oblixorprime.ioe.worldgen;

public record IoeWorldgenPlacementGates(
        boolean runtimeWorldgenEnabled,
        boolean provinceRuntimeIntegrationEnabled,
        boolean diagnosticsEnabled
) {
    public static IoeWorldgenPlacementGates fromConfig() {
        return new IoeWorldgenPlacementGates(
                IoeWorldgenConfig.runtimePlacementEnabled(),
                IoeWorldgenConfig.provinceRuntimeIntegrationEnabled(),
                IoeWorldgenConfig.runtimePlacementDiagnostics()
        );
    }

    public static IoeWorldgenPlacementGates disabled() {
        return new IoeWorldgenPlacementGates(false, false, false);
    }

    public boolean shouldNoOpRuntimePlacement() {
        return !runtimeWorldgenEnabled;
    }

    public boolean mayEvaluateProvinceRuntimeIntegration() {
        return runtimeWorldgenEnabled && provinceRuntimeIntegrationEnabled;
    }
}
