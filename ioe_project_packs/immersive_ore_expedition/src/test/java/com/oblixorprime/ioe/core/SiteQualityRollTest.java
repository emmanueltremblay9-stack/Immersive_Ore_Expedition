package com.oblixorprime.ioe.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SiteQualityRollTest {
    @Test
    void defaultUsesCanonicalWeights() {
        SiteQualityRoll roll = SiteQualityRoll.DEFAULT;

        assertEquals(10, roll.dryWeight());
        assertEquals(25, roll.poorWeight());
        assertEquals(45, roll.normalWeight());
        assertEquals(17, roll.richWeight());
        assertEquals(3, roll.motherlodeWeight());
        assertEquals(100, roll.totalWeight());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 9, DRY",
            "10, 34, POOR",
            "35, 79, NORMAL",
            "80, 96, RICH",
            "97, 99, MOTHERLODE"
    })
    void defaultMapsEveryRollInCanonicalRanges(int first, int last, SiteQuality expected) {
        for (int value = first; value <= last; value++) {
            assertEquals(expected, SiteQualityRoll.DEFAULT.qualityAt(value), "roll=" + value);
        }
    }

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
