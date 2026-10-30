package com.oblixorprime.ioe.budding;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BuddingFoundationTest {
    @Test
    void canonicalRanksDegradeAndRestoreWithoutCrossingFlawed() {
        assertEquals(BuddingRank.CHIPPED, BuddingRank.FLAWED.degradedRank().orElseThrow());
        assertEquals(BuddingRank.DAMAGED, BuddingRank.CHIPPED.degradedRank().orElseThrow());
        assertTrue(BuddingRank.DAMAGED.degradedRank().isEmpty());
        assertEquals(
                ResourceLocation.fromNamespaceAndPath("geore", "iron_block"),
                BuddingResourceFamily.GEORE_IRON.storageBlockId()
        );

        assertEquals(BuddingRank.CHIPPED, BuddingRank.DAMAGED.restoredRank().orElseThrow());
        assertEquals(BuddingRank.FLAWED, BuddingRank.CHIPPED.restoredRank().orElseThrow());
        assertTrue(BuddingRank.FLAWED.restoredRank().isEmpty());
        assertEquals(BuddingRank.FLAWED, BuddingRank.MAXIMUM_RESTORABLE);

        assertFalse(BuddingRank.FLAWLESS.canGrowthDegrade());
        assertTrue(BuddingRank.FLAWLESS.degradedRank().isEmpty());
        assertTrue(BuddingRank.FLAWLESS.restoredRank().isEmpty());
    }

    @Test
    void certusUsesTheNativeAe2RankIds() {
        for (BuddingRank rank : BuddingRank.values()) {
            assertEquals(
                    ResourceLocation.fromNamespaceAndPath("ae2", rank.path() + "_budding_quartz"),
                    BuddingResourceFamily.CERTUS_QUARTZ.observedNativeBuddingId(rank).orElseThrow()
            );
        }
    }

    @Test
    void extendedAeRanksRemainExplicitlyUnsupported() {
        for (BuddingRank rank : BuddingRank.values()) {
            BuddingRankResolution resolution =
                    BuddingRankResolver.resolve(BuddingResourceFamily.ENTROIZED_FLUIX, rank);

            assertFalse(resolution.supported());
            assertEquals(BuddingRankResolution.Status.RANK_UNSUPPORTED, resolution.status());
            assertTrue(resolution.buddingBlock().isEmpty());
            assertTrue(resolution.buddingBlockId().isPresent());
            assertTrue(resolution.detail().contains(
                    "fully -> mostly -> half -> hardly -> ae2:quartz_block"
            ));
        }
    }

    @Test
    void geOreFamiliesProduceFiftyTwoUniqueIdsWithCompletePackagedData() {
        Set<String> ids = new HashSet<>();
        ClassLoader resources = BuddingFoundationTest.class.getClassLoader();
        int geOreFamilies = 0;
        int restorationRecipes = 0;

        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) {
                continue;
            }
            geOreFamilies++;
            for (BuddingRank rank : BuddingRank.values()) {
                String path = rank.path() + "_budding_" + family.key();
                String id = "immersive_ore_expedition:" + path;
                assertTrue(ids.add(id), () -> "duplicate Budding id: " + id);
                assertTrue(resourceExists(
                        resources,
                        "assets/immersive_ore_expedition/blockstates/" + path + ".json"
                ));
                assertTrue(resourceExists(
                        resources,
                        "assets/immersive_ore_expedition/models/block/" + path + ".json"
                ));
                assertTrue(resourceExists(
                        resources,
                        "assets/immersive_ore_expedition/models/item/" + path + ".json"
                ));
                assertTrue(resourceExists(
                        resources,
                        "data/immersive_ore_expedition/loot_table/blocks/" + path + ".json"
                ));
                if (EnumSet.of(
                        BuddingRank.DAMAGED,
                        BuddingRank.CHIPPED,
                        BuddingRank.FLAWED
                ).contains(rank)) {
                    assertTrue(resourceExists(
                            resources,
                            "data/immersive_ore_expedition/recipe/aggregator/" + path + ".json"
                    ));
                    restorationRecipes++;
                }
            }
        }

        assertEquals(13, geOreFamilies);
        assertEquals(52, ids.size());
        assertEquals(39, restorationRecipes);
    }

    private static boolean resourceExists(ClassLoader resources, String path) {
        return resources.getResource(path) != null;
    }
}
