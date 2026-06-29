package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.LoadedResourceScanner;

import java.util.Objects;

public final class ProspectingCompatGates {
    public static final String IMMERSIVE_ENGINEERING = "immersiveengineering";
    public static final String IMMERSIVE_PETROLEUM = "immersivepetroleum";

    private ProspectingCompatGates() {
    }

    public static boolean immersiveEngineeringEnabled(LoadedResourceScanner scanner) {
        return modLoadedIfEnabled(
                scanner,
                IMMERSIVE_ENGINEERING,
                IoeIeipProspectingConfig.immersiveEngineeringEnabledIfLoaded()
        );
    }

    public static boolean immersivePetroleumEnabled(LoadedResourceScanner scanner) {
        return modLoadedIfEnabled(
                scanner,
                IMMERSIVE_PETROLEUM,
                IoeIeipProspectingConfig.immersivePetroleumEnabledIfLoaded()
        );
    }

    private static boolean modLoadedIfEnabled(LoadedResourceScanner scanner, String modId, boolean enabledByConfig) {
        Objects.requireNonNull(scanner, "scanner");
        return enabledByConfig && scanner.isModLoaded(modId);
    }
}
