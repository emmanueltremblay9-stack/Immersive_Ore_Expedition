package com.oblixorprime.ioe.worldgen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class ExpeditionWorldgenGameTests {
    private static final String TEMPLATE = "expedition_worldgen_empty";
    private static final long PRODUCTIVE_SEED = findProductiveSeed();
    private static final long[] BIOME_FREQUENCY_SEEDS = {
            104729L, 130363L, 155921L, 181081L, 206369L,
            232003L, 257591L, 283009L, 308411L, 334021L
    };
    private static final int[] BIOME_SAMPLE_HEIGHTS = {-32, 64, 128};
    private static final int BIOME_SAMPLE_GRID_SIDE = 80;
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
        for (ExpeditionSiteType type : ExpeditionSiteType.naturalSurfaceSites()) {
            ExpeditionSiteBlockPlan plan = structureOnlyPlan(
                    type,
                    origin,
                    SiteQuality.NORMAL,
                    42L
            );
            helper.assertTrue(plan.isConnectedExpeditionSite(), type.id() + " is not connected");
            helper.assertTrue(new HashSet<>(plan.generatedComponents()).equals(Set.of(
                    type.id(),
                    IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR,
                    IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER
            )), type.id() + " did not plan exactly one surface clue, connector, and chamber");
            helper.assertTrue(plan.oreBlockCount() == 0L && plan.oreNodeCount() == 0,
                    type.id() + " embedded a forbidden free resource in its structural chamber");
            helper.assertTrue(plan.roomCenters().size() == 3,
                    type.id() + " did not create the normal-quality three-room mine");
            assertRoomReachability(helper, plan);
            helper.assertTrue(plan.blocks().values().stream().anyMatch(state -> state.is(Blocks.CARTOGRAPHY_TABLE)),
                    type.id() + " did not create a survey room or village supply desk");
            for (int depth = 1; depth <= origin.getY() - plan.connectorEnd().getY(); depth++) {
                BlockPos shaftCell = origin.below(depth);
                var shaftState = plan.blocks().get(shaftCell);
                helper.assertTrue(shaftState != null && shaftState.isAir(),
                        type.id() + " blocked its vertical shaft at " + shaftCell);
            }
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
            for (int localZ : new int[]{6, 9}) {
                BlockPos qualityOrigin = new BlockPos(localX, 90, localZ);
                for (SiteQuality quality : SiteQuality.values()) {
                    ExpeditionSiteBlockPlan plan = structureOnlyPlan(
                            ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                            qualityOrigin,
                            quality,
                            19L
                    );
                    helper.assertTrue(plan.blocks().keySet().stream()
                                    .allMatch(pos -> (pos.getX() >> 4) == 0 && (pos.getZ() >> 4) == 0),
                            quality + " plan crossed its safe source-chunk boundary at "
                                    + localX + "," + localZ);
                    helper.assertTrue(plan.oreBlockCount() == 0L && plan.oreNodeCount() == 0,
                            quality + " structure-only plan embedded a forbidden free resource");
                    helper.assertTrue(plan.roomCenters().size() == expectedRoomCount(quality),
                            quality + " planned the wrong number of expedition rooms");
                    helper.assertTrue(plan.blocks().size() <= 2_500,
                            quality + " exceeded the 2,500-block transaction cap");
                    helper.assertTrue(surfaceSpan(plan, qualityOrigin, true) >= 12,
                            quality + " surface outpost is not wide enough");
                    helper.assertTrue(surfaceSpan(plan, qualityOrigin, false) >= 10,
                            quality + " surface outpost is not deep enough");
                    helper.assertTrue(surfaceStructureBlockCount(plan, qualityOrigin) >= 60,
                            quality + " surface outpost is still too small");
                    helper.assertTrue(plan.blocks().values().stream()
                                    .anyMatch(state -> state.is(Blocks.YELLOW_TERRACOTTA)),
                            quality + " surface outpost is missing its village-route marker");
                    assertRoomReachability(helper, plan);
                }
            }
        }
        for (SiteQuality quality : SiteQuality.values()) {
            assertEmbeddedOreBudget(helper, quality);
        }
        ExpeditionSiteBlockPlan deterministicFirst = structureOnlyPlan(
                ExpeditionSiteType.MINER_CAMP,
                origin,
                SiteQuality.MOTHERLODE,
                211L
        );
        ExpeditionSiteBlockPlan deterministicSecond = structureOnlyPlan(
                ExpeditionSiteType.MINER_CAMP,
                origin,
                SiteQuality.MOTHERLODE,
                211L
        );
        helper.assertTrue(deterministicFirst.blocks().equals(deterministicSecond.blocks())
                        && deterministicFirst.roomCenters().equals(deterministicSecond.roomCenters()),
                "Equal seeds did not produce an equal expedition layout");
        helper.assertTrue(ExpeditionSiteType.registeredFeatureIds().size() == 6,
                "The expedition-site catalog does not expose all six Feature ids");
        helper.assertTrue(Set.copyOf(ExpeditionSiteType.registeredFeatureIds())
                        .equals(Set.copyOf(ExpeditionStructureRegistry.enabledStructureIds())),
                "The registered Feature ids diverge from the configured expedition-site catalog");
        helper.succeed();
    }

    private static void assertEmbeddedOreBudget(GameTestHelper helper, SiteQuality quality) {
        int oreBudget = switch (quality) {
            case DRY -> 0;
            case POOR -> 4;
            case NORMAL -> 8;
            case RICH -> 14;
            case MOTHERLODE -> 24;
        };
        int nodeBudget = switch (quality) {
            case DRY -> 0;
            case POOR -> 1;
            case NORMAL -> 2;
            case RICH -> 3;
            case MOTHERLODE -> 4;
        };
        ExpeditionSiteBlockPlan plan;
        if (quality == SiteQuality.DRY) {
            plan = ExpeditionSiteBlueprints.plan(
                    ExpeditionSiteType.ORE_LOAD_CHAMBER,
                    new BlockPos(8, 40, 8),
                    quality,
                    null,
                    null,
                    RandomSource.create(307L)
            );
        } else {
            ResourceLocation ironOreId = BuiltInRegistries.BLOCK.getKey(Blocks.IRON_ORE);
            plan = ExpeditionSiteBlueprints.plan(
                    ExpeditionSiteType.ORE_LOAD_CHAMBER,
                    new BlockPos(8, 40, 8),
                    quality,
                    ironOreId,
                    Blocks.IRON_ORE.defaultBlockState(),
                    oreBudget,
                    nodeBudget,
                    RandomSource.create(307L)
            );
        }
        helper.assertTrue(plan.oreBlockCount() == oreBudget,
                quality + " changed the established ore budget");
        helper.assertTrue(plan.oreNodeCount() == nodeBudget,
                quality + " changed the established ore-node budget");
    }

    private static int expectedRoomCount(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 2;
            case NORMAL, RICH -> 3;
            case MOTHERLODE -> 4;
        };
    }

    private static int surfaceSpan(
            ExpeditionSiteBlockPlan plan,
            BlockPos origin,
            boolean xAxis
    ) {
        var summary = plan.blocks().keySet().stream()
                .filter(pos -> pos.getY() >= origin.getY() - 1)
                .mapToInt(pos -> xAxis ? pos.getX() : pos.getZ())
                .summaryStatistics();
        return summary.getCount() == 0 ? 0 : summary.getMax() - summary.getMin() + 1;
    }

    private static long surfaceStructureBlockCount(ExpeditionSiteBlockPlan plan, BlockPos origin) {
        return plan.blocks().entrySet().stream()
                .filter(entry -> entry.getKey().getY() >= origin.getY() - 1)
                .filter(entry -> !entry.getValue().isAir())
                .count();
    }

    private static void assertRoomReachability(GameTestHelper helper, ExpeditionSiteBlockPlan plan) {
        Set<BlockPos> traversable = plan.blocks().entrySet().stream()
                .filter(entry -> entry.getValue().isAir()
                        || entry.getValue().is(Blocks.LADDER)
                        || entry.getValue().is(Blocks.OAK_TRAPDOOR)
                        || entry.getValue().is(Blocks.COBWEB))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
        BlockPos start = plan.anchorPos().offset(0, -1, 1);
        helper.assertTrue(traversable.contains(start), "The main shaft has no traversable ladder start");

        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        HashSet<BlockPos> visited = new HashSet<>();
        frontier.add(start);
        visited.add(start);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.removeFirst();
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                BlockPos next = current.relative(direction);
                if (traversable.contains(next) && visited.add(next)) {
                    frontier.addLast(next);
                }
            }
        }
        for (BlockPos roomCenter : plan.roomCenters()) {
            helper.assertTrue(visited.contains(roomCenter),
                    "Expedition room is disconnected from the main shaft: " + roomCenter);
        }
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
        ExpeditionSiteBlockPlan finalPlan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                resolution.finalQuality(),
                19L
        );
        helper.assertTrue(finalPlan.quality() == SiteQuality.RICH, "The blueprint retained Mother metadata");
        helper.assertTrue(finalPlan.oreBlockCount() == 0L && finalPlan.oreNodeCount() == 0,
                "The downgraded plan embedded a forbidden free resource");

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
                        ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "mineral/iron"),
                        1,
                        IoeExcavatorDepositRules.DepositTier.MOTHER,
                        48,
                        16_384
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
    public static void ieCoreSampleAndExcavatorUseCommittedIoeDeposit(GameTestHelper helper) {
        if (!ModList.get().isLoaded("immersiveengineering")) {
            helper.succeed();
            return;
        }

        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(31, 10, 31));
        BlockPos drillPos = anchor.above(2);
        ResourceLocation ironMixId = ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                "mineral/iron"
        );
        IoeExcavatorDepositRules.MotherDepositRequest request =
                new IoeExcavatorDepositRules.MotherDepositRequest(
                        anchor,
                        ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "test_province"),
                        "iron",
                        ironMixId,
                        1,
                        IoeExcavatorDepositRules.DepositTier.MOTHER,
                        48,
                        16_384
                );
        IoeMotherDepositReservation reservation = null;
        try {
            ClassLoader loader = ExpeditionWorldgenGameTests.class.getClassLoader();
            Class<?> bridge = Class.forName(
                    "com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge",
                    true,
                    loader
            );
            Method reserve = bridge.getMethod(
                    "reserveGuaranteedDeposit",
                    ServerLevel.class,
                    IoeExcavatorDepositRules.MotherDepositRequest.class
            );
            Optional<?> prepared = (Optional<?>) reserve.invoke(null, level, request);
            helper.assertTrue(prepared.isPresent(), "IE did not prepare the IOE iron deposit");
            reservation = (IoeMotherDepositReservation) prepared.orElseThrow();
            reservation.commit();

            Class<?> handler = Class.forName(
                    "blusunrize.immersiveengineering.api.excavator.ExcavatorHandler",
                    true,
                    loader
            );
            Object worldInfo = handler.getMethod("getMineralWorldInfo", Level.class, BlockPos.class)
                    .invoke(null, level, anchor);
            List<?> weightedVeins = (List<?>) worldInfo.getClass().getMethod("getAllVeins").invoke(worldInfo);
            Object committedVein = null;
            for (Object weightedVein : weightedVeins) {
                Object vein = weightedVein.getClass().getMethod("getFirst").invoke(weightedVein);
                ResourceLocation mineralName = (ResourceLocation) vein.getClass()
                        .getMethod("getMineralName")
                        .invoke(vein);
                if (ironMixId.equals(mineralName)) {
                    committedVein = vein;
                    break;
                }
            }
            helper.assertTrue(committedVein != null,
                    "The Core Sample mineral query did not detect the committed IOE iron deposit");

            ResourceLocation drillId = ResourceLocation.fromNamespaceAndPath(
                    "immersiveengineering",
                    "sample_drill"
            );
            Block drill = BuiltInRegistries.BLOCK.getOptional(drillId).orElseThrow();
            level.setBlock(drillPos, drill.defaultBlockState(), 3);
            Object drillBlockEntity = level.getBlockEntity(drillPos);
            helper.assertTrue(drillBlockEntity != null,
                    "The IE Core Sample Drill block did not create its block entity");
            ItemStack coreSample = (ItemStack) drillBlockEntity.getClass()
                    .getMethod("createCoreSample", worldInfo.getClass())
                    .invoke(drillBlockEntity, worldInfo);
            helper.assertFalse(coreSample.isEmpty(), "The IE Core Sample Drill produced no sample");

            Class<?> coreSampleItem = Class.forName(
                    "blusunrize.immersiveengineering.common.items.CoresampleItem",
                    true,
                    loader
            );
            List<?> detectedMixes = (List<?>) coreSampleItem
                    .getMethod("getMineralMixes", Level.class, ItemStack.class)
                    .invoke(null, level, coreSample);
            boolean detectedIronMix = false;
            for (Object detectedMix : detectedMixes) {
                if (ironMixId.equals(detectedMix.getClass().getMethod("id").invoke(detectedMix))) {
                    detectedIronMix = true;
                    break;
                }
            }
            helper.assertTrue(detectedIronMix,
                    "The generated Core Sample did not retain the IOE iron mineral mix");

            Object mineralMix = committedVein.getClass().getMethod("getMineral", Level.class)
                    .invoke(committedVein, level);
            Method getRandomOre = mineralMix.getClass().getMethod("getRandomOre", Random.class);
            TagKey<Item> ironOreTag = TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "ores/iron")
            );
            boolean sawIron = false;
            boolean sawCalcite = false;
            Random compositionRandom = new Random(0x10E2026L);
            for (int draw = 0; draw < 512; draw++) {
                ItemStack extracted = (ItemStack) getRandomOre.invoke(mineralMix, compositionRandom);
                helper.assertFalse(extracted.isEmpty(), "The IE Excavator recipe returned an empty output");
                boolean iron = extracted.is(ironOreTag);
                boolean calcite = extracted.is(net.minecraft.world.item.Items.CALCITE);
                helper.assertTrue(iron || calcite, "The IE Excavator recipe returned an unexpected output");
                sawIron |= iron;
                sawCalcite |= calcite;
            }
            helper.assertTrue(sawIron && sawCalcite,
                    "The IE mineral mix did not exercise both configured iron outputs");

            ItemStack machineOutput = invokeExcavatorFillBucket(
                    helper,
                    level,
                    anchor,
                    committedVein,
                    mineralMix,
                    loader
            );
            helper.assertTrue(machineOutput.is(ironOreTag)
                            || machineOutput.is(net.minecraft.world.item.Items.CALCITE)
                            || machineOutput.is(net.minecraft.world.item.Items.GRAVEL)
                            || machineOutput.is(net.minecraft.world.item.Items.COBBLESTONE)
                            || machineOutput.is(net.minecraft.world.item.Items.COBBLED_DEEPSLATE),
                    "The real IE Excavator bucket path produced an unexpected ore or spoil output");

            assertMineralMixContainsItem(
                    helper,
                    level,
                    ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "mineral/certus"),
                    ResourceLocation.fromNamespaceAndPath("ae2", "certus_quartz_crystal")
            );
            assertMineralMixContainsItem(
                    helper,
                    level,
                    ResourceLocation.fromNamespaceAndPath(
                            ImmersiveOreExpeditionMod.MODID,
                            "mineral/entroized_fluix"
                    ),
                    ResourceLocation.fromNamespaceAndPath("extendedae", "entro_crystal")
            );
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not exercise the IE Core Sample and Excavator runtime APIs", failure);
        } finally {
            level.setBlock(drillPos, Blocks.AIR.defaultBlockState(), 3);
            if (reservation != null) {
                reservation.rollback();
            }
        }
        helper.succeed();
    }

    private static ItemStack invokeExcavatorFillBucket(
            GameTestHelper helper,
            ServerLevel level,
            BlockPos anchor,
            Object mineralVein,
            Object mineralMix,
            ClassLoader loader
    ) throws ReflectiveOperationException {
        Class<?> mineralVeinClass = Class.forName(
                "blusunrize.immersiveengineering.api.excavator.MineralVein",
                true,
                loader
        );
        Class<?> mineralMixClass = Class.forName(
                "blusunrize.immersiveengineering.api.excavator.MineralMix",
                true,
                loader
        );
        Class<?> wheelStateClass = Class.forName(
                "blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic$State",
                true,
                loader
        );
        Class<?> multiblockLevelClass = Class.forName(
                "blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel",
                true,
                loader
        );
        Class<?> excavatorLogicClass = Class.forName(
                "blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ExcavatorLogic",
                true,
                loader
        );
        Object wheelState = wheelStateClass.getConstructor().newInstance();
        @SuppressWarnings("unchecked")
        List<ItemStack> digStacks = (List<ItemStack>) wheelStateClass.getField("digStacks").get(wheelState);
        Object multiblockLevel = Proxy.newProxyInstance(
                loader,
                new Class<?>[]{multiblockLevelClass},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getRawLevel" -> level;
                    case "getAbsoluteOrigin" -> anchor;
                    case "getMaxBuildHeight" -> level.getMaxBuildHeight();
                    case "shouldTickModulo" -> true;
                    case "isThundering" -> level.isThundering();
                    case "isRaining" -> level.isRaining();
                    case "getBlockState" -> level.getBlockState((BlockPos) arguments[0]);
                    case "getBlockEntity", "forciblyGetBlockEntity" ->
                            level.getBlockEntity((BlockPos) arguments[0]);
                    case "setBlock" -> {
                        level.setBlock(
                                (BlockPos) arguments[0],
                                (net.minecraft.world.level.block.state.BlockState) arguments[1],
                                3
                        );
                        yield null;
                    }
                    case "toAbsolute", "toRelative" -> arguments[0];
                    case "updateNeighbourForOutputSignal" -> null;
                    case "equals" -> proxy == arguments[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "IOE Excavator GameTest multiblock level";
                    default -> throw new AssertionError(
                            "Unexpected Excavator multiblock-level call: " + method
                    );
                }
        );
        Object excavatorLogic = excavatorLogicClass.getConstructor().newInstance();
        Method fillBucket = excavatorLogicClass.getDeclaredMethod(
                "fillBucket",
                mineralVeinClass,
                mineralMixClass,
                BlockPos.class,
                wheelStateClass,
                int.class,
                multiblockLevelClass
        );
        fillBucket.setAccessible(true);
        Method getDepletion = mineralVeinClass.getMethod("getDepletion");
        int depletionBefore = (int) getDepletion.invoke(mineralVein);
        boolean filled = (boolean) fillBucket.invoke(
                excavatorLogic,
                mineralVein,
                mineralMix,
                anchor,
                wheelState,
                0,
                multiblockLevel
        );
        helper.assertTrue(filled, "The real IE Excavator logic did not fill its bucket");
        ItemStack output = digStacks.getFirst().copy();
        helper.assertFalse(output.isEmpty(), "The real IE Excavator bucket was empty");
        mineralVeinClass.getMethod("deplete").invoke(mineralVein);
        int depletionAfter = (int) getDepletion.invoke(mineralVein);
        helper.assertTrue(depletionAfter == depletionBefore + 1,
                "The real IE Excavator extraction did not consume exactly one reserve unit");
        return output;
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void normalAe2MeteoriteBlocksSurviveOreGuard(GameTestHelper helper) {
        for (String path : List.of("flawed_budding_quartz", "sky_stone_block", "fluix_block")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("ae2", path);
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElseThrow();
            helper.assertFalse(IoeNewChunkOreGuard.isCandidate(block.defaultBlockState()),
                    "The new-chunk guard still removes preserved AE2 meteorite resource " + id);
        }
        if (ModList.get().isLoaded("extendedae")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    "extendedae",
                    "entro_budding_fully"
            );
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElseThrow();
            helper.assertFalse(IoeNewChunkOreGuard.isCandidate(block.defaultBlockState()),
                    "The new-chunk guard removes an ExtendedAE-owned progression block");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void fullRuntimeBiomeInventoryAndProfileExclusivity(GameTestHelper helper) {
        if (!List.of("biomesoplenty", "regions_unexplored", "biomeswevegone").stream()
                .allMatch(modId -> ModList.get().isLoaded(modId))) {
            helper.succeed();
            return;
        }

        var biomeRegistry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Map<String, Integer> expectedNamespaceCounts = Map.of(
                "minecraft", 64,
                "biomesoplenty", 69,
                "regions_unexplored", 78,
                "biomeswevegone", 55
        );
        HashMap<String, Integer> actualNamespaceCounts = new HashMap<>();
        TagKey<Biome> removedRu = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("regions_unexplored", "removed")
        );
        List<TagKey<Biome>> exclusiveProfiles = List.of(
                "diamond", "emerald", "certus", "entroized_fluix", "redstone", "gold", "uranium",
                "silver", "lapis", "copper", "aluminum", "coal", "nickel", "lead", "iron"
        ).stream().map(profile -> TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(
                        ImmersiveOreExpeditionMod.MODID,
                        "ore_profile/" + profile
                )
        )).toList();
        List<TagKey<Biome>> aquaticProfiles = List.of("alluvial_sift", "silt", "ancient_seabed")
                .stream().map(profile -> TagKey.create(
                        Registries.BIOME,
                        ResourceLocation.fromNamespaceAndPath(
                                ImmersiveOreExpeditionMod.MODID,
                                "aquatic/" + profile
                        )
                )).toList();

        int removedCount = 0;
        int activeCount = 0;
        for (Map.Entry<net.minecraft.resources.ResourceKey<Biome>, Biome> entry : biomeRegistry.entrySet()) {
            String namespace = entry.getKey().location().getNamespace();
            if (!expectedNamespaceCounts.containsKey(namespace)) {
                continue;
            }
            actualNamespaceCounts.merge(namespace, 1, Integer::sum);
            var biome = biomeRegistry.getHolder(entry.getKey()).orElseThrow();
            if (biome.is(removedRu)) {
                removedCount++;
                continue;
            }
            activeCount++;
            long ownerCount = exclusiveProfiles.stream().filter(biome::is).count()
                    + aquaticProfiles.stream().filter(biome::is).count();
            helper.assertTrue(ownerCount <= 1,
                    entry.getKey().location() + " has more than one specialized or aquatic profile");
        }

        helper.assertTrue(actualNamespaceCounts.equals(expectedNamespaceCounts),
                "Pinned runtime biome inventories differ: " + actualNamespaceCounts);
        helper.assertTrue(removedCount == 14, "Regions Unexplored removed tag must contain 14 biomes");
        helper.assertTrue(activeCount == 252, "Pinned active biome inventory must contain 252 biomes");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void fullRuntimeMultiSeedBiomeFrequency(GameTestHelper helper) {
        if (!List.of("biomesoplenty", "regions_unexplored", "biomeswevegone").stream()
                .allMatch(modId -> ModList.get().isLoaded(modId))) {
            helper.succeed();
            return;
        }

        ServerLevel level = helper.getLevel();
        LinkedHashMap<String, TagKey<Biome>> profileTags = new LinkedHashMap<>();
        for (String profile : List.of(
                "diamond", "emerald", "certus", "entroized_fluix", "redstone", "gold", "uranium",
                "silver", "lapis", "copper", "aluminum", "coal", "nickel", "lead", "iron"
        )) {
            profileTags.put(profile, TagKey.create(
                    Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(
                            ImmersiveOreExpeditionMod.MODID,
                            "ore_profile/" + profile
                    )
            ));
        }
        LinkedHashMap<String, TagKey<Biome>> aquaticTags = new LinkedHashMap<>();
        for (String profile : List.of("alluvial_sift", "silt", "ancient_seabed")) {
            aquaticTags.put("aquatic/" + profile, TagKey.create(
                    Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath(
                            ImmersiveOreExpeditionMod.MODID,
                            "aquatic/" + profile
                    )
            ));
        }

        JsonObject report = new JsonObject();
        report.addProperty("classification", "CONFIRMED_RUNTIME");
        report.addProperty("scope", "fresh normal Overworld BiomeSource initialized by TerraBlender per seed");
        report.addProperty("terrablender_initialized_per_seed", true);
        report.addProperty("grid_chunks_per_seed", BIOME_SAMPLE_GRID_SIDE * BIOME_SAMPLE_GRID_SIDE);
        JsonArray heights = new JsonArray();
        for (int height : BIOME_SAMPLE_HEIGHTS) {
            heights.add(height);
        }
        report.add("sample_block_y", heights);
        JsonArray seedReports = new JsonArray();
        LinkedHashMap<String, Long> aggregateProfiles = emptyProfileCounts(profileTags, aquaticTags);
        HashSet<ResourceLocation> aggregateBiomes = new HashSet<>();

        int gridOffset = BIOME_SAMPLE_GRID_SIDE / 2;
        for (long seed : BIOME_FREQUENCY_SEEDS) {
            NoiseBasedChunkGenerator noiseGenerator = terraBlenderGeneratorForSeed(level, seed);
            var settingsKey = noiseGenerator.generatorSettings().unwrapKey().orElseThrow();
            BiomeSource biomeSource = noiseGenerator.getBiomeSource();
            RandomState randomState = RandomState.create(
                    level.registryAccess().asGetterLookup(),
                    settingsKey,
                    seed
            );
            LinkedHashMap<String, Long> seedProfiles = emptyProfileCounts(profileTags, aquaticTags);
            HashSet<ResourceLocation> seedBiomes = new HashSet<>();
            for (int chunkX = -gridOffset; chunkX < gridOffset; chunkX++) {
                for (int chunkZ = -gridOffset; chunkZ < gridOffset; chunkZ++) {
                    int quartX = QuartPos.fromBlock(chunkX * 16 + 8);
                    int quartZ = QuartPos.fromBlock(chunkZ * 16 + 8);
                    for (int blockY : BIOME_SAMPLE_HEIGHTS) {
                        Holder<Biome> biome = biomeSource.getNoiseBiome(
                                quartX,
                                QuartPos.fromBlock(blockY),
                                quartZ,
                                randomState.sampler()
                        );
                        ResourceLocation biomeId = biome.unwrapKey().orElseThrow().location();
                        seedBiomes.add(biomeId);
                        aggregateBiomes.add(biomeId);
                        String profile = sampledProfile(biome, profileTags, aquaticTags);
                        seedProfiles.merge(profile, 1L, Long::sum);
                        aggregateProfiles.merge(profile, 1L, Long::sum);
                    }
                }
            }
            JsonObject seedReport = new JsonObject();
            seedReport.addProperty("seed", seed);
            seedReport.addProperty("samples",
                    BIOME_SAMPLE_GRID_SIDE * BIOME_SAMPLE_GRID_SIDE * BIOME_SAMPLE_HEIGHTS.length);
            seedReport.addProperty("distinct_biomes", seedBiomes.size());
            seedReport.add("profile_samples", profileCountsJson(seedProfiles));
            seedReports.add(seedReport);
        }
        report.add("seeds", seedReports);
        report.addProperty("distinct_biomes_observed", aggregateBiomes.size());
        report.add("aggregate_profile_samples", profileCountsJson(aggregateProfiles));

        String reportPath = System.getProperty("ioe.multiSeedReport");
        helper.assertTrue(reportPath != null && !reportPath.isBlank(),
                "The full-runtime multi-seed report path is not configured");
        try {
            Path output = Path.of(reportPath);
            Files.createDirectories(output.getParent());
            Files.writeString(
                    output,
                    new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n",
                    StandardCharsets.UTF_8
            );
        } catch (java.io.IOException failure) {
            throw new AssertionError("Could not write the runtime multi-seed biome report", failure);
        }
        IoeExpeditionWorldgenMod.LOGGER.info(
                "Measured IOE biome-profile frequency across {} seeds, {} samples and {} observed biomes: {}",
                BIOME_FREQUENCY_SEEDS.length,
                BIOME_FREQUENCY_SEEDS.length
                        * BIOME_SAMPLE_GRID_SIDE
                        * BIOME_SAMPLE_GRID_SIDE
                        * BIOME_SAMPLE_HEIGHTS.length,
                aggregateBiomes.size(),
                aggregateProfiles
        );
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static NoiseBasedChunkGenerator terraBlenderGeneratorForSeed(ServerLevel level, long seed) {
        LevelStem normalOverworld = WorldPresets.getNormalOverworld(level.registryAccess());
        if (!(normalOverworld.generator() instanceof NoiseBasedChunkGenerator templateGenerator)) {
            throw new AssertionError("The normal Overworld preset is not noise-based");
        }
        if (!(templateGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource templateBiomeSource)) {
            throw new AssertionError("The normal Overworld preset does not use a multi-noise biome source");
        }

        try {
            ClassLoader loader = ExpeditionWorldgenGameTests.class.getClassLoader();
            Class<?> sourceAccessType = Class.forName(
                    "terrablender.mixin.MultiNoiseBiomeSourceAccess",
                    true,
                    loader
            );
            if (!sourceAccessType.isInstance(templateBiomeSource)) {
                throw new AssertionError("TerraBlender biome-source accessor mixin is not active");
            }
            Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters =
                    (Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>>)
                            sourceAccessType.getMethod("getParameters").invoke(templateBiomeSource);
            Climate.ParameterList<Holder<Biome>> templateParameters = parameters.map(
                    direct -> direct,
                    preset -> preset.value().parameters()
            );

            Class<?> extendedParametersType = Class.forName(
                    "terrablender.worldgen.IExtendedParameterList",
                    true,
                    loader
            );
            Climate.ParameterList<Holder<Biome>> freshParameters = new Climate.ParameterList<>(
                    templateParameters.values()
            );
            if (!extendedParametersType.isInstance(freshParameters)) {
                throw new AssertionError("TerraBlender parameter-list mixin is not active");
            }
            MultiNoiseBiomeSource freshBiomeSource = MultiNoiseBiomeSource.createFromList(freshParameters);
            NoiseBasedChunkGenerator freshGenerator = new NoiseBasedChunkGenerator(
                    freshBiomeSource,
                    templateGenerator.generatorSettings()
            );

            Class<?> levelUtilsType = Class.forName("terrablender.util.LevelUtils", true, loader);
            levelUtilsType.getMethod(
                    "initializeBiomes",
                    RegistryAccess.class,
                    Holder.class,
                    ResourceKey.class,
                    ChunkGenerator.class,
                    long.class
            ).invoke(
                    null,
                    level.registryAccess(),
                    normalOverworld.type(),
                    LevelStem.OVERWORLD,
                    freshGenerator,
                    seed
            );
            boolean initialized = (boolean) extendedParametersType
                    .getMethod("isInitialized")
                    .invoke(freshParameters);
            if (!initialized) {
                throw new AssertionError("TerraBlender did not initialize the fresh biome source for seed " + seed);
            }
            return freshGenerator;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Could not initialize a fresh TerraBlender Overworld for seed " + seed, failure);
        }
    }

    private static LinkedHashMap<String, Long> emptyProfileCounts(
            Map<String, TagKey<Biome>> profileTags,
            Map<String, TagKey<Biome>> aquaticTags
    ) {
        LinkedHashMap<String, Long> counts = new LinkedHashMap<>();
        profileTags.keySet().forEach(profile -> counts.put(profile, 0L));
        aquaticTags.keySet().forEach(profile -> counts.put(profile, 0L));
        counts.put("generic", 0L);
        return counts;
    }

    private static String sampledProfile(
            Holder<Biome> biome,
            Map<String, TagKey<Biome>> profileTags,
            Map<String, TagKey<Biome>> aquaticTags
    ) {
        String matched = null;
        for (Map.Entry<String, TagKey<Biome>> entry : profileTags.entrySet()) {
            if (biome.is(entry.getValue())) {
                if (matched != null) {
                    throw new IllegalStateException("Sampled biome belongs to multiple IOE profiles");
                }
                matched = entry.getKey();
            }
        }
        for (Map.Entry<String, TagKey<Biome>> entry : aquaticTags.entrySet()) {
            if (biome.is(entry.getValue())) {
                if (matched != null) {
                    throw new IllegalStateException("Sampled biome belongs to specialized and aquatic profiles");
                }
                matched = entry.getKey();
            }
        }
        return matched == null ? "generic" : matched;
    }

    private static JsonObject profileCountsJson(Map<String, Long> counts) {
        JsonObject payload = new JsonObject();
        counts.forEach((profile, count) -> payload.addProperty(profile, count));
        return payload;
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void dryFallbackConfirmsWithoutDeposit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(16, 24, 16)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);
        BiomeMineResourceProfile profile = testIronProfile(level);
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
        BiomeMineResourceProfile profile = testIronProfile(level);
        ExpeditionSiteBlockPlan plan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.RICH,
                73L
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
    public static void ieCommitFailuresRunFullChainBeforeLocator(GameTestHelper helper) {
        if (!ModList.get().isLoaded("immersiveengineering")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(new BlockPos(48, 24, 48)));
        BlockPos origin = new BlockPos(testChunk.getMinBlockX() + 4, 41, testChunk.getMinBlockZ() + 6);
        fillTestChunk(level, testChunk);
        BiomeMineResourceProfile profile = testIronProfile(level);
        ExpeditionSiteBlockPlan motherPlan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.MOTHERLODE,
                83L
        );
        ExpeditionSiteBlockPlan richPlan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.RICH,
                89L
        );
        ExpeditionSiteBlockPlan normalPlan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.NORMAL,
                97L
        );
        ExpeditionSiteBlockPlan poorPlan = structureOnlyPlan(
                ExpeditionSiteType.TINY_VERTICAL_MINE_ENTRANCE,
                origin,
                SiteQuality.POOR,
                101L
        );
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        AtomicInteger fallbackCommits = new AtomicInteger();
        AtomicInteger fallbackRollbacks = new AtomicInteger();
        ArrayList<IoeExcavatorDepositRules.DepositTier> attemptedFallbackTiers = new ArrayList<>();
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
                        null,
                        List.of(richPlan, normalPlan, poorPlan)
                ),
                "The Mother plan was not staged for transactional confirmation");

        IoePendingExpeditionSites.Confirmation confirmation =
                IoePendingExpeditionSites.confirmLoadedChunk(
                        level,
                        testChunk,
                        (ignoredLevel, request) -> {
                            attemptedFallbackTiers.add(request.tier());
                            return Optional.of(new IoeMotherDepositReservation() {
                                @Override
                                public boolean createdByIoe() {
                                    return true;
                                }

                                @Override
                                public void commit() {
                                    fallbackCommits.incrementAndGet();
                                    if (request.tier() != IoeExcavatorDepositRules.DepositTier.DIRECT) {
                                        throw new IllegalStateException(
                                                "forced " + request.tier() + " commit failure"
                                        );
                                    }
                                }

                                @Override
                                public void rollback() {
                                    fallbackRollbacks.incrementAndGet();
                                }
                            });
                        }
                );

        helper.assertTrue(confirmation.confirmedSites() == 1, "The Direct fallback was not confirmed");
        helper.assertTrue(confirmation.rejectedSites() == 0, "The Direct fallback was rejected");
        helper.assertTrue(commits.get() == 1, "The IE commit was not attempted exactly once");
        helper.assertTrue(rollbacks.get() == 1, "The failed IE reservation was not compensated exactly once");
        helper.assertTrue(attemptedFallbackTiers.equals(List.of(
                        IoeExcavatorDepositRules.DepositTier.MAJOR,
                        IoeExcavatorDepositRules.DepositTier.MINOR,
                        IoeExcavatorDepositRules.DepositTier.DIRECT
                )),
                "The fallback did not attempt Major, Minor and Direct in exact order");
        helper.assertTrue(fallbackCommits.get() == 3,
                "The fallback did not commit-attempt every lower reserve tier");
        helper.assertTrue(fallbackRollbacks.get() == 2,
                "The two failed lower-tier reservations were not compensated exactly once");
        helper.assertTrue(poorPlan.blocks().entrySet().stream()
                        .filter(entry -> !entry.getValue().isAir())
                        .allMatch(entry -> level.getBlockState(entry.getKey()).getBlock()
                                == entry.getValue().getBlock()),
                "The final world blocks do not match the Direct pipeline");
        helper.assertTrue(ExpeditionLocatorService.index(level).sites().stream()
                        .anyMatch(site -> site.pos().equals(origin)
                                && site.quality().filter(quality -> quality == SiteQuality.POOR).isPresent()),
                "The locator did not persist the final Direct quality");
        helper.assertFalse(ExpeditionLocatorService.index(level).sites().stream()
                        .anyMatch(site -> site.pos().equals(origin)
                                && site.quality().filter(quality -> quality == SiteQuality.MOTHERLODE).isPresent()),
                "The provisional Mother quality leaked into SavedData");
        helper.succeed();
    }

    private static BiomeMineResourceProfile testIronProfile(ServerLevel level) {
        ResourceLocation definitionId = ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                "iron"
        );
        var definitions = level.registryAccess()
                .registry(BiomeMineResourceDefinition.REGISTRY_KEY)
                .orElseThrow();
        Map.Entry<net.minecraft.resources.ResourceKey<BiomeMineResourceDefinition>, BiomeMineResourceDefinition> entry =
                definitions.entrySet().stream()
                        .filter(candidate -> candidate.getKey().location().equals(definitionId))
                        .findFirst()
                        .orElseThrow();
        return new BiomeMineResourceProfile(
                ResourceLocation.fromNamespaceAndPath("minecraft", "plains"),
                definitionId,
                entry.getValue(),
                1
        );
    }

    private static void assertMineralMixContainsItem(
            GameTestHelper helper,
            Level level,
            ResourceLocation mineralMixId,
            ResourceLocation expectedItemId
    ) throws ReflectiveOperationException {
        Class<?> mineralMixClass = Class.forName(
                "blusunrize.immersiveengineering.api.excavator.MineralMix",
                true,
                ExpeditionWorldgenGameTests.class.getClassLoader()
        );
        Object recipeCache = mineralMixClass.getField("RECIPES").get(null);
        Object mineralMix = recipeCache.getClass()
                .getMethod("getById", Level.class, ResourceLocation.class)
                .invoke(recipeCache, level, mineralMixId);
        helper.assertTrue(mineralMix != null, "Missing runtime mineral mix " + mineralMixId);
        List<?> outputs = (List<?>) mineralMixClass.getField("outputs").get(mineralMix);
        boolean containsExpectedItem = false;
        for (Object outputWithChance : outputs) {
            Object tagOutput = outputWithChance.getClass().getMethod("stack").invoke(outputWithChance);
            ItemStack output = (ItemStack) tagOutput.getClass().getMethod("get").invoke(tagOutput);
            if (expectedItemId.equals(BuiltInRegistries.ITEM.getKey(output.getItem()))) {
                containsExpectedItem = true;
                break;
            }
        }
        helper.assertTrue(
                containsExpectedItem,
                mineralMixId + " does not expose the expected Excavator output " + expectedItemId
        );
    }

    private static ExpeditionSiteBlockPlan structureOnlyPlan(
            ExpeditionSiteType type,
            BlockPos origin,
            SiteQuality quality,
            long seed
    ) {
        return ExpeditionSiteBlueprints.plan(
                type,
                origin,
                quality,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                RandomSource.create(seed)
        );
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

        if (type.naturalSurfaceSite()) {
            RandomSource expectationRandom = RandomSource.create(PRODUCTIVE_SEED);
            SiteQuality expectedQuality = new SiteQualityRoll(0, 25, 45, 17, 3).roll(expectationRandom);
            ExpeditionSiteBlockPlan preview = structureOnlyPlan(
                    type,
                    origin,
                    expectedQuality,
                    expectationRandom.nextLong()
            );
            boolean hasSpecializedChamberProfile = BiomeMineResourceProfile.resolve(
                    level,
                    preview.chamberCenter()
            ).failure() == BiomeMineResourceProfile.Failure.NONE;
            boolean shouldPlace = ModList.get().isLoaded("immersiveengineering")
                    && hasSpecializedChamberProfile;
            if (shouldPlace) {
                helper.assertTrue(placed,
                        type.id() + " rejected a specialized chamber with its IE backend loaded");
            } else {
                helper.assertFalse(placed,
                        type.id() + " created a natural site without both a specialized chamber and IE backend");
            }
        }

        if (type.naturalSurfaceSite() && !placed) {
            helper.assertFalse(containsBlock(level, testChunk, Blocks.LADDER),
                    type.id() + " left structure blocks after rejecting a non-qualified productive site");
            helper.assertFalse(ExpeditionLocatorService.index(level).sites().stream()
                            .anyMatch(site -> site.pos().equals(origin)),
                    type.id() + " persisted a locator entry after rejecting a non-qualified productive site");
            helper.succeed();
            return;
        }

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
            helper.assertFalse(containsAnyProductiveResourceBlock(level, testChunk, origin, type),
                    type.id() + " placed a forbidden free ore node or artificial geode");
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
                profile.profileName()
        ) {
            case "certus" -> Ae2MeteoriteIntegration.resolve().map(material -> Set.of(
                    material.buddingBlock(),
                    material.skyStoneBlock()
            ));
            case "entroized_fluix" -> ExtendedAeGeodeIntegration.resolve().map(material -> Set.of(
                    material.buddingBlock(),
                    material.shellBlock()
            ));
            default -> GeOreNodeIntegration.resolve(profile.profileName()).map(material -> Set.of(
                    material.nodeBlock(),
                    material.buddingBlock()
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
