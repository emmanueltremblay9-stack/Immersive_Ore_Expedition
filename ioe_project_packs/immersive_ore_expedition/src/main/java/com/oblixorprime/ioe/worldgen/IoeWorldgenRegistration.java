package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record IoeWorldgenRegistration(
        List<ResourceLocation> futureFeatureKeys,
        List<ResourceLocation> anchorPlacementPlanKeys,
        IoeWorldgenPlacementGates placementGates,
        boolean configuredFeaturesRegistered,
        boolean placedFeaturesRegistered,
        boolean biomeModifiersRegistered
) {
    public IoeWorldgenRegistration {
        futureFeatureKeys = List.copyOf(Objects.requireNonNull(futureFeatureKeys, "futureFeatureKeys"));
        anchorPlacementPlanKeys = List.copyOf(Objects.requireNonNull(anchorPlacementPlanKeys, "anchorPlacementPlanKeys"));
        Objects.requireNonNull(placementGates, "placementGates");
    }

    public static IoeWorldgenRegistration scaffold(
            List<ResourceLocation> futureFeatureKeys,
            IoeWorldgenPlacementGates placementGates
    ) {
        return new IoeWorldgenRegistration(
                futureFeatureKeys,
                IoeWorldgenFeatureKeys.anchorFeatureKeys(),
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

    public boolean anchorPlacementPlanningReady() {
        return !anchorPlacementPlanKeys.isEmpty();
    }
}
