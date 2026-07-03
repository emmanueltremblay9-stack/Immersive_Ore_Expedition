package com.oblixorprime.ioe.worldgen;

import java.util.Objects;

public final class IoeWorldgenBootstrap {
    private IoeWorldgenBootstrap() {
    }

    public static IoeWorldgenRegistration bootstrap() {
        return registration(IoeWorldgenPlacementGates.fromConfig());
    }

    static IoeWorldgenRegistration registration(IoeWorldgenPlacementGates placementGates) {
        return IoeWorldgenRegistration.scaffold(
                IoeWorldgenFeatureKeys.allFeatureKeys(),
                Objects.requireNonNull(placementGates, "placementGates")
        );
    }
}
