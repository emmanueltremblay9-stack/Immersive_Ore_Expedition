package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.LoadedResourceScanner;

import java.util.Objects;

public final class GeOreWorldgenPolicy {
    private final LoadedResourceScanner scanner;

    public GeOreWorldgenPolicy(LoadedResourceScanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public boolean shouldDisableFreeGeOreWorldgen() {
        return CrystalGrowthCompatGates.georeEnabled(scanner) && IoeCrystalGrowthConfig.disableFreeGeoreWorldgen();
    }

    public boolean mustAnchorAllGeOreSites() {
        return CrystalGrowthCompatGates.georeEnabled(scanner)
                && IoeCrystalGrowthConfig.anchorAllGeoresToExpeditionStructures();
    }
}
