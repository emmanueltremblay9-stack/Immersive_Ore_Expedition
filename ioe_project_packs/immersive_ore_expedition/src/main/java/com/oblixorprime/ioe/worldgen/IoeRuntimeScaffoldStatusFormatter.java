package com.oblixorprime.ioe.worldgen;

import java.util.List;
import java.util.Objects;

public final class IoeRuntimeScaffoldStatusFormatter {
    public static final String VISIBILITY_EXPLANATION =
            "No visible world or JourneyMap changes are expected until a later PR enables gated runtime placement or map marker integration.";

    private IoeRuntimeScaffoldStatusFormatter() {
    }

    public static List<String> format(IoeRuntimeScaffoldStatus status) {
        Objects.requireNonNull(status, "status");
        return List.of(
                "IOE status: modId=" + status.modId() + ", version=" + status.modVersion(),
                "IOE gates: runtimeWorldgenEnabled=" + status.runtimeWorldgenEnabled()
                        + ", provinceRuntimeIntegrationEnabled=" + status.provinceRuntimeIntegrationEnabled()
                        + ", diagnosticsEnabled=" + status.diagnosticsEnabled(),
                "IOE scaffold: worldgenRegistration=" + ready(status.worldgenRegistrationScaffoldReady())
                        + ", anchorPlacementPlanning=" + ready(status.anchorPlacementPlanningReady())
                        + ", oreLoadChamberPlanning=" + ready(status.oreLoadChamberPlacementPlanningReady())
                        + ", randomOreSuppressionPlanning=" + ready(status.randomOreSuppressionPlanningReady())
                        + ", biomeProvinceBinding=" + ready(status.biomeProvinceBindingScaffoldReady())
                        + ", ieIpSurfaceCluePlanning=" + ready(status.ieIpSurfaceCluePlanningReady()),
                "IOE live placement: anchors=not live / planning-only"
                        + ", oreChambers=not live / planning-only"
                        + ", ieIpSurfaceClues=not live / planning-only"
                        + ", crystalsGeodes=not live / planning-only"
                        + ", retrogenMutation=not live / planning-only",
                "IOE registration: configuredFeaturesRegistered=" + status.configuredFeaturesRegistered()
                        + ", placedFeaturesRegistered=" + status.placedFeaturesRegistered()
                        + ", biomeModifiersRegistered=" + status.biomeModifiersRegistered()
                        + ", scaffoldOnly=" + status.scaffoldOnly(),
                "IOE visibility: " + VISIBILITY_EXPLANATION
        );
    }

    private static String ready(boolean ready) {
        return ready ? "ready" : "not ready";
    }
}
