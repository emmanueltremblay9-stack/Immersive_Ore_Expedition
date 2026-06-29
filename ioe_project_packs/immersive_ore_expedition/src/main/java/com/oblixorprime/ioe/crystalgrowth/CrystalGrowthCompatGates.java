package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.LoadedResourceScanner;

import java.util.Objects;

public final class CrystalGrowthCompatGates {
    public static final String AE2 = "ae2";
    public static final String GEORE = "geore";

    private CrystalGrowthCompatGates() {
    }

    public static boolean ae2Enabled(LoadedResourceScanner scanner) {
        Objects.requireNonNull(scanner, "scanner");
        return IoeCrystalGrowthConfig.ae2EnabledIfLoaded() && scanner.isModLoaded(AE2);
    }

    public static boolean georeEnabled(LoadedResourceScanner scanner) {
        Objects.requireNonNull(scanner, "scanner");
        return IoeCrystalGrowthConfig.georeEnabledIfLoaded() && scanner.isModLoaded(GEORE);
    }
}
