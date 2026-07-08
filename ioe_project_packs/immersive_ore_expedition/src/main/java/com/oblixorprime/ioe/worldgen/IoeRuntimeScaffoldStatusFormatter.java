package com.oblixorprime.ioe.worldgen;

import java.util.List;
import java.util.Objects;

public final class IoeRuntimeScaffoldStatusFormatter {
    public static final String VISIBILITY_EXPLANATION =
            "Expedition site features are registered; visible world and JourneyMap targets require runtime placement gates to be enabled and a proven site to be generated.";

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
                "IOE live placement: anchors=" + (status.runtimeWorldgenEnabled()
                        ? "runtime gate enabled"
                        : "registered / runtime gate disabled")
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
