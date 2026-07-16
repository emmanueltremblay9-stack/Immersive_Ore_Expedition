package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
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

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void connectedBlueprintInvariants(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 90, 6);
        ResourceLocation ironOreId = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ore");
        ResourceLocation buddingAmethystId = ResourceLocation.fromNamespaceAndPath("minecraft", "budding_amethyst");
        for (ExpeditionSiteType type : ExpeditionSiteType.naturalSurfaceSites()) {
            ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                    type,
                    origin,
                    SiteQuality.NORMAL,
                    ironOreId,
                    Blocks.IRON_ORE.defaultBlockState(),
                    RandomSource.create(42L)
            );
            helper.assertTrue(plan.isConnectedExpeditionSite(), type.id() + " is not connected");
            helper.assertTrue(new HashSet<>(plan.generatedComponents()).equals(Set.of(
                    type.id(),
                    IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR,
                    IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER
            )), type.id() + " did not plan exactly one surface clue, connector, and chamber");
            helper.assertTrue(plan.oreBlockCount() == 8L,
                    type.id() + " normal-quality chamber did not contain eight sparse ore blocks");
        }

        ExpeditionSiteBlockPlan dry = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.COLLAPSED_SHAFT,
                origin,
                SiteQuality.DRY,
                null,
                null,
                RandomSource.create(7L)
        );
        helper.assertTrue(dry.isConnectedExpeditionSite(), "Dry site lost its connected route");
        helper.assertTrue(dry.oreBlockCount() == 0L, "Dry site generated ore");
        helper.assertFalse(dry.blocks().values().stream()
                        .anyMatch(state -> ORE_BLOCKS.contains(state.getBlock())),
                "Dry site block plan contains an ore block");

        for (int localX : new int[]{4, 11}) {
            ExpeditionSiteBlockPlan motherlode = ExpeditionSiteBlueprints.plan(
                    ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                    new BlockPos(localX, 90, 6),
                    SiteQuality.MOTHERLODE,
                    ironOreId,
                    Blocks.IRON_ORE.defaultBlockState(),
                    buddingAmethystId,
                    Blocks.BUDDING_AMETHYST.defaultBlockState(),
                    24,
                    4,
                    RandomSource.create(19L)
            );
            helper.assertTrue(motherlode.blocks().keySet().stream()
                            .allMatch(pos -> (pos.getX() >> 4) == 0 && (pos.getZ() >> 4) == 0),
                    "Motherlode plan crossed its safe source-chunk boundary at local X=" + localX);
            helper.assertTrue(motherlode.oreBlockCount() == 24L,
                    "Motherlode plan did not contain its sparse 24-block ore budget");
            helper.assertTrue(motherlode.oreNodeCount() == 4,
                    "Motherlode plan did not retain its four connected ore nodes");
            helper.assertTrue(motherlode.oreNodeHeartCount() == 4L,
                    "Motherlode plan did not retain one budding heart per ore node");
        }
        helper.assertTrue(ExpeditionSiteType.registeredFeatureIds().size() == 6,
                "The expedition-site catalog does not expose all six Feature ids");
        helper.assertTrue(Set.copyOf(ExpeditionSiteType.registeredFeatureIds())
                        .equals(Set.copyOf(ExpeditionStructureRegistry.enabledStructureIds())),
                "The registered Feature ids diverge from the configured expedition-site catalog");
        helper.succeed();
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
            helper.assertTrue(containsProductiveResourceNode(level, testChunk, origin, type),
                    type.id() + " did not place its connected ore-load chamber");
            if (type == ExpeditionSiteType.MINER_CAMP || type == ExpeditionSiteType.BURIED_SURVEY_MARKER) {
                assertSealedSurfaceHatch(helper, level, origin, type);
            }
        }
        helper.succeed();
    }

    private static void assertSealedSurfaceHatch(
            GameTestHelper helper,
            ServerLevel level,
            BlockPos origin,
            ExpeditionSiteType type
    ) {
        int hatchCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                var state = level.getBlockState(origin.offset(dx, 0, dz));
                helper.assertFalse(state.isAir(),
                        type.id() + " left an open surface cell around its shaft hatch");
                if (state.is(Blocks.OAK_TRAPDOOR)) {
                    hatchCount++;
                }
            }
        }
        helper.assertTrue(level.getBlockState(origin.offset(0, 0, 1)).is(Blocks.OAK_TRAPDOOR),
                type.id() + " hatch is not aligned above the ladder column");
        helper.assertTrue(hatchCount == 1, type.id() + " did not place exactly one surface hatch");
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
        helper.assertFalse(containsAnyProductiveResourceBlock(
                        level,
                        testChunk,
                        origin,
                        ExpeditionSiteType.ORE_LOAD_CHAMBER
                ),
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

    private static boolean containsProductiveResourceNode(
            ServerLevel level,
            ChunkPos chunk,
            BlockPos origin,
            ExpeditionSiteType type
    ) {
        return expectedProductiveResourceBlocks(level, origin, type)
                .map(blocks -> blocks.stream().allMatch(block -> containsBlock(level, chunk, block)))
                .orElse(false);
    }

    private static boolean containsAnyProductiveResourceBlock(
            ServerLevel level,
            ChunkPos chunk,
            BlockPos origin,
            ExpeditionSiteType type
    ) {
        return expectedProductiveResourceBlocks(level, origin, type)
                .map(blocks -> blocks.stream().anyMatch(block -> containsBlock(level, chunk, block)))
                .orElse(false);
    }

    private static Optional<Set<Block>> expectedProductiveResourceBlocks(
            ServerLevel level,
            BlockPos origin,
            ExpeditionSiteType type
    ) {
        return BiomeMineResourceProfile.resolve(level, origin).profile().flatMap(profile -> switch (
                profile.resourceKind()
        ) {
            case GEORE -> GeOreNodeIntegration.resolve(profile.profileName()).map(material -> Set.of(
                    material.nodeBlock(),
                    material.buddingBlock()
            ));
            case AE2_CERTUS -> Ae2MeteoriteIntegration.resolve().map(material -> Set.of(
                    material.buddingBlock(),
                    material.skyStoneBlock()
            ));
            case EXTENDEDAE_FLUIX -> ExtendedAeGeodeIntegration.resolve().map(material -> Set.of(
                    material.buddingBlock(),
                    material.shellBlock()
            ));
        });
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
