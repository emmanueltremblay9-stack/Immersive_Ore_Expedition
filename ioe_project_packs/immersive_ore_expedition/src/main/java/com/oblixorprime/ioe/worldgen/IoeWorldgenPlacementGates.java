package com.oblixorprime.ioe.worldgen;

public record IoeWorldgenPlacementGates(
        boolean runtimeWorldgenEnabled,
        boolean provinceRuntimeIntegrationEnabled,
        boolean diagnosticsEnabled
) {
    public static IoeWorldgenPlacementGates fromConfig() {
        return new IoeWorldgenPlacementGates(
                false,
                IoeWorldgenConfig.provinceRuntimeIntegrationEnabled(),
                IoeWorldgenConfig.provinceDebugDiagnostics()
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
