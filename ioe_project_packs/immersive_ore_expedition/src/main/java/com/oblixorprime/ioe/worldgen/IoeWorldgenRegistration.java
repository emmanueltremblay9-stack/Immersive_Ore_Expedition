package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record IoeWorldgenRegistration(
        List<ResourceLocation> futureFeatureKeys,
        IoeWorldgenPlacementGates placementGates,
        boolean configuredFeaturesRegistered,
        boolean placedFeaturesRegistered,
        boolean biomeModifiersRegistered
) {
    public IoeWorldgenRegistration {
        futureFeatureKeys = List.copyOf(Objects.requireNonNull(futureFeatureKeys, "futureFeatureKeys"));
        Objects.requireNonNull(placementGates, "placementGates");
    }

    public static IoeWorldgenRegistration scaffold(
            List<ResourceLocation> futureFeatureKeys,
            IoeWorldgenPlacementGates placementGates
    ) {
        return new IoeWorldgenRegistration(
                futureFeatureKeys,
                placementGates,
                false,
                false,
                false
        );
    }

    public boolean scaffoldOnly() {
        return !configuredFeaturesRegistered
                && !placedFeaturesRegistered
                && !biomeModifiersRegistered;
    }

    public boolean runtimePlacementNoOp() {
        return placementGates.shouldNoOpRuntimePlacement();
    }
}
