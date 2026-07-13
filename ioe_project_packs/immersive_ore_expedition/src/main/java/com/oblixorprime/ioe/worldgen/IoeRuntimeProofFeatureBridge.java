package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Objects;

final class IoeRuntimeProofFeatureBridge {
    private IoeRuntimeProofFeatureBridge() {
    }

    static void register(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus")
                .addListener(RegisterEvent.class, IoeRuntimeProofFeatureBridge::registerFeatures);
    }

    static ResourceLocation featureId() {
        return IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE;
    }

    static List<ResourceLocation> registeredFeatureIds() {
        return ExpeditionSiteType.registeredFeatureIds();
    }

    static boolean shouldInvokeProof(
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        Objects.requireNonNull(bridgeGates, "bridgeGates");
        Objects.requireNonNull(placementGates, "placementGates");
        return bridgeGates.runtimeProofFeatureEnabled() && !placementGates.shouldNoOpRuntimePlacement();
    }

    static boolean shouldPlaceBlocksFromBiomeInvocation(
            IoeRuntimeProofFeatureGates bridgeGates,
            IoeWorldgenPlacementGates placementGates
    ) {
        Objects.requireNonNull(bridgeGates, "bridgeGates");
        Objects.requireNonNull(placementGates, "placementGates");
        return shouldInvokeProof(bridgeGates, placementGates);
    }

    private static void registerFeatures(RegisterEvent event) {
        Objects.requireNonNull(event, "event");
        for (ExpeditionSiteType siteType : ExpeditionSiteType.values()) {
            event.register(
                    Registries.FEATURE,
                    siteType.id(),
                    () -> new ExpeditionSiteFeature(siteType)
            );
        }
    }
}
