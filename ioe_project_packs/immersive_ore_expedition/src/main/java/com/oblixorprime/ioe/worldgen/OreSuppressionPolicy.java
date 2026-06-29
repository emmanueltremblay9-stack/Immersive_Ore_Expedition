package com.oblixorprime.ioe.worldgen;

public final class OreSuppressionPolicy {
    public int scaledRandomOreCount(int originalCount) {
        if (originalCount <= 0) {
            return 0;
        }
        double scaled = originalCount * densityMultiplier();
        return Math.max(0, (int) Math.floor(scaled));
    }

    public boolean requiresExpeditionAnchorForMajorLoad() {
        return IoeWorldgenConfig.requireStructureAnchorForMajorOreLoads();
    }

    public boolean allowsTinyScrapOutsideProvinces() {
        return IoeWorldgenConfig.allowTinyScrapOreOutsideProvinces();
    }

    public double densityMultiplier() {
        return Math.clamp(IoeWorldgenConfig.randomOreDensityMultiplier(), 0.0D, 1.0D);
    }
}
