package com.oblixorprime.ioe.worldgen;

public record IoeRuntimeProofFeatureGates(
        boolean runtimeProofFeatureEnabled,
        boolean diagnosticsEnabled
) {
    public static IoeRuntimeProofFeatureGates fromConfig() {
        return new IoeRuntimeProofFeatureGates(
                IoeWorldgenConfig.runtimeProofFeatureEnabled(),
                IoeWorldgenConfig.runtimeProofFeatureDiagnostics()
        );
    }

    public static IoeRuntimeProofFeatureGates disabled() {
        return new IoeRuntimeProofFeatureGates(false, false);
    }

    public boolean shouldNoOpRuntimeProofFeature() {
        return !runtimeProofFeatureEnabled;
    }
}
