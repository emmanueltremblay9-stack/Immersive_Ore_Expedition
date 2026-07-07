package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;

import java.util.Objects;

public record IoeRuntimeScaffoldStatus(
        String modId,
        String modVersion,
        boolean runtimeWorldgenEnabled,
        boolean provinceRuntimeIntegrationEnabled,
        boolean diagnosticsEnabled,
        boolean worldgenRegistrationScaffoldReady,
        boolean anchorPlacementPlanningReady,
        boolean oreLoadChamberPlacementPlanningReady,
        boolean randomOreSuppressionPlanningReady,
        boolean biomeProvinceBindingScaffoldReady,
        boolean ieIpSurfaceCluePlanningReady,
        boolean scaffoldOnly,
        boolean configuredFeaturesRegistered,
        boolean placedFeaturesRegistered,
        boolean biomeModifiersRegistered
) {
    public IoeRuntimeScaffoldStatus {
        modId = requireNonBlank(modId, "modId");
        modVersion = normalizeVersion(modVersion);
    }

    public static IoeRuntimeScaffoldStatus fromConfig(
            String modVersion,
            boolean ieIpSurfaceCluePlanningReady
    ) {
        return fromRegistration(
                modVersion,
                IoeWorldgenBootstrap.bootstrap(),
                ieIpSurfaceCluePlanningReady
        );
    }

    public static IoeRuntimeScaffoldStatus fromRegistration(
            String modVersion,
            IoeWorldgenRegistration registration,
            boolean ieIpSurfaceCluePlanningReady
    ) {
        Objects.requireNonNull(registration, "registration");
        IoeWorldgenPlacementGates placementGates = registration.placementGates();
        return new IoeRuntimeScaffoldStatus(
                ImmersiveOreExpeditionMod.MODID,
                modVersion,
                placementGates.runtimeWorldgenEnabled(),
                placementGates.provinceRuntimeIntegrationEnabled(),
                placementGates.diagnosticsEnabled(),
                !registration.futureFeatureKeys().isEmpty(),
                registration.anchorPlacementPlanningReady(),
                registration.oreLoadChamberPlacementPlanningReady(),
                registration.randomOreSuppressionPlanningReady(),
                registration.liveBiomeProvinceBindingPlanningReady(),
                ieIpSurfaceCluePlanningReady,
                registration.scaffoldOnly(),
                registration.configuredFeaturesRegistered(),
                registration.placedFeaturesRegistered(),
                registration.biomeModifiersRegistered()
        );
    }

    public boolean livePlacementRegistered() {
        return configuredFeaturesRegistered || placedFeaturesRegistered || biomeModifiersRegistered;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String normalizeVersion(String modVersion) {
        if (modVersion == null || modVersion.isBlank()) {
            return "unknown";
        }
        return modVersion.trim();
    }
}
