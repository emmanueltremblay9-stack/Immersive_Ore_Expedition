package com.oblixorprime.ioe.worldgen;

import net.neoforged.bus.api.IEventBus;

import java.util.Objects;

public final class IoeWorldgenBootstrap {
    private IoeWorldgenBootstrap() {
    }

    public static IoeWorldgenRegistration bootstrap() {
        return registration(IoeWorldgenPlacementGates.fromConfig());
    }

    public static IoeWorldgenRegistration bootstrap(IEventBus modEventBus) {
        IoeRuntimeProofFeatureBridge.register(modEventBus);
        return registration(IoeWorldgenPlacementGates.fromConfig(), true);
    }

    static IoeWorldgenRegistration registration(IoeWorldgenPlacementGates placementGates) {
        return registration(placementGates, false);
    }

    static IoeWorldgenRegistration registration(
            IoeWorldgenPlacementGates placementGates,
            boolean customFeaturesRegistered
    ) {
        return IoeWorldgenRegistration.scaffold(
                IoeWorldgenFeatureKeys.allFeatureKeys(),
                Objects.requireNonNull(placementGates, "placementGates"),
                customFeaturesRegistered
        );
    }
}
