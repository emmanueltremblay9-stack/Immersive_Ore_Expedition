package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Objects;

public final class RuntimeWorldgenRegistrationSmokeBridgeFeature extends Feature<NoneFeatureConfiguration> {
    public RuntimeWorldgenRegistrationSmokeBridgeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Objects.requireNonNull(context, "context");

        IoeRuntimeProofFeatureGates bridgeGates = IoeRuntimeProofFeatureGates.fromConfig();
        IoeWorldgenPlacementGates placementGates = IoeWorldgenPlacementGates.fromConfig();
        if (!IoeRuntimeProofFeatureBridge.shouldInvokeProof(bridgeGates, placementGates)) {
            logSkipped(context.origin(), bridgeGates, placementGates);
            return false;
        }

        if (!shouldPlaceBlocksFromBiomeInvocation(bridgeGates, placementGates)) {
            logSuppressedNaturalInvocation(context.origin(), bridgeGates, placementGates);
            return false;
        }

        return false;
    }

    static boolean shouldPlaceBlocksFromBiomeInvocation(
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        Objects.requireNonNull(bridgeGates, "bridgeGates");
        Objects.requireNonNull(placementGates, "placementGates");
        return false;
    }

    private static void logSkipped(
            BlockPos origin,
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (!bridgeGates.diagnosticsEnabled()) {
            return;
        }
        String reason = bridgeGates.shouldNoOpRuntimeProofFeature()
                ? "runtime proof feature gate disabled"
                : "runtime placement gate disabled";
        IoeExpeditionWorldgenMod.LOGGER.info(
                "IOE v19 runtime proof feature skipped at {}: {}",
                origin,
                reason
        );
    }

    private static void logSuppressedNaturalInvocation(
            BlockPos origin,
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (!bridgeGates.diagnosticsEnabled() && !placementGates.diagnosticsEnabled()) {
            return;
        }
        IoeExpeditionWorldgenMod.LOGGER.info(
                "IOE runtime proof feature suppressed natural/free placement at {}: biome feature invocations are diagnostic-only; ore loads require explicit anchor/chamber planning",
                origin
        );
    }
}
