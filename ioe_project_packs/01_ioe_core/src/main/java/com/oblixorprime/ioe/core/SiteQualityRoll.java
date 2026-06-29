package com.oblixorprime.ioe.core;

import net.minecraft.util.RandomSource;

public record SiteQualityRoll(int dryWeight, int poorWeight, int normalWeight, int richWeight, int motherlodeWeight) {
    public static final SiteQualityRoll DEFAULT = new SiteQualityRoll(10, 25, 45, 17, 3);

    public SiteQualityRoll {
        if (dryWeight < 0 || poorWeight < 0 || normalWeight < 0 || richWeight < 0 || motherlodeWeight < 0) {
            throw new IllegalArgumentException("Site quality weights must not be negative");
        }
        if (totalWeight(dryWeight, poorWeight, normalWeight, richWeight, motherlodeWeight) <= 0) {
            throw new IllegalArgumentException("At least one site quality weight must be positive");
        }
    }

    public int totalWeight() {
        return totalWeight(dryWeight, poorWeight, normalWeight, richWeight, motherlodeWeight);
    }

    public SiteQuality roll(RandomSource randomSource) {
        return qualityAt(randomSource.nextInt(totalWeight()));
    }

    public SiteQuality qualityAt(int roll) {
        if (roll < 0 || roll >= totalWeight()) {
            throw new IllegalArgumentException("Roll must be in [0, " + totalWeight() + "): " + roll);
        }
        int threshold = dryWeight;
        if (roll < threshold) {
            return SiteQuality.DRY;
        }
        threshold += poorWeight;
        if (roll < threshold) {
            return SiteQuality.POOR;
        }
        threshold += normalWeight;
        if (roll < threshold) {
            return SiteQuality.NORMAL;
        }
        threshold += richWeight;
        if (roll < threshold) {
            return SiteQuality.RICH;
        }
        return SiteQuality.MOTHERLODE;
    }

    private static int totalWeight(int dryWeight, int poorWeight, int normalWeight, int richWeight, int motherlodeWeight) {
        long total = (long) dryWeight + poorWeight + normalWeight + richWeight + motherlodeWeight;
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Site quality total weight must not exceed Integer.MAX_VALUE");
        }
        return (int) total;
    }
}
