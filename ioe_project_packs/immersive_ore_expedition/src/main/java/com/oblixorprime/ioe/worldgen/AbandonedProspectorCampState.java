package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;

import java.util.List;
import java.util.Objects;

/** Narrative condition for the abandoned archetype; distinct from Surface 01's visual wear states. */
public enum AbandonedProspectorCampState {
    RECENTLY_ABANDONED,
    WEATHERED,
    COLLAPSED,
    FAILED_PROSPECTION,
    EVACUATED;

    static final long ABANDONED_STATE_SELECTION_SALT = 0x7C3A95E6B1D24F08L;

    public static AbandonedProspectorCampState select(long planSeed, SiteQuality quality) {
        List<AbandonedProspectorCampState> eligible = eligibleFor(quality);
        int index = (int) Math.floorMod(
                ProspectorCampContext.mix64(planSeed ^ ABANDONED_STATE_SELECTION_SALT),
                eligible.size()
        );
        return eligible.get(index);
    }

    public static List<AbandonedProspectorCampState> eligibleFor(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        return switch (quality) {
            case DRY -> List.of(FAILED_PROSPECTION, WEATHERED, COLLAPSED);
            case POOR -> List.of(FAILED_PROSPECTION, WEATHERED, COLLAPSED, RECENTLY_ABANDONED);
            case NORMAL, RICH -> List.of(RECENTLY_ABANDONED, WEATHERED, COLLAPSED, EVACUATED);
            case MOTHERLODE -> List.of(RECENTLY_ABANDONED, COLLAPSED, EVACUATED);
        };
    }
}
