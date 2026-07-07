package com.oblixorprime.ioe.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SiteQualityRollTest {
    @Test
    void mapsWeightedRangesDeterministically() {
        SiteQualityRoll roll = new SiteQualityRoll(1, 1, 1, 1, 1);

        assertEquals(SiteQuality.DRY, roll.qualityAt(0));
        assertEquals(SiteQuality.POOR, roll.qualityAt(1));
        assertEquals(SiteQuality.NORMAL, roll.qualityAt(2));
        assertEquals(SiteQuality.RICH, roll.qualityAt(3));
        assertEquals(SiteQuality.MOTHERLODE, roll.qualityAt(4));
    }

    @Test
    void rejectsInvalidWeightsAndOutOfRangeRolls() {
        assertThrows(IllegalArgumentException.class, () -> new SiteQualityRoll(0, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new SiteQualityRoll(-1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SiteQualityRoll(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                0,
                0
        ));
        assertThrows(IllegalArgumentException.class, () -> SiteQualityRoll.DEFAULT.qualityAt(-1));
        assertThrows(IllegalArgumentException.class, () -> SiteQualityRoll.DEFAULT.qualityAt(SiteQualityRoll.DEFAULT.totalWeight()));
    }

    @Test
    void dryIsTheOnlyNonProductiveQuality() {
        assertTrue(SiteQuality.POOR.isProductive());
        assertTrue(SiteQuality.NORMAL.isProductive());
        assertTrue(SiteQuality.RICH.isProductive());
        assertTrue(SiteQuality.MOTHERLODE.isProductive());
    }
}
