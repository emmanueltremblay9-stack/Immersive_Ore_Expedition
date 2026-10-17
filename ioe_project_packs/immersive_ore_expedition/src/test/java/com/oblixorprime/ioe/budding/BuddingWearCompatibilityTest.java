package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BuddingWearCompatibilityTest {
    @Test void fourWearingHeartsReachStorageWithoutPassingThroughPermanentFlawless() {
        var expected = List.of(BuddingRank.FULLY, BuddingRank.FLAWED, BuddingRank.CHIPPED, BuddingRank.DAMAGED);
        var rank = BuddingRank.FULLY;
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), rank);
            assertTrue(rank.canGrowthDegrade());
            if (i < expected.size() - 1) rank = rank.degradedRank().orElseThrow();
            else assertTrue(rank.degradedRank().isEmpty());
        }
        assertFalse(BuddingRank.FLAWLESS.canGrowthDegrade());
        assertTrue(BuddingRank.FLAWLESS.degradedRank().isEmpty());
    }
    @Test void existingNamesOrdinalsAndRestorationCeilingAreStable() {
        var old = List.of("damaged", "chipped", "flawed", "flawless");
        for (int i = 0; i < old.size(); i++) assertEquals(old.get(i), BuddingRank.values()[i].path());
        assertEquals(BuddingRank.FLAWED, BuddingRank.MAXIMUM_RESTORABLE);
        assertTrue(BuddingRank.FULLY.restoredRank().isEmpty());
        assertTrue(BuddingRank.FLAWED.restoredRank().isEmpty());
    }
    @Test void existingSiteBudgetsDoNotStartPlacingFully() {
        for (var quality : com.oblixorprime.ioe.core.SiteQuality.values()) {
            var plan = BuddingSitePlan.forQuality(quality, net.minecraft.util.RandomSource.create(64), 0);
            assertFalse(plan.nodeRanks().contains(BuddingRank.FULLY));
        }
    }
    @Test void nativeCertusAndEntroDoNotInventMissingPhases() {
        assertEquals(4, NativeCertusBudding.ranks().size());
        assertFalse(NativeCertusBudding.ranks().contains(BuddingRank.FULLY));
        assertTrue(BuddingResourceFamily.CERTUS_QUARTZ.observedNativeBuddingId(BuddingRank.FULLY).isEmpty());
        assertTrue(BuddingResourceFamily.ENTROIZED_FLUIX.observedNativeBuddingId(BuddingRank.FLAWLESS).isEmpty());
        assertEquals("extendedae:entro_budding_fully", BuddingResourceFamily.ENTROIZED_FLUIX
                .observedNativeBuddingId(BuddingRank.FULLY).orElseThrow().toString());
    }
}
