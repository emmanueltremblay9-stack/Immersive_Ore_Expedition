package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;

import java.util.Objects;

/**
 * Bounded surface-design budgets for the prospector camp. The Motherlode target remains 17x17, while its
 * placed envelope is deliberately limited to 15x15 so the existing single-chunk transaction invariant holds.
 */
public record ProspectorCampQualitySpec(
        int targetFootprint,
        int placedFootprint,
        int maxHeight,
        int outcropWidth,
        int shelterWidth,
        int shelterDepth,
        int postHeight,
        int markerCount,
        int containerCount,
        int maxDomumAccents
) {
    public ProspectorCampQualitySpec {
        if (targetFootprint < 1 || placedFootprint < 1 || targetFootprint % 2 == 0 || placedFootprint % 2 == 0) {
            throw new IllegalArgumentException("Prospector-camp footprints must be positive odd numbers");
        }
        if (placedFootprint > targetFootprint || placedFootprint > 15) {
            throw new IllegalArgumentException("Placed camp footprints must fit the target and the anchor chunk");
        }
        if (maxHeight < 1 || outcropWidth < 1 || shelterWidth < 1 || shelterDepth < 1 || postHeight < 1) {
            throw new IllegalArgumentException("Prospector-camp dimensions must be positive");
        }
        if (markerCount < 0 || containerCount < 0 || containerCount > 2 || maxDomumAccents < 0) {
            throw new IllegalArgumentException("Prospector-camp detail budgets are invalid");
        }
    }

    public static ProspectorCampQualitySpec forQuality(SiteQuality quality) {
        Objects.requireNonNull(quality, "quality");
        return switch (quality) {
            case DRY -> new ProspectorCampQualitySpec(9, 9, 4, 3, 3, 2, 2, 1, 0, 0);
            case POOR -> new ProspectorCampQualitySpec(11, 11, 5, 4, 3, 3, 3, 2, 1, 7);
            case NORMAL -> new ProspectorCampQualitySpec(13, 13, 6, 5, 5, 3, 3, 3, 2, 12);
            case RICH -> new ProspectorCampQualitySpec(15, 15, 7, 6, 6, 4, 4, 4, 2, 18);
            case MOTHERLODE -> new ProspectorCampQualitySpec(17, 15, 8, 7, 7, 5, 5, 5, 2, 24);
        };
    }

    public int placedRadius() {
        return placedFootprint / 2;
    }

    public int biomeShelterDetailBlockCount(ProspectorCampVisualFamily visualFamily) {
        Objects.requireNonNull(visualFamily, "visualFamily");
        return switch (visualFamily) {
            case TEMPERATE, WETLAND -> 0;
            case CONIFER -> shelterWidth + shelterDepth - 3;
            case SNOWY, VOLCANIC -> shelterWidth - 1;
            case TROPICAL -> (shelterWidth - 1) / 2;
            case ARID -> shelterWidth - 2;
            case ROCKY -> 2 * Math.min(2, shelterWidth - 2);
            case AQUATIC -> throw new IllegalArgumentException("Aquatic biomes do not receive a camp shelter");
        };
    }

    public int shelterShellBlockCount(
            boolean raisedShelter,
            ProspectorCampVisualFamily visualFamily
    ) {
        int floorAndRoof = shelterWidth * shelterDepth * 2;
        int verticalPosts = 4 * (postHeight - (raisedShelter ? 1 : 0));
        int frontBeamWithoutPostOverlap = Math.max(0, shelterWidth - 2);
        int openHeadroomBay = requiresOpenRaisedHeadroomBay(raisedShelter) ? 2 : 0;
        return floorAndRoof + verticalPosts + frontBeamWithoutPostOverlap - openHeadroomBay
                + biomeShelterDetailBlockCount(visualFamily);
    }

    public boolean requiresOpenRaisedHeadroomBay(boolean raisedShelter) {
        return raisedShelter && postHeight < 3;
    }
}
