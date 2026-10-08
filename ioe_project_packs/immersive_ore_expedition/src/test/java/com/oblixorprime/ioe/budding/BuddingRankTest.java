package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BuddingRankTest {
    @Test
    void restoresDamagedThroughChippedToFlawed() {
        BuddingRank firstRestoration = BuddingRank.DAMAGED.restoredRank().orElseThrow();
        assertEquals(BuddingRank.CHIPPED, firstRestoration);

        BuddingRank secondRestoration = firstRestoration.restoredRank().orElseThrow();
        assertEquals(BuddingRank.FLAWED, secondRestoration);
    }

    @Test
    void doesNotRestoreFlawedOrFlawless() {
        assertTrue(BuddingRank.FLAWED.restoredRank().isEmpty(), "Flawed must not restore to Flawless");
        assertTrue(BuddingRank.FLAWLESS.restoredRank().isEmpty(), "Flawless has no restoration target");
    }

    @Test
    void maximumRestorableRankIsFlawed() {
        assertEquals(BuddingRank.FLAWED, BuddingRank.MAXIMUM_RESTORABLE);
    }
}
