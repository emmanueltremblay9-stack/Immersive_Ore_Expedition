package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.LoadedResourceScanner;

import java.util.Objects;
import java.util.Set;

public final class CrystalGrowthCompatGates {
    public static final String AE2 = "ae2";
    public static final String AE2_CRYSTAL_SCIENCE = "ae2cs";
    public static final String GEORE = "geore";
    private static final Set<String> AE2_BUDDING_CERTUS_PATHS = Set.of(
            "flawless_budding_quartz",
            "flawed_budding_quartz",
            "chipped_budding_quartz",
            "damaged_budding_quartz"
    );

    private CrystalGrowthCompatGates() {
    }

    public static boolean ae2Enabled(LoadedResourceScanner scanner) {
        Objects.requireNonNull(scanner, "scanner");
        return IoeCrystalGrowthConfig.ae2EnabledIfLoaded() && scanner.isModLoaded(AE2);
    }

    public static boolean ae2CrystalProcessingStackEnabled(LoadedResourceScanner scanner) {
        Objects.requireNonNull(scanner, "scanner");
        return ae2Enabled(scanner) && scanner.isModLoaded(AE2_CRYSTAL_SCIENCE);
    }

    public static boolean georeEnabled(LoadedResourceScanner scanner) {
        Objects.requireNonNull(scanner, "scanner");
        return IoeCrystalGrowthConfig.georeEnabledIfLoaded() && scanner.isModLoaded(GEORE);
    }

    public static boolean isNativeBuddingCertusPath(String path) {
        return path != null && AE2_BUDDING_CERTUS_PATHS.contains(path);
    }
}
