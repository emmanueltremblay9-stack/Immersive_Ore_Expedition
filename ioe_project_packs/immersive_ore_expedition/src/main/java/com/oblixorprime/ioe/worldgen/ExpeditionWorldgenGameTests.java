package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.Set;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class ExpeditionWorldgenGameTests {
    private static final String TEMPLATE = "expedition_worldgen_empty";
    private static final long PRODUCTIVE_SEED = findProductiveSeed();
    private static final Set<Block> ORE_BLOCKS = Set.of(
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.COPPER_ORE,
            Blocks.GOLD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE
    );

    private ExpeditionWorldgenGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void tinyVerticalMineEntrance(GameTestHelper helper) {
        proveFeaturePlacement(helper, ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void collapsedShaft(GameTestHelper helper) {
        proveFeaturePlacement(helper, ExpeditionSiteType.COLLAPSED_SHAFT);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void minerCamp(GameTestHelper helper) {
        proveFeaturePlacement(helper, ExpeditionSiteType.MINER_CAMP);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void buriedSurveyMarker(GameTestHelper helper) {
        proveFeaturePlacement(helper, ExpeditionSiteType.BURIED_SURVEY_MARKER);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void basicMineshaftConnector(GameTestHelper helper) {
        proveFeaturePlacement(helper, ExpeditionSiteType.BASIC_MINESHAFT_CONNECTOR);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void oreLoadChamber(GameTestHelper helper) {
        proveStandaloneChamberGuard(helper);
    }

    private static void proveFeaturePlacement(GameTestHelper helper, ExpeditionSiteType type) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(16, 24, 16)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);

        FeaturePlaceContext<NoneFeatureConfiguration> context = new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                RandomSource.create(PRODUCTIVE_SEED),
                origin,
                NoneFeatureConfiguration.INSTANCE
        );
        boolean placed = new ExpeditionSiteFeature(type).place(context);

        helper.assertTrue(placed, type.id() + " refused its production Feature.place GameTest placement");
        helper.assertTrue(
                containsBlock(level, testChunk, characteristicBlock(type)),
                type.id() + " did not place its characteristic block"
        );
        if (type.naturalSurfaceSite()) {
            helper.assertTrue(containsBlock(level, testChunk, Blocks.LADDER),
                    type.id() + " did not place a connected mineshaft ladder");
            helper.assertTrue(containsAnyBlock(level, testChunk, ORE_BLOCKS),
                    type.id() + " did not place its connected ore-load chamber");
        }
        helper.succeed();
    }

    private static void proveStandaloneChamberGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(16, 24, 16)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 24, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);
        FeaturePlaceContext<NoneFeatureConfiguration> context = new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                RandomSource.create(PRODUCTIVE_SEED),
                origin,
                NoneFeatureConfiguration.INSTANCE
        );

        boolean placed = new ExpeditionSiteFeature(ExpeditionSiteType.ORE_LOAD_CHAMBER).place(context);

        helper.assertFalse(placed, "Standalone ore-load chamber bypassed the required anchor policy");
        helper.assertFalse(containsAnyBlock(level, testChunk, ORE_BLOCKS),
                "Standalone ore-load chamber placed ore despite the required anchor policy");
        helper.succeed();
    }

    private static void fillTestChunk(ServerLevel level, ChunkPos chunk) {
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int y = 4; y <= 40; y++) {
                for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static boolean containsBlock(ServerLevel level, ChunkPos chunk, Block block) {
        return containsAnyBlock(level, chunk, Set.of(block));
    }

    private static boolean containsAnyBlock(ServerLevel level, ChunkPos chunk, Set<Block> blocks) {
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int y = 0; y <= 47; y++) {
                for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                    if (blocks.contains(level.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Block characteristicBlock(ExpeditionSiteType type) {
        return switch (type) {
            case TINY_VERTICAL_MINE_ENTRANCE -> Blocks.LANTERN;
            case COLLAPSED_SHAFT -> Blocks.TUFF;
            case MINER_CAMP -> Blocks.CAMPFIRE;
            case BURIED_SURVEY_MARKER -> Blocks.CHISELED_STONE_BRICKS;
            case BASIC_MINESHAFT_CONNECTOR -> Blocks.LADDER;
            case ORE_LOAD_CHAMBER -> Blocks.CALCITE;
        };
    }

    private static long findProductiveSeed() {
        for (long seed = 1L; seed < 10_000L; seed++) {
            if (SiteQualityRoll.DEFAULT.roll(RandomSource.create(seed)).isProductive()) {
                return seed;
            }
        }
        throw new IllegalStateException("Could not find a deterministic productive site-quality seed");
    }
}
