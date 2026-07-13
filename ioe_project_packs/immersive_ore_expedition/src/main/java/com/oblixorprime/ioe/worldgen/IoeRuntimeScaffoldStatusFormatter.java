package com.oblixorprime.ioe.worldgen;

import java.util.List;
import java.util.Objects;

public final class IoeRuntimeScaffoldStatusFormatter {
    public static final String VISIBILITY_EXPLANATION =
            "Natural expedition sites generate only in new chunks when their generation option is enabled; JourneyMap targets are indexed only after a proven placement.";

    private IoeRuntimeScaffoldStatusFormatter() {
    }

    public static List<String> format(IoeRuntimeScaffoldStatus status) {
        Objects.requireNonNull(status, "status");
        return List.of(
                "IOE status: modId=" + status.modId() + ", version=" + status.modVersion(),
                "IOE gates: naturalExpeditionSiteGenerationEnabled="
                        + status.naturalExpeditionSiteGenerationEnabled()
                        + ", runtimeWorldgenEnabled=" + status.runtimeWorldgenEnabled()
                        + ", provinceRuntimeIntegrationEnabled=" + status.provinceRuntimeIntegrationEnabled()
                        + ", diagnosticsEnabled=" + status.diagnosticsEnabled(),
                "IOE scaffold: worldgenRegistration=" + ready(status.worldgenRegistrationScaffoldReady())
                        + ", anchorPlacementPlanning=" + ready(status.anchorPlacementPlanningReady())
                        + ", oreLoadChamberPlanning=" + ready(status.oreLoadChamberPlacementPlanningReady())
                        + ", randomOreSuppressionPlanning=" + ready(status.randomOreSuppressionPlanningReady())
                        + ", biomeProvinceBinding=" + ready(status.biomeProvinceBindingScaffoldReady())
                        + ", ieIpSurfaceCluePlanning=" + ready(status.ieIpSurfaceCluePlanningReady()),
                "IOE live placement: surfaceAnchors="
                        + (status.naturalExpeditionSiteGenerationEnabled() ? "natural generation enabled" : "disabled")
                        + ", mineshaftConnectors=connected child generation"
                        + ", oreChambers=connected child generation"
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
