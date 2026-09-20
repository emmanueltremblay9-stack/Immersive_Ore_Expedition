package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;

import java.util.Objects;

/** Independent structural choice made once per miner-camp site after its quality roll. */
public enum ProspectorCampArchetype {
    ACTIVE,
    ABANDONED;

    static final long ARCHETYPE_SELECTION_SALT = 0xA6B4C2D8E19F7305L;

    public static ProspectorCampArchetype select(long planSeed, SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        int percentile = (int) Math.floorMod(
                ProspectorCampContext.mix64(planSeed ^ ARCHETYPE_SELECTION_SALT),
                100
        );
        return percentile < abandonedPercent(quality) ? ABANDONED : ACTIVE;
    }

    public static int abandonedPercent(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        return switch (quality) {
            case DRY -> 70;
            case POOR -> 45;
            case NORMAL -> 25;
            case RICH -> 15;
            case MOTHERLODE -> 10;
        };
    }
}
