package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BuddingSitePlanTest {
    @ParameterizedTest
    @CsvSource({
            "POOR, 3, 4, 12, DAMAGED",
            "NORMAL, 4, 5, 20, CHIPPED",
            "RICH, 5, 6, 30, FLAWED",
            "MOTHERLODE, 7, 7, 49, FLAWED"
    })
    void matchesCanonicalProductiveBudgets(SiteQuality quality, int nodes, int orePerNode,
                                          int totalOre, BuddingRank rank) {
        ScriptedRandom random = new ScriptedRandom(777);
        BuddingSitePlan plan = BuddingSitePlan.forQuality(quality, random, 5);

        assertEquals(quality, plan.quality());
        assertEquals(nodes, plan.nodeRanks().size());
        assertTrue(plan.nodeRanks().stream().allMatch(actual -> actual == rank));
        assertEquals(orePerNode, plan.oreBlocksPerNode());
        assertEquals(totalOre, plan.totalOreBlocks(), "Budding hearts are not ore blocks");
        assertEquals(0, plan.residualOreBlocks());
        assertEquals(quality == SiteQuality.MOTHERLODE ? List.of(10_000) : List.of(), random.bounds);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void dryKeepsTheExplicitResidualBudgetWithoutNodesOrRandomDraws(int residualOre) {
        ScriptedRandom random = new ScriptedRandom();
        BuddingSitePlan plan = BuddingSitePlan.forQuality(SiteQuality.DRY, random, residualOre);

        assertTrue(plan.nodeRanks().isEmpty());
        assertEquals(0, plan.oreBlocksPerNode());
        assertEquals(residualOre, plan.residualOreBlocks());
        assertEquals(residualOre, plan.totalOreBlocks());
        assertTrue(random.bounds.isEmpty());
    }

    @Test
    void exactly777Of10000MotherlodeRollsReplaceOneFlawedNode() {
        int successfulSites = 0;
        for (int roll = 0; roll < 10_000; roll++) {
            ScriptedRandom random = new ScriptedRandom(roll, 3);
            BuddingSitePlan plan = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, random, 0);
            long flawless = plan.nodeRanks().stream().filter(rank -> rank == BuddingRank.FLAWLESS).count();
            assertEquals(roll < 777 ? 1L : 0L, flawless, "roll=" + roll);
            assertEquals(7, plan.nodeRanks().size());
            assertEquals(49, plan.totalOreBlocks());
            assertEquals(7 - flawless,
                    plan.nodeRanks().stream().filter(rank -> rank == BuddingRank.FLAWED).count());
            assertEquals(roll < 777 ? List.of(10_000, 7) : List.of(10_000), random.bounds);
            successfulSites += (int) flawless;
        }
        assertEquals(777, successfulSites);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void anyOfTheSevenNodesCanBeTheSingleFlawlessReplacement(int index) {
        BuddingSitePlan plan = BuddingSitePlan.forQuality(
                SiteQuality.MOTHERLODE, new ScriptedRandom(776, index), 0);
        for (int node = 0; node < 7; node++) {
            assertEquals(node == index ? BuddingRank.FLAWLESS : BuddingRank.FLAWED, plan.nodeRanks().get(node));
        }
    }

    @Test
    void fallbackDiscardsFlawlessAndRebuildsEachLowerBudgetWithoutAnotherLottery() {
        BuddingSitePlan original = BuddingSitePlan.forQuality(
                SiteQuality.MOTHERLODE, new ScriptedRandom(0, 6), 0);
        BuddingSitePlan rich = original.downgradeTo(SiteQuality.RICH, 0);
        BuddingSitePlan normal = rich.downgradeTo(SiteQuality.NORMAL, 0);
        BuddingSitePlan poor = normal.downgradeTo(SiteQuality.POOR, 0);
        BuddingSitePlan dry = poor.downgradeTo(SiteQuality.DRY, 5);

        assertEquals(List.of(30, 20, 12, 5),
                List.of(rich.totalOreBlocks(), normal.totalOreBlocks(), poor.totalOreBlocks(), dry.totalOreBlocks()));
        assertEquals(List.of(5, 4, 3, 0),
                List.of(rich.nodeRanks().size(), normal.nodeRanks().size(), poor.nodeRanks().size(), dry.nodeRanks().size()));
        assertTrue(rich.nodeRanks().stream().allMatch(rank -> rank == BuddingRank.FLAWED));
        assertTrue(normal.nodeRanks().stream().allMatch(rank -> rank == BuddingRank.CHIPPED));
        assertTrue(poor.nodeRanks().stream().allMatch(rank -> rank == BuddingRank.DAMAGED));
        assertEquals(BuddingRank.FLAWLESS, original.nodeRanks().get(6), "Fallback must not mutate its source");
        assertEquals(normal.nodeRanks(), original.downgradeTo(SiteQuality.NORMAL, 0).nodeRanks());
    }

    @Test
    void naturalQualitySelectionUsesAllCanonicalWeights() {
        int[] counts = new int[SiteQuality.values().length];
        for (int roll = 0; roll < 100; roll++) {
            ScriptedRandom random = new ScriptedRandom(roll, 777);
            BuddingSitePlan plan = BuddingSitePlan.roll(random, 2);
            counts[plan.quality().ordinal()]++;
            assertEquals(plan.quality() == SiteQuality.MOTHERLODE ? List.of(100, 10_000) : List.of(100), random.bounds);
        }
        assertArrayEquals(new int[]{10, 25, 45, 17, 3}, counts);
    }

    @Test
    void plansAreRepeatableForTheSameSeed() {
        RandomSource first = RandomSource.create(1234567);
        RandomSource second = RandomSource.create(1234567);
        for (int site = 0; site < 100; site++) {
            BuddingSitePlan a = BuddingSitePlan.roll(first, 3);
            BuddingSitePlan b = BuddingSitePlan.roll(second, 3);
            assertEquals(a.quality(), b.quality());
            assertEquals(a.nodeRanks(), b.nodeRanks());
            assertEquals(a.totalOreBlocks(), b.totalOreBlocks());
        }
    }

    @Test
    void dryCannotFallBackAndInvalidTargetsDoNotChangeTheSource() {
        BuddingSitePlan dry = BuddingSitePlan.forQuality(SiteQuality.DRY, new ScriptedRandom(), 4);
        for (SiteQuality target : SiteQuality.values()) {
            assertThrows(IllegalArgumentException.class, () -> dry.downgradeTo(target, 0));
        }
        assertEquals(4, dry.totalOreBlocks());
        assertTrue(dry.nodeRanks().isEmpty());
    }

    @Test
    void rejectsInvalidBudgetsBeforeConsumingRandomnessAndRejectsUpgrades() {
        ScriptedRandom random = new ScriptedRandom();
        for (int invalid : new int[]{-1, 6}) {
            assertThrows(IllegalArgumentException.class, () -> BuddingSitePlan.roll(random, invalid));
            assertThrows(IllegalArgumentException.class,
                    () -> BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, random, invalid));
        }
        assertTrue(random.bounds.isEmpty());
        BuddingSitePlan plan = BuddingSitePlan.forQuality(SiteQuality.NORMAL, random, 0);
        for (SiteQuality invalid : List.of(SiteQuality.NORMAL, SiteQuality.RICH, SiteQuality.MOTHERLODE)) {
            assertThrows(IllegalArgumentException.class, () -> plan.downgradeTo(invalid, 0));
        }
        assertThrows(IllegalArgumentException.class, () -> plan.downgradeTo(SiteQuality.DRY, 6));
        assertThrows(UnsupportedOperationException.class, () -> plan.nodeRanks().set(0, BuddingRank.FLAWLESS));
    }

    private static final class ScriptedRandom extends LegacyRandomSource {
        private final int[] values;
        private final List<Integer> bounds = new ArrayList<>();
        private int cursor;

        private ScriptedRandom(int... values) {
            super(0);
            this.values = Arrays.copyOf(values, values.length);
        }

        @Override
        public int nextInt(int bound) {
            bounds.add(bound);
            assertTrue(cursor < values.length, "Unexpected random draw");
            int value = values[cursor++];
            assertTrue(value >= 0 && value < bound, "Invalid scripted random value");
            return value;
        }
    }
}
