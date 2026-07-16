package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
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
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void ieDepositFailureUsesOnlyDowngradedQuality(GameTestHelper helper) {
        IoeSiteQualityFallbackResolver.Resolution resolution = IoeSiteQualityFallbackResolver.resolve(
                SiteQuality.MOTHERLODE,
                ignored -> true,
                quality -> quality == SiteQuality.MOTHERLODE
                        ? IoeSiteQualityFallbackResolver.DepositAttempt.FAILED
                        : IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED,
                (current, lower) -> true
        );
        helper.assertTrue(resolution.confirmed(), "The lower quality pipeline was unexpectedly rejected");
        helper.assertTrue(resolution.finalQuality() == SiteQuality.RICH,
                "A failed Mother deposit did not downgrade exactly to Rich/Major");

        BlockPos origin = new BlockPos(4, 90, 6);
        ResourceLocation ironOreId = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ore");
        ResourceLocation buddingAmethystId = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "budding_amethyst"
        );
        ExpeditionSiteBlockPlan finalPlan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                resolution.finalQuality(),
                ironOreId,
                Blocks.IRON_ORE.defaultBlockState(),
                buddingAmethystId,
                Blocks.BUDDING_AMETHYST.defaultBlockState(),
                14,
                3,
                RandomSource.create(19L)
        );
        helper.assertTrue(finalPlan.quality() == SiteQuality.RICH, "The blueprint retained Mother metadata");
        helper.assertTrue(finalPlan.oreBlockCount() == 14L, "The blueprint retained the Mother ore budget");
        helper.assertTrue(finalPlan.oreNodeCount() == 3, "The blueprint retained the Mother node count");

        ExpeditionSite finalSite = ExpeditionSite.anchor(
                helper.getLevel().dimension(),
                origin,
                finalPlan.requestedFeatureId(),
                ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "test_province"),
                finalPlan.quality(),
                "quality_downgrade_gametest"
        );
        ExpeditionLocatorService.record(helper.getLevel(), finalSite);
        List<ExpeditionSite> recorded = ExpeditionLocatorService.index(helper.getLevel()).sites().stream()
                .filter(site -> site.source().filter("quality_downgrade_gametest"::equals).isPresent())
                .filter(site -> site.pos().equals(origin))
                .toList();
        helper.assertTrue(recorded.size() == 1, "SavedData received more than one confirmation");
        helper.assertTrue(recorded.getFirst().quality().orElseThrow() == SiteQuality.RICH,
                "The locator retained the provisional Mother quality");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void nativeIeDepositReservationIsTransactional(GameTestHelper helper) {
        if (!ModList.get().isLoaded("immersiveengineering")) {
            helper.succeed();
            return;
        }

        BlockPos anchor = helper.absolutePos(new BlockPos(31, 10, 31));
        IoeExcavatorDepositRules.MotherDepositRequest request =
                new IoeExcavatorDepositRules.MotherDepositRequest(
                        anchor,
                        ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "test_province"),
                        "iron",
                        1
                );
        IoeMotherDepositReservation created = null;
        IoeMotherDepositReservation preparedDuplicate = null;
        IoeMotherDepositReservation existing = null;
        IoeMotherDepositReservation afterRollback = null;
        try {
            Class<?> bridge = Class.forName(
                    "com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge",
                    true,
                    ExpeditionWorldgenGameTests.class.getClassLoader()
            );
            Method reserve = bridge.getMethod(
                    "reserveGuaranteedDeposit",
                    ServerLevel.class,
                    IoeExcavatorDepositRules.MotherDepositRequest.class
            );
            Optional<?> first = (Optional<?>) reserve.invoke(null, helper.getLevel(), request);
            helper.assertTrue(first.isPresent(), "IE did not create the native Mother deposit reservation");
            created = (IoeMotherDepositReservation) first.orElseThrow();
            helper.assertTrue(created.createdByIoe(), "The first unique reservation was not created by IOE");

            Optional<?> second = (Optional<?>) reserve.invoke(null, helper.getLevel(), request);
            helper.assertTrue(second.isPresent(), "IE did not prepare an idempotent Mother reservation");
            preparedDuplicate = (IoeMotherDepositReservation) second.orElseThrow();
            helper.assertTrue(preparedDuplicate.createdByIoe(),
                    "A prepared reservation mutated the IE vein list before commit");
            preparedDuplicate.rollback();
            preparedDuplicate = null;

            created.commit();
            Optional<?> committedLookup = (Optional<?>) reserve.invoke(null, helper.getLevel(), request);
            helper.assertTrue(committedLookup.isPresent(), "IE did not find the committed Mother deposit");
            existing = (IoeMotherDepositReservation) committedLookup.orElseThrow();
            helper.assertFalse(existing.createdByIoe(), "The idempotent lookup created a duplicate IE vein");

            existing.rollback();
            existing = null;
            created.rollback();
            created = null;

            Optional<?> postRollbackLookup = (Optional<?>) reserve.invoke(null, helper.getLevel(), request);
            helper.assertTrue(postRollbackLookup.isPresent(), "IE could not prepare after compensation");
            afterRollback = (IoeMotherDepositReservation) postRollbackLookup.orElseThrow();
            helper.assertTrue(afterRollback.createdByIoe(),
                    "Compensation left the committed IE vein visible at the Mother anchor");
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not exercise the optional native IE reservation bridge", failure);
        } finally {
            if (afterRollback != null) {
                afterRollback.rollback();
            }
            if (existing != null) {
                existing.rollback();
            }
            if (preparedDuplicate != null) {
                preparedDuplicate.rollback();
            }
            if (created != null) {
                created.rollback();
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void dryFallbackConfirmsWithoutDeposit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(16, 24, 16)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);
        BiomeMineResourceProfile profile = BiomeMineResourceProfile.resolve(level, origin)
                .profile()
                .orElseThrow();
        ExpeditionSiteBlockPlan dryPlan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.COLLAPSED_SHAFT,
                origin,
                SiteQuality.DRY,
                null,
                null,
                RandomSource.create(71L)
        );
        IoePendingExpeditionSites.stage(level, dryPlan, profile);

        IoePendingExpeditionSites.Confirmation confirmation =
                IoePendingExpeditionSites.confirmLoadedChunk(level, testChunk);

        helper.assertTrue(confirmation.confirmedSites() == 1, "The Dry fallback was not confirmed");
        helper.assertTrue(confirmation.rejectedSites() == 0, "The Dry fallback was rejected");
        helper.assertTrue(confirmation.rejectedResourcePositions().isEmpty(),
                "The resource-free Dry fallback reported resource cleanup");
        helper.assertTrue(ExpeditionLocatorService.index(level).sites().stream()
                        .anyMatch(site -> site.pos().equals(origin)
                                && site.quality().filter(quality -> quality == SiteQuality.DRY).isPresent()),
                "SavedData did not retain the final Dry quality");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void invalidPendingSiteRollsBackCreatedDeposit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(16, 24, 16)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        BiomeMineResourceProfile profile = BiomeMineResourceProfile.resolve(level, origin)
                .profile()
                .orElseThrow();
        ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.RICH,
                ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ore"),
                Blocks.IRON_ORE.defaultBlockState(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "budding_amethyst"),
                Blocks.BUDDING_AMETHYST.defaultBlockState(),
                14,
                3,
                RandomSource.create(73L)
        );
        AtomicInteger rollbacks = new AtomicInteger();
        IoeMotherDepositReservation reservation = new IoeMotherDepositReservation() {
            @Override
            public boolean createdByIoe() {
                return true;
            }

            @Override
            public void commit() {
            }

            @Override
            public void rollback() {
                rollbacks.incrementAndGet();
            }
        };
        IoePendingExpeditionSites.stage(level, plan, profile, reservation);
        BlockPos blockedPos = plan.blocks().entrySet().stream()
                .filter(entry -> !entry.getValue().isAir())
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
        level.setBlock(blockedPos, Blocks.CHEST.defaultBlockState(), 2);

        IoePendingExpeditionSites.Confirmation confirmation =
                IoePendingExpeditionSites.confirmLoadedChunk(level, testChunk);

        helper.assertTrue(confirmation.confirmedSites() == 0, "The invalid pending site was confirmed");
        helper.assertTrue(confirmation.rejectedSites() == 1, "The invalid pending site was not rejected");
        helper.assertTrue(rollbacks.get() == 1, "The created IE reservation was not rolled back exactly once");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void ieCommitFailureRunsRichPipelineBeforeLocator(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(48, 24, 48)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);
        BiomeMineResourceProfile profile = BiomeMineResourceProfile.resolve(level, origin)
                .profile()
                .orElseThrow();
        ResourceLocation ironOreId = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ore");
        ResourceLocation buddingAmethystId = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "budding_amethyst"
        );
        ExpeditionSiteBlockPlan motherPlan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.MOTHERLODE,
                ironOreId,
                Blocks.IRON_ORE.defaultBlockState(),
                buddingAmethystId,
                Blocks.BUDDING_AMETHYST.defaultBlockState(),
                24,
                4,
                RandomSource.create(83L)
        );
        ExpeditionSiteBlockPlan richPlan = ExpeditionSiteBlueprints.plan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.RICH,
                ironOreId,
                Blocks.IRON_ORE.defaultBlockState(),
                buddingAmethystId,
                Blocks.BUDDING_AMETHYST.defaultBlockState(),
                14,
                3,
                RandomSource.create(89L)
        );
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        IoeMotherDepositReservation failingReservation = new IoeMotherDepositReservation() {
            @Override
            public boolean createdByIoe() {
                return true;
            }

            @Override
            public void commit() {
                commits.incrementAndGet();
                throw new IllegalStateException("forced IE commit failure");
            }

            @Override
            public void rollback() {
                rollbacks.incrementAndGet();
            }
        };
        helper.assertTrue(IoePendingExpeditionSites.stage(
                        level,
                        motherPlan,
                        profile,
                        failingReservation,
                        richPlan
                ),
                "The Mother plan was not staged for transactional confirmation");

        IoePendingExpeditionSites.Confirmation confirmation =
                IoePendingExpeditionSites.confirmLoadedChunk(level, testChunk);

        helper.assertTrue(confirmation.confirmedSites() == 1, "The Rich fallback was not confirmed");
        helper.assertTrue(confirmation.rejectedSites() == 0, "The Rich fallback was rejected");
        helper.assertTrue(commits.get() == 1, "The IE commit was not attempted exactly once");
        helper.assertTrue(rollbacks.get() == 1, "The failed IE reservation was not compensated exactly once");
        helper.assertTrue(richPlan.blocks().entrySet().stream()
                        .filter(entry -> !entry.getValue().isAir())
                        .allMatch(entry -> level.getBlockState(entry.getKey()).getBlock()
                                == entry.getValue().getBlock()),
                "The final world blocks do not match the Rich pipeline");
        helper.assertTrue(ExpeditionLocatorService.index(level).sites().stream()
                        .anyMatch(site -> site.pos().equals(origin)
                                && site.quality().filter(quality -> quality == SiteQuality.RICH).isPresent()),
                "The locator did not persist the final Rich quality");
        helper.assertFalse(ExpeditionLocatorService.index(level).sites().stream()
                        .anyMatch(site -> site.pos().equals(origin)
                                && site.quality().filter(quality -> quality == SiteQuality.MOTHERLODE).isPresent()),
                "The provisional Mother quality leaked into SavedData");
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
        if (type.naturalSurfaceSite()) {
            helper.assertFalse(containsBlock(level, testChunk, Blocks.LADDER),
                    type.id() + " wrote provisional mine blocks before final server-thread confirmation");
            helper.assertFalse(containsAnyProductiveResourceBlock(level, testChunk, origin, type),
                    type.id() + " wrote provisional resources before final server-thread confirmation");
            helper.assertFalse(ExpeditionLocatorService.index(level).sites().stream()
                            .anyMatch(site -> site.pos().equals(origin)),
                    type.id() + " wrote a provisional locator entry before final confirmation");
            IoePendingExpeditionSites.Confirmation confirmation =
                    IoePendingExpeditionSites.confirmLoadedChunk(level, testChunk);
            helper.assertTrue(confirmation.confirmedSites() == 1,
                    type.id() + " did not commit its final server-thread site transaction");
        }
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
