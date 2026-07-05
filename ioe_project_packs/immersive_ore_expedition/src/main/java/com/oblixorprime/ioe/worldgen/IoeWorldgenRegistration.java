package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record IoeWorldgenRegistration(
        List<ResourceLocation> futureFeatureKeys,
        List<ResourceLocation> anchorPlacementPlanKeys,
        List<ResourceLocation> oreLoadChamberPlacementPlanKeys,
        boolean randomOreSuppressionPlanningReady,
        boolean liveBiomeProvinceBindingPlanningReady,
        boolean ieIpSurfaceCluePlacementPlanningReady,
        boolean crystalAe2SitePlacementPlanningReady,
        boolean meteoriticAe2GeodePlacementPlanningReady,
        boolean netherSubLavaGeodePlacementPlanningReady,
        boolean persistentConservativeRetrogenPlanningReady,
        IoeWorldgenPlacementGates placementGates,
        boolean customFeaturesRegistered,
        boolean configuredFeaturesRegistered,
        boolean placedFeaturesRegistered,
        boolean biomeModifiersRegistered
) {
    public IoeWorldgenRegistration {
        futureFeatureKeys = List.copyOf(Objects.requireNonNull(futureFeatureKeys, "futureFeatureKeys"));
        anchorPlacementPlanKeys = List.copyOf(Objects.requireNonNull(anchorPlacementPlanKeys, "anchorPlacementPlanKeys"));
        oreLoadChamberPlacementPlanKeys = List.copyOf(Objects.requireNonNull(oreLoadChamberPlacementPlanKeys, "oreLoadChamberPlacementPlanKeys"));
        Objects.requireNonNull(placementGates, "placementGates");
    }

    public static IoeWorldgenRegistration scaffold(
            List<ResourceLocation> futureFeatureKeys,
            IoeWorldgenPlacementGates placementGates,
            boolean customFeaturesRegistered
    ) {
        return new IoeWorldgenRegistration(
                futureFeatureKeys,
                IoeWorldgenFeatureKeys.anchorFeatureKeys(),
                IoeWorldgenFeatureKeys.oreLoadChamberFeatureKeys(),
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                placementGates,
                customFeaturesRegistered,
                false,
                false,
                false
        );
    }

    public boolean scaffoldOnly() {
        return !customFeaturesRegistered
                && !configuredFeaturesRegistered
                && !placedFeaturesRegistered
                && !biomeModifiersRegistered;
    }

    public boolean runtimePlacementNoOp() {
        return placementGates.shouldNoOpRuntimePlacement();
    }

    public boolean anchorPlacementPlanningReady() {
        return !anchorPlacementPlanKeys.isEmpty();
    }

    public boolean oreLoadChamberPlacementPlanningReady() {
        return !oreLoadChamberPlacementPlanKeys.isEmpty();
    }
}
