package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Objects;

public final class RuntimeWorldgenRegistrationSmokeBridgeFeature extends Feature<NoneFeatureConfiguration> {
    private final OreLoadGenerator oreLoadGenerator;

    public RuntimeWorldgenRegistrationSmokeBridgeFeature() {
        this(new OreLoadGenerator());
    }

    RuntimeWorldgenRegistrationSmokeBridgeFeature(OreLoadGenerator oreLoadGenerator) {
        super(NoneFeatureConfiguration.CODEC);
        this.oreLoadGenerator = Objects.requireNonNull(oreLoadGenerator, "oreLoadGenerator");
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

        return oreLoadGenerator.generateAnchoredOreLoad(context.level(), context.origin());
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
}
