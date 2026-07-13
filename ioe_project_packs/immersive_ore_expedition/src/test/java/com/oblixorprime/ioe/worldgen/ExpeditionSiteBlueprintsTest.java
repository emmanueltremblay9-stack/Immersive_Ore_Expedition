package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionSiteBlueprintsTest {
    private static final ResourceLocation IRON_ORE_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ore");

    @Test
    void everySurfaceClueBuildsOneConnectedConnectorAndChamberSite() {
        for (ExpeditionSiteType type : ExpeditionSiteType.naturalSurfaceSites()) {
            ExpeditionSiteBlockPlan plan = plan(type, SiteQuality.NORMAL, 42L);

            assertTrue(plan.isConnectedExpeditionSite(), type::name);
            assertEquals(
                    Set.of(type.id(), IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR,
                            IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER),
                    new HashSet<>(plan.generatedComponents()),
                    type::name
            );
            assertEquals(8L, plan.oreBlockCount(), type::name);
            assertTrue(plan.blocks().containsValue(Blocks.LADDER.defaultBlockState()), type::name);
        }
    }

    @Test
    void surfaceCluesHaveDistinctCharacteristicBlocks() {
        assertContainsBlock(plan(ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE, SiteQuality.NORMAL, 1L), Blocks.LANTERN);
        assertContainsBlock(plan(ExpeditionSiteType.COLLAPSED_SHAFT, SiteQuality.NORMAL, 1L), Blocks.TUFF);
        assertContainsBlock(plan(ExpeditionSiteType.MINER_CAMP, SiteQuality.NORMAL, 1L), Blocks.CAMPFIRE);
        assertContainsBlock(plan(ExpeditionSiteType.BURIED_SURVEY_MARKER, SiteQuality.NORMAL, 1L),
                Blocks.CHISELED_STONE_BRICKS);
    }

    @Test
    void campAndSurveyMarkerSealTheSurfaceShaftAroundOneAccessibleHatch() {
        BlockPos origin = new BlockPos(4, 80, 6);
        for (ExpeditionSiteType type : new ExpeditionSiteType[]{
                ExpeditionSiteType.MINER_CAMP,
                ExpeditionSiteType.BURIED_SURVEY_MARKER
        }) {
            ExpeditionSiteBlockPlan plan = plan(type, SiteQuality.NORMAL, 1L);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos surfacePos = origin.offset(dx, 0, dz);
                    assertTrue(plan.blocks().containsKey(surfacePos), type::name);
                    assertFalse(plan.blocks().get(surfacePos).isAir(), type::name);
                }
            }
            assertEquals(Blocks.OAK_TRAPDOOR,
                    plan.blocks().get(origin.offset(0, 0, 1)).getBlock(), type::name);
            assertEquals(1L, plan.blocks().values().stream()
                    .filter(state -> state.is(Blocks.OAK_TRAPDOOR))
                    .count(), type::name);
        }
    }

    @Test
    void drySiteKeepsTheRouteAndChamberButContainsNoOre() {
        ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.COLLAPSED_SHAFT,
                new BlockPos(4, 80, 6),
                SiteQuality.DRY,
                null,
                null,
                RandomSource.create(7L)
        );

        assertTrue(plan.isConnectedExpeditionSite());
        assertEquals(0L, plan.oreBlockCount());
        assertFalse(plan.blocks().containsValue(Blocks.IRON_ORE.defaultBlockState()));
    }

    @Test
    void motherlodePlanRemainsInsideItsSourceChunkAtBothSafeEdges() {
        for (int localX : new int[]{4, 11}) {
            ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                    ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                    new BlockPos(localX, 90, 6),
                    SiteQuality.MOTHERLODE,
                    IRON_ORE_ID,
                    Blocks.IRON_ORE.defaultBlockState(),
                    RandomSource.create(19L)
            );

            assertTrue(plan.blocks().keySet().stream().allMatch(pos -> (pos.getX() >> 4) == 0));
            assertTrue(plan.blocks().keySet().stream().allMatch(pos -> (pos.getZ() >> 4) == 0));
            assertEquals(24L, plan.oreBlockCount());
        }
    }

    @Test
    void allSixCatalogIdsHaveConcreteFeatureTypes() {
        assertEquals(6, ExpeditionSiteType.registeredFeatureIds().size());
        assertEquals(Set.copyOf(ExpeditionStructureRegistry.enabledStructureIds()),
                Set.copyOf(ExpeditionSiteType.registeredFeatureIds()));
    }

    private static ExpeditionSiteBlockPlan plan(ExpeditionSiteType type, SiteQuality quality, long seed) {
        return ExpeditionSiteBlueprints.plan(
                type,
                new BlockPos(4, 80, 6),
                quality,
                IRON_ORE_ID,
                Blocks.IRON_ORE.defaultBlockState(),
                RandomSource.create(seed)
        );
    }

    private static void assertContainsBlock(ExpeditionSiteBlockPlan plan, Block block) {
        ResourceLocation expectedId = BuiltInRegistries.BLOCK.getKey(block);
        assertTrue(plan.blocks().values().stream()
                .map(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()))
                .anyMatch(expectedId::equals));
    }
}
