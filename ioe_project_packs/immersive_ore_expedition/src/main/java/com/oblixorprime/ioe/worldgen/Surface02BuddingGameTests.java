package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingRankResolution;
import com.oblixorprime.ioe.budding.BuddingRankResolver;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.budding.GeOreRankedBuddingBlock;
import com.oblixorprime.ioe.budding.IoeBuddingBlocks;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.surface02.Surface02BiomeFamily;
import com.oblixorprime.ioe.surface02.Surface02Composer;
import com.oblixorprime.ioe.surface02.Surface02Plan;
import com.oblixorprime.ioe.surface02.Surface02TerrainContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Headless runtime proof for the passive Surface 02 plan and the compiled Budding foundation.
 *
 * <p>These tests call the real composer, registry resolver, adapter, placement journal, recipe
 * manager, tags, and random-tick implementation. They do not register Surface 02 with natural
 * worldgen.</p>
 */
@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class Surface02BuddingGameTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Surface02BuddingGameTests.class);
    private static final String TEMPLATE = "expedition_worldgen_empty";
    private static final int RANDOM_TICK_SEED_LIMIT = 65_536;
    private static final ResourceLocation RESONATING_SEED = id("ae2cs", "resonating_seed");
    private static final ResourceLocation PURIFIED_RESONATING_CRYSTAL =
            id("ae2cs", "purified_resonating_crystal");
    private static final TagKey<Item> RESONATING_CRYSTAL_TAG = TagKey.create(
            Registries.ITEM,
            id("ae2cs", "pure_crystal/resonating_crystal")
    );
    private static final Set<ResourceLocation> DISABLED_EXTERNAL_RECIPES = Set.of(
            id("ae2cs", "aggregator/flawless_budding_quartz"),
            id("extendedae", "fixer/certus_flawed"),
            id("extendedae", "fixer/entro_mostly")
    );
    private static final List<TagKey<Block>> REQUIRED_BLOCK_TAGS = List.of(
            TagKey.create(Registries.BLOCK, id("c", "budding_blocks")),
            TagKey.create(Registries.BLOCK, id("c", "relocation_not_supported")),
            TagKey.create(Registries.BLOCK, id("minecraft", "mineable/pickaxe")),
            TagKey.create(Registries.BLOCK, id("minecraft", "crystal_sound_blocks")),
            TagKey.create(Registries.BLOCK, id("geore", "budding_geore"))
    );
    private static final List<TagKey<Item>> REQUIRED_ITEM_TAGS = List.of(
            TagKey.create(Registries.ITEM, id("c", "budding_blocks")),
            TagKey.create(Registries.ITEM, id("geore", "geore_budding"))
    );
    private static final Set<ResourceLocation> ALL_RANKED_BUDDING_IDS = allRankedBuddingIds();
    private static final Set<ResourceLocation> ALL_MINERAL_IDS = allMineralIds();

    private Surface02BuddingGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void buddingRegistriesTagsAndRecipesAreRuntimeBound(GameTestHelper helper) {
        assertLoaded(helper, ImmersiveOreExpeditionMod.MODID);
        assertLoaded(helper, "ae2");
        assertLoaded(helper, "ae2cs");
        assertLoaded(helper, "geore");
        assertLoaded(helper, "extendedae");

        Set<ResourceLocation> expectedRankedIds = new LinkedHashSet<>();
        for (String material : IoeBuddingBlocks.GEORE_MATERIALS) {
            BuddingResourceFamily family = BuddingResourceFamily.fromGeOreMaterial(material).orElseThrow();
            for (BuddingRank rank : BuddingRank.values()) {
                ResourceLocation expectedId = IoeBuddingBlocks.blockId(material, rank);
                expectedRankedIds.add(expectedId);
                Block registered = BuiltInRegistries.BLOCK.getOptional(expectedId).orElseThrow();
                helper.assertTrue(
                        registered instanceof GeOreRankedBuddingBlock,
                        expectedId + " is not a GeOreRankedBuddingBlock"
                );
                helper.assertTrue(
                        IoeBuddingBlocks.registration(material, rank).orElseThrow().get() == registered,
                        expectedId + " DeferredBlock is not bound to the runtime registry instance"
                );
                helper.assertTrue(
                        BuiltInRegistries.ITEM.getOptional(expectedId).isPresent(),
                        expectedId + " has no registered BlockItem"
                );
                BuddingRankResolution resolution = BuddingRankResolver.resolve(family, rank);
                helper.assertTrue(resolution.supported(), expectedId + " did not resolve: " + resolution.detail());
                helper.assertTrue(
                        resolution.buddingBlockId().orElseThrow().equals(expectedId),
                        expectedId + " resolved to " + resolution.buddingBlockId()
                );
            }
        }
        helper.assertTrue(expectedRankedIds.size() == 52, "Expected exactly 52 ranked GeOre ids");

        for (BuddingRank rank : BuddingRank.values()) {
            ResourceLocation certusId = id("ae2", rank.path() + "_budding_quartz");
            helper.assertTrue(BuiltInRegistries.BLOCK.getOptional(certusId).isPresent(), "Missing " + certusId);
            BuddingRankResolution resolution = BuddingRankResolver.resolve(
                    BuddingResourceFamily.CERTUS_QUARTZ,
                    rank
            );
            helper.assertTrue(resolution.supported(), certusId + " did not resolve: " + resolution.detail());
            helper.assertTrue(
                    resolution.buddingBlockId().orElseThrow().equals(certusId),
                    certusId + " resolved to the wrong native Certus block"
            );
            helper.assertTrue(
                    BuiltInRegistries.BLOCK.getOptional(
                            id(ImmersiveOreExpeditionMod.MODID, rank.path() + "_budding_certus_quartz")
                    ).isEmpty(),
                    "IOE registered a forbidden duplicate Certus rank"
            );
            BuddingRankResolution extended = BuddingRankResolver.resolve(
                    BuddingResourceFamily.ENTROIZED_FLUIX,
                    rank
            );
            helper.assertFalse(extended.supported(), "ExtendedAE unexpectedly became rank-supported");
            helper.assertTrue(
                    extended.detail().startsWith("RANK_UNSUPPORTED:"),
                    "ExtendedAE refusal lost its typed detail: " + extended.detail()
            );
        }

        assertTags(helper, expectedRankedIds);
        assertRecipes(helper, expectedRankedIds);
        helper.assertFalse(
                helper.getLevel().getRecipeManager().hadErrorsLoading(),
                "RecipeManager reports datapack recipe errors"
        );

        LOGGER.info(
                "IOE_RUNTIME registry blocks={} blockTags={} itemTags={} recipes=39 extendedaeLoaded={}",
                expectedRankedIds.size(),
                REQUIRED_BLOCK_TAGS.size(),
                REQUIRED_ITEM_TAGS.size(),
                ModList.get().isLoaded("extendedae")
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void surface02DryTemperateGeOreIron(GameTestHelper helper) {
        runSupportedCase(
                helper,
                "dry_temperate_geore_iron",
                SiteQuality.DRY,
                BuddingResourceFamily.GEORE_IRON,
                Surface02BiomeFamily.TEMPERATE,
                0L
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void surface02PoorAridGeOreIron(GameTestHelper helper) {
        runSupportedCase(
                helper,
                "poor_arid_geore_iron",
                SiteQuality.POOR,
                BuddingResourceFamily.GEORE_IRON,
                Surface02BiomeFamily.ARID,
                1L
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void surface02NormalColdGeOreCopper(GameTestHelper helper) {
        runSupportedCase(
                helper,
                "normal_cold_geore_copper",
                SiteQuality.NORMAL,
                BuddingResourceFamily.GEORE_COPPER,
                Surface02BiomeFamily.COLD,
                -1L
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void surface02PoorWetCertus(GameTestHelper helper) {
        runSupportedCase(
                helper,
                "poor_wet_certus",
                SiteQuality.POOR,
                BuddingResourceFamily.CERTUS_QUARTZ,
                Surface02BiomeFamily.WET,
                0x5EEDC0DEL
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void surface02NormalReliefCertus(GameTestHelper helper) {
        PlacedCase placed = runSupportedCase(
                helper,
                "normal_relief_certus",
                SiteQuality.NORMAL,
                BuddingResourceFamily.CERTUS_QUARTZ,
                Surface02BiomeFamily.MOUNTAIN,
                0L
        );
        helper.assertTrue(
                placed.plan().canonicalBiomeFamily() == Surface02BiomeFamily.Canonical.RELIEF,
                "MOUNTAIN did not canonicalize to RELIEF"
        );
        helper.assertTrue(
                Surface02BiomeFamily.ROCKY.canonical().orElseThrow() == Surface02BiomeFamily.Canonical.RELIEF
                        && Surface02BiomeFamily.VOLCANIC.canonical().orElseThrow()
                        == Surface02BiomeFamily.Canonical.RELIEF,
                "RELIEF aliases are not canonical"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void surface02RefusalMatrix(GameTestHelper helper) {
        BlockPos origin = primaryOrigin(helper);
        assertRefusal(
                helper,
                "poor_extendedae",
                origin,
                SiteQuality.POOR,
                BuddingResourceFamily.ENTROIZED_FLUIX,
                Surface02BiomeFamily.TEMPERATE,
                1L,
                Surface02Composer.Status.UNSUPPORTED_RESOURCE_FAMILY
        );
        assertRefusal(
                helper,
                "rich_geore_iron",
                origin,
                SiteQuality.RICH,
                BuddingResourceFamily.GEORE_IRON,
                Surface02BiomeFamily.TEMPERATE,
                -1L,
                Surface02Composer.Status.INELIGIBLE_QUALITY
        );
        assertRefusal(
                helper,
                "motherlode_certus",
                origin,
                SiteQuality.MOTHERLODE,
                BuddingResourceFamily.CERTUS_QUARTZ,
                Surface02BiomeFamily.TEMPERATE,
                0x5EEDC0DEL,
                Surface02Composer.Status.INELIGIBLE_QUALITY
        );
        assertRefusal(
                helper,
                "aquatic_geore_iron",
                origin,
                SiteQuality.POOR,
                BuddingResourceFamily.GEORE_IRON,
                Surface02BiomeFamily.AQUATIC,
                0L,
                Surface02Composer.Status.INVALID_TERRAIN
        );
        assertRefusal(
                helper,
                "shoreline_geore_iron",
                origin,
                SiteQuality.POOR,
                BuddingResourceFamily.GEORE_IRON,
                Surface02BiomeFamily.SHORELINE,
                1L,
                Surface02Composer.Status.INVALID_TERRAIN
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 800)
    public static void surface02RuntimeDeterminism(GameTestHelper helper) {
        BlockPos firstOrigin = primaryOrigin(helper);
        BlockPos secondOrigin = firstOrigin.offset(16, 0, 0);
        long[] seeds = {0L, 1L, -1L, 0x5EEDC0DEL};
        Map<Long, String> forwardDigests = new LinkedHashMap<>();
        for (long seed : seeds) {
            String first = placeForDigest(helper, firstOrigin, seed);
            String second = placeForDigest(helper, secondOrigin, seed);
            helper.assertTrue(first.equals(second), "Runtime digest differs across zones for seed " + seed);
            forwardDigests.put(seed, first);
        }
        for (int index = seeds.length - 1; index >= 0; index--) {
            long seed = seeds[index];
            String reverse = placeForDigest(helper, firstOrigin, seed);
            helper.assertTrue(
                    reverse.equals(forwardDigests.get(seed)),
                    "Runtime digest depends on execution order for seed " + seed
            );
        }
        LOGGER.info(
                "IOE_RUNTIME determinism seeds={} digests={} salts=[0x5355524630324147,"
                        + "0x5355524630324641,0x5355524630324E41]",
                List.of(0L, 1L, -1L, 0x5EEDC0DEL),
                forwardDigests
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 1200)
    public static void geOreIronRandomTickRankChain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(16, 24, 16));
        GrowthObservation decayBranch = assertDecayStep(
                helper,
                level,
                center,
                BuddingRank.FLAWED,
                IoeBuddingBlocks.blockId("iron", BuddingRank.CHIPPED)
        );
        assertDecayStep(helper, level, center, BuddingRank.CHIPPED, IoeBuddingBlocks.blockId(
                "iron",
                BuddingRank.DAMAGED
        ));
        assertDecayStep(helper, level, center, BuddingRank.DAMAGED, id("geore", "iron_block"));

        ResourceLocation flawlessId = IoeBuddingBlocks.blockId("iron", BuddingRank.FLAWLESS);
        GeOreRankedBuddingBlock flawless = requireGeOreRankedBlock(flawlessId);
        setBlock(level, center, flawless.defaultBlockState());
        clearGrowthCells(level, center);
        flawless.randomTick(
                level.getBlockState(center),
                level,
                center,
                RandomSource.create(decayBranch.seed())
        );
        List<ResourceLocation> flawlessGrowth = growthProducts(level, center);
        helper.assertTrue(
                flawlessGrowth.equals(List.of(id("geore", "small_iron_bud"))),
                "FLAWLESS did not exercise the proven growth-plus-decay branch: " + flawlessGrowth
        );
        helper.assertTrue(
                blockId(level.getBlockState(center)).equals(flawlessId),
                "FLAWLESS degraded under the proven decay-triggering seed " + decayBranch.seed()
        );
        LOGGER.info(
                "IOE_RUNTIME randomTick start=FLAWLESS decayBranchSeed={} limit={} growth={} center={}",
                decayBranch.seed(),
                RANDOM_TICK_SEED_LIMIT,
                flawlessGrowth.getFirst(),
                flawlessId
        );
        helper.succeed();
    }

    private static PlacedCase runSupportedCase(
            GameTestHelper helper,
            String caseName,
            SiteQuality quality,
            BuddingResourceFamily family,
            Surface02BiomeFamily biome,
            long seed
    ) {
        BlockPos origin = primaryOrigin(helper);
        Surface02Composer.Decision decision = Surface02Composer.resolveAndCompose(
                quality,
                family,
                seed,
                biome,
                origin,
                Surface02TerrainContext.canonicalEnvelope(origin)
        );
        helper.assertTrue(
                decision.status() == Surface02Composer.Status.SUPPORTED,
                caseName + " was refused: " + decision.status() + " " + decision.detail()
        );
        Surface02Plan plan = decision.plan().orElseThrow();
        PlacedCase placed = placeAndAssert(helper, caseName, plan, true);
        LOGGER.info(
                "IOE_RUNTIME case={} seed={} quality={} biome={} canonicalBiome={} family={} slots={} "
                        + "budding={} minerals={} digest={}",
                caseName,
                seed,
                quality,
                biome,
                plan.canonicalBiomeFamily(),
                family,
                plan.slots().size(),
                plan.activeBuddingCount(),
                plan.mineralBlockCount(),
                placed.digest()
        );
        return placed;
    }

    private static PlacedCase placeAndAssert(
            GameTestHelper helper,
            String caseName,
            Surface02Plan plan,
            boolean accept
    ) {
        ServerLevel level = helper.getLevel();
        level.getChunk(plan.origin());
        ExpeditionSiteBlockPlan adapted = plan.toExpeditionSiteBlockPlan(
                Surface02BuddingGameTests::resolveBlockState
        );
        assertAdapterCompleteness(helper, plan, adapted);
        preparePlacementFixture(level, plan);
        Map<BlockPos, BlockState> outsidePlanBefore = snapshotOutsidePlan(level, plan);
        Set<UUID> entitiesBefore = entityIds(level, plan);
        Optional<IoeExpeditionPlanPlacement.AppliedPlan> applied =
                IoeExpeditionPlanPlacement.apply(level, adapted);
        helper.assertTrue(applied.isPresent(), caseName + " real plan placement was refused");
        assertPlacedWorld(helper, plan, adapted, outsidePlanBefore, entitiesBefore);
        String digest = normalizedDigest(level, plan.origin(), adapted.blocks());
        if (accept) {
            applied.orElseThrow().accept();
        } else {
            helper.assertTrue(applied.orElseThrow().rollback(level), caseName + " rollback failed");
        }
        return new PlacedCase(plan, adapted, digest);
    }

    private static void assertAdapterCompleteness(
            GameTestHelper helper,
            Surface02Plan plan,
            ExpeditionSiteBlockPlan adapted
    ) {
        helper.assertTrue(
                adapted.blocks().keySet().equals(plan.blocks().keySet()),
                "Surface 02 adapter changed the canonical plan position set"
        );
        plan.blocks().forEach((pos, cell) -> helper.assertTrue(
                blockId(adapted.blocks().get(pos)).equals(cell.blockId()),
                "Surface 02 adapter changed " + pos + " from " + cell.blockId()
                        + " to " + blockId(adapted.blocks().get(pos))
        ));
    }

    private static void assertPlacedWorld(
            GameTestHelper helper,
            Surface02Plan plan,
            ExpeditionSiteBlockPlan adapted,
            Map<BlockPos, BlockState> outsidePlanBefore,
            Set<UUID> entitiesBefore
    ) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(Surface02Plan.PLANNING_ONLY, "Surface 02 lost PLANNING_ONLY");
        helper.assertTrue(Surface02Plan.DEFAULT_OFF, "Surface 02 lost DEFAULT_OFF");
        helper.assertTrue(plan.slots().size() == 4, "Surface 02 did not expose four logical slots");
        helper.assertTrue(plan.lootContainerCount() == 0, "Surface 02 planned a loot container");
        helper.assertTrue(
                adapted.blocks().keySet().stream().allMatch(pos -> new ChunkPos(pos).equals(new ChunkPos(
                        plan.origin()
                ))),
                "Surface 02 escaped its anchor chunk"
        );
        for (Map.Entry<BlockPos, BlockState> expected : adapted.blocks().entrySet()) {
            BlockState actual = level.getBlockState(expected.getKey());
            helper.assertTrue(
                    actual.equals(expected.getValue()),
                    "World mismatch at " + expected.getKey() + ": expected "
                            + expected.getValue() + " actual " + actual
            );
            helper.assertTrue(actual.getFluidState().isEmpty(), "Fluid placed at " + expected.getKey());
            helper.assertFalse(actual.hasBlockEntity(), "Block entity placed at " + expected.getKey());
            helper.assertFalse(
                    actual.is(Blocks.FIRE) || actual.is(Blocks.SOUL_FIRE) || actual.is(Blocks.LAVA),
                    "Active hazard placed at " + expected.getKey()
            );
        }
        outsidePlanBefore.forEach((pos, state) -> helper.assertTrue(
                level.getBlockState(pos).equals(state),
                "Unexpected write outside the Surface 02 plan at " + pos
        ));
        helper.assertTrue(
                entityIds(level, plan).equals(entitiesBefore),
                "Surface 02 placement created or removed an entity"
        );

        int expectedActive = switch (plan.quality()) {
            case DRY -> 0;
            case POOR -> 3;
            case NORMAL -> 4;
            case RICH, MOTHERLODE -> throw new AssertionError("Ineligible plan was placed");
        };
        int expectedMinerals = switch (plan.quality()) {
            case DRY -> 0;
            case POOR -> 12;
            case NORMAL -> 20;
            case RICH, MOTHERLODE -> throw new AssertionError("Ineligible plan was placed");
        };
        BuddingRank expectedRank = plan.quality() == SiteQuality.POOR
                ? BuddingRank.DAMAGED
                : plan.quality() == SiteQuality.NORMAL ? BuddingRank.CHIPPED : null;
        helper.assertTrue(plan.activeBuddingCount() == expectedActive, "Wrong active Budding plan count");
        helper.assertTrue(plan.mineralBlockCount() == expectedMinerals, "Wrong mineral plan budget");

        ResourceLocation mineralId = plan.resource()
                .map(resource -> resource.mineralBlockId())
                .orElse(null);
        for (int index = 0; index < plan.slots().size(); index++) {
            Surface02Plan.Slot slot = plan.slots().get(index);
            boolean shouldBeActive = index < expectedActive;
            helper.assertTrue(slot.active() == shouldBeActive, slot.id() + " active state is wrong");
            BlockState nodeState = level.getBlockState(slot.nodePos());
            if (shouldBeActive) {
                helper.assertTrue(
                        slot.activeRank().orElseThrow() == expectedRank,
                        slot.id() + " has the wrong rank"
                );
                helper.assertTrue(
                        blockId(nodeState).equals(plan.resource().orElseThrow().buddingBlockId()),
                        slot.id() + " has the wrong world Budding block"
                );
                helper.assertTrue(
                        slot.protectedGrowthEnvelope().size() == Direction.values().length,
                        slot.id() + " does not own six growth cells"
                );
                for (BlockPos growthPos : slot.protectedGrowthEnvelope()) {
                    BlockState growth = level.getBlockState(growthPos);
                    helper.assertTrue(growth.isAir(), slot.id() + " growth cell is obstructed: " + growthPos);
                    helper.assertTrue(
                            growth.getFluidState().isEmpty(),
                            slot.id() + " growth cell contains fluid: " + growthPos
                    );
                }
            } else {
                helper.assertTrue(
                        nodeState.is(Blocks.COBBLED_DEEPSLATE),
                        slot.id() + " does not expose the canonical exhausted face"
                );
            }
            for (BlockPos mineralPos : slot.mineralPositions()) {
                helper.assertTrue(
                        blockId(level.getBlockState(mineralPos)).equals(mineralId),
                        slot.id() + " mineral ring contains the wrong block at " + mineralPos
                );
            }
        }
        Map<ResourceLocation, Integer> census = censusEnvelope(level, plan);
        int actualActive = ALL_RANKED_BUDDING_IDS.stream()
                .mapToInt(id -> census.getOrDefault(id, 0))
                .sum();
        int actualMinerals = ALL_MINERAL_IDS.stream()
                .mapToInt(id -> census.getOrDefault(id, 0))
                .sum();
        helper.assertTrue(
                actualActive == expectedActive,
                "Independent envelope census found " + actualActive + " Budding blocks, expected "
                        + expectedActive
        );
        helper.assertTrue(
                actualMinerals == expectedMinerals,
                "Independent envelope census found " + actualMinerals + " mineral blocks, expected "
                        + expectedMinerals
        );
        assertStructuralTraversal(helper, plan);
        assertPlanningOnlyRegistries(helper);
    }

    private static void assertStructuralTraversal(GameTestHelper helper, Surface02Plan plan) {
        ServerLevel level = helper.getLevel();
        for (BlockPos feet : plan.walkableCells()) {
            helper.assertTrue(level.getBlockState(feet).isAir(), "Walkable feet are blocked at " + feet);
            helper.assertTrue(level.getBlockState(feet.above()).isAir(), "Walkable head is blocked at " + feet);
            helper.assertTrue(
                    level.getBlockState(feet).getFluidState().isEmpty()
                            && level.getBlockState(feet.above()).getFluidState().isEmpty(),
                    "Walkable corridor contains fluid at " + feet
            );
            helper.assertFalse(
                    level.getBlockState(feet.above(2)).getBlock() instanceof FallingBlock,
                    "Unstable falling block is above mandatory path at " + feet
            );
        }

        BlockPos entrance = plan.origin().offset(0, 0, 2);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(entrance);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (BlockPos candidate : plan.walkableCells()) {
                int horizontal = Math.abs(candidate.getX() - current.getX())
                        + Math.abs(candidate.getZ() - current.getZ());
                int vertical = Math.abs(candidate.getY() - current.getY());
                if (horizontal == 1
                        && vertical <= 1
                        && level.getBlockState(candidate).isAir()
                        && level.getBlockState(candidate.above()).isAir()
                        && !visited.contains(candidate)) {
                    queue.addLast(candidate);
                }
            }
        }
        for (Surface02Plan.Slot slot : plan.slots()) {
            helper.assertTrue(
                    visited.contains(slot.approachPos()),
                    "Structural BFS cannot reach " + slot.id() + " approach"
            );
        }
        helper.assertTrue(
                plan.obstructionCells().stream().allMatch(pos -> !level.getBlockState(pos).isAir()),
                "Narrative collapse obstruction disappeared"
        );
    }

    private static void assertPlanningOnlyRegistries(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertFalse(
                level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                        .containsKey(Surface02Plan.SURFACE_02_ID),
                "Surface 02 became a configured feature"
        );
        helper.assertFalse(
                level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                        .containsKey(Surface02Plan.SURFACE_02_ID),
                "Surface 02 became a placed feature"
        );
        boolean biomeModifierPresent = level.registryAccess()
                .registry(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                .map(registry -> registry.containsKey(Surface02Plan.SURFACE_02_ID))
                .orElse(false);
        helper.assertFalse(biomeModifierPresent, "Surface 02 became a biome modifier");
    }

    private static void assertRefusal(
            GameTestHelper helper,
            String caseName,
            BlockPos origin,
            SiteQuality quality,
            BuddingResourceFamily family,
            Surface02BiomeFamily biome,
            long seed,
            Surface02Composer.Status expectedStatus
    ) {
        ServerLevel level = helper.getLevel();
        BlockPos sentinel = origin.offset(0, 0, 2);
        setBlock(level, sentinel, Blocks.STONE.defaultBlockState());
        BlockState sentinelBefore = level.getBlockState(sentinel);
        int pendingBefore = IoePendingExpeditionSites.pendingResourcePositionCount();
        int authorizationBefore = IoeOrePlacementAuthorization.positionCount();

        Surface02Composer.Decision decision = Surface02Composer.resolveAndCompose(
                quality,
                family,
                seed,
                biome,
                origin,
                Surface02TerrainContext.canonicalEnvelope(origin)
        );
        helper.assertTrue(
                decision.status() == expectedStatus,
                caseName + " expected " + expectedStatus + " but got " + decision.status()
        );
        helper.assertTrue(decision.plan().isEmpty(), caseName + " produced a refused plan");
        helper.assertTrue(level.getBlockState(sentinel).equals(sentinelBefore), caseName + " mutated the world");
        helper.assertTrue(
                IoePendingExpeditionSites.pendingResourcePositionCount() == pendingBefore,
                caseName + " changed pending resource reservations"
        );
        helper.assertTrue(
                IoeOrePlacementAuthorization.positionCount() == authorizationBefore,
                caseName + " changed ore placement authorizations"
        );
        LOGGER.info(
                "IOE_RUNTIME refusal case={} seed={} quality={} biome={} family={} status={} "
                        + "plan=false placementApiInvoked=false pendingReservationDelta=0 authorizationDelta=0",
                caseName,
                seed,
                quality,
                biome,
                family,
                decision.status()
        );
    }

    private static String placeForDigest(GameTestHelper helper, BlockPos origin, long seed) {
        Surface02Composer.Decision first = Surface02Composer.resolveAndCompose(
                SiteQuality.NORMAL,
                BuddingResourceFamily.GEORE_IRON,
                seed,
                Surface02BiomeFamily.TEMPERATE,
                origin,
                Surface02TerrainContext.canonicalEnvelope(origin)
        );
        Surface02Composer.Decision second = Surface02Composer.resolveAndCompose(
                SiteQuality.NORMAL,
                BuddingResourceFamily.GEORE_IRON,
                seed,
                Surface02BiomeFamily.TEMPERATE,
                origin,
                Surface02TerrainContext.canonicalEnvelope(origin)
        );
        helper.assertTrue(first.equals(second), "Composer is not deterministic for seed " + seed);
        return placeAndAssert(helper, "determinism_" + seed, first.plan().orElseThrow(), false).digest();
    }

    private static GrowthObservation assertDecayStep(
            GameTestHelper helper,
            ServerLevel level,
            BlockPos center,
            BuddingRank startingRank,
            ResourceLocation expectedTarget
    ) {
        ResourceLocation startingId = IoeBuddingBlocks.blockId("iron", startingRank);
        GeOreRankedBuddingBlock block = requireGeOreRankedBlock(startingId);
        long selectedSeed = -1L;
        ResourceLocation growthProduct = null;
        for (long seed = 0; seed < RANDOM_TICK_SEED_LIMIT; seed++) {
            setBlock(level, center, block.defaultBlockState());
            clearGrowthCells(level, center);
            block.randomTick(level.getBlockState(center), level, center, RandomSource.create(seed));
            List<ResourceLocation> grown = growthProducts(level, center);
            if (grown.equals(List.of(id("geore", "small_iron_bud")))
                    && blockId(level.getBlockState(center)).equals(expectedTarget)) {
                selectedSeed = seed;
                growthProduct = grown.getFirst();
                break;
            }
        }
        helper.assertTrue(
                selectedSeed >= 0,
                startingRank + " did not grow and degrade exactly once within the strict seed bound"
        );
        LOGGER.info(
                "IOE_RUNTIME randomTick start={} seed={} limit={} growth={} target={}",
                startingRank,
                selectedSeed,
                RANDOM_TICK_SEED_LIMIT,
                growthProduct,
                expectedTarget
        );
        return new GrowthObservation(selectedSeed, growthProduct);
    }

    private static void assertTags(GameTestHelper helper, Set<ResourceLocation> expectedIds) {
        for (TagKey<Block> tag : REQUIRED_BLOCK_TAGS) {
            for (ResourceLocation expectedId : expectedIds) {
                Block block = BuiltInRegistries.BLOCK.getOptional(expectedId).orElseThrow();
                helper.assertTrue(
                        BuiltInRegistries.BLOCK.wrapAsHolder(block).is(tag),
                        expectedId + " is missing from block tag " + tag.location()
                );
            }
            long observed = BuiltInRegistries.BLOCK.getTag(tag).orElseThrow().stream().count();
            LOGGER.info("IOE_RUNTIME blockTag={} observedEntries={}", tag.location(), observed);
        }
        for (TagKey<Item> tag : REQUIRED_ITEM_TAGS) {
            for (ResourceLocation expectedId : expectedIds) {
                Item item = BuiltInRegistries.ITEM.getOptional(expectedId).orElseThrow();
                helper.assertTrue(
                        BuiltInRegistries.ITEM.wrapAsHolder(item).is(tag),
                        expectedId + " is missing from item tag " + tag.location()
                );
            }
            long observed = BuiltInRegistries.ITEM.getTag(tag).orElseThrow().stream().count();
            LOGGER.info("IOE_RUNTIME itemTag={} observedEntries={}", tag.location(), observed);
        }
    }

    private static void assertRecipes(GameTestHelper helper, Set<ResourceLocation> expectedRankedIds) {
        ServerLevel level = helper.getLevel();
        Set<ResourceLocation> expectedFlawlessOutputs = expectedRankedIds.stream()
                .filter(id -> id.getPath().startsWith("flawless_budding_"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        expectedFlawlessOutputs.add(id("ae2", "flawless_budding_quartz"));
        int checked = 0;
        for (String material : IoeBuddingBlocks.GEORE_MATERIALS) {
            ResourceLocation storage = id("geore", material + "_block");
            ResourceLocation damaged = IoeBuddingBlocks.blockId(material, BuddingRank.DAMAGED);
            ResourceLocation chipped = IoeBuddingBlocks.blockId(material, BuddingRank.CHIPPED);
            ResourceLocation flawed = IoeBuddingBlocks.blockId(material, BuddingRank.FLAWED);
            checked += assertAggregatorRecipe(helper, material, BuddingRank.DAMAGED, storage, damaged);
            checked += assertAggregatorRecipe(helper, material, BuddingRank.CHIPPED, damaged, chipped);
            checked += assertAggregatorRecipe(helper, material, BuddingRank.FLAWED, chipped, flawed);
        }
        helper.assertTrue(checked == 39, "Expected to inspect exactly 39 IOE aggregator recipes");
        for (ResourceLocation disabled : DISABLED_EXTERNAL_RECIPES) {
            helper.assertTrue(
                    level.getRecipeManager().byKey(disabled).isEmpty(),
                    "Disabled external top-rank producer is active: " + disabled
            );
        }
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            ItemStack result = holder.value().getResultItem(level.registryAccess());
            if (!result.isEmpty()) {
                ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
                helper.assertFalse(
                        expectedFlawlessOutputs.contains(resultId)
                                || resultId.equals(id("extendedae", "entro_budding_fully")),
                        "Loaded recipe " + holder.id() + " produces forbidden maximum rank " + resultId
                );
            }
        }
    }

    private static int assertAggregatorRecipe(
            GameTestHelper helper,
            String material,
            BuddingRank outputRank,
            ResourceLocation expectedInputC,
            ResourceLocation expectedResult
    ) {
        ResourceLocation recipeId = id(
                ImmersiveOreExpeditionMod.MODID,
                "aggregator/" + outputRank.path() + "_budding_" + material
        );
        RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(recipeId).orElseThrow();
        Object recipe = holder.value();
        helper.assertTrue(
                recipe.getClass().getName().endsWith("CrystalAggregatorRecipe"),
                recipeId + " loaded the wrong concrete recipe type: " + recipe.getClass().getName()
        );
        helper.assertTrue(
                ((Number) invoke(recipe, "energyCost")).intValue() == 16_000,
                recipeId + " has the wrong energy cost"
        );
        assertSizedIngredient(helper, recipeId, invoke(recipe, "inputA"), Set.of(RESONATING_SEED));
        Set<ResourceLocation> resonatingCrystals = itemsInTag(RESONATING_CRYSTAL_TAG);
        helper.assertTrue(
                resonatingCrystals.equals(Set.of(PURIFIED_RESONATING_CRYSTAL)),
                "Pinned resonating-crystal tag is empty or changed: " + resonatingCrystals
        );
        assertSizedIngredient(
                helper,
                recipeId,
                invoke(recipe, "inputB"),
                resonatingCrystals
        );
        assertSizedIngredient(helper, recipeId, invoke(recipe, "inputC"), Set.of(expectedInputC));
        Object resultObject = invoke(recipe, "result");
        helper.assertTrue(resultObject instanceof ItemStack, recipeId + " result() is not an ItemStack");
        ItemStack result = (ItemStack) resultObject;
        helper.assertTrue(result.getCount() == 1, recipeId + " result count is not one");
        helper.assertTrue(
                BuiltInRegistries.ITEM.getKey(result.getItem()).equals(expectedResult),
                recipeId + " result is " + BuiltInRegistries.ITEM.getKey(result.getItem())
        );
        return 1;
    }

    private static void assertSizedIngredient(
            GameTestHelper helper,
            ResourceLocation recipeId,
            Object sizedIngredient,
            Set<ResourceLocation> expectedItems
    ) {
        helper.assertTrue(
                ((Number) invoke(sizedIngredient, "count")).intValue() == 1,
                recipeId + " has an ingredient count other than one"
        );
        Object values = invoke(sizedIngredient, "getItems");
        Set<ResourceLocation> actualItems = new LinkedHashSet<>();
        int length = Array.getLength(values);
        for (int index = 0; index < length; index++) {
            Object value = Array.get(values, index);
            helper.assertTrue(value instanceof ItemStack, recipeId + " ingredient is not an ItemStack");
            actualItems.add(BuiltInRegistries.ITEM.getKey(((ItemStack) value).getItem()));
        }
        helper.assertTrue(
                actualItems.equals(expectedItems),
                recipeId + " ingredient mismatch: expected=" + expectedItems + " actual=" + actualItems
        );
    }

    private static Set<ResourceLocation> itemsInTag(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTag(tag).orElseThrow().stream()
                .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException failure) {
            throw new IllegalStateException(
                    "Could not inspect runtime recipe method "
                            + target.getClass().getName() + "#" + methodName,
                    failure
            );
        }
    }

    private static Map<BlockPos, BlockState> snapshotOutsidePlan(ServerLevel level, Surface02Plan plan) {
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        LinkedHashMap<BlockPos, BlockState> snapshot = new LinkedHashMap<>();
        for (int x = bounds.minX() - 1; x <= bounds.maxX() + 1; x++) {
            for (int y = bounds.minY() - 1; y <= bounds.maxY() + 1; y++) {
                for (int z = bounds.minZ() - 1; z <= bounds.maxZ() + 1; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!plan.blocks().containsKey(pos)) {
                        snapshot.put(pos, level.getBlockState(pos));
                    }
                }
            }
        }
        return snapshot;
    }

    private static Set<UUID> entityIds(ServerLevel level, Surface02Plan plan) {
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        AABB box = new AABB(
                bounds.minX(),
                bounds.minY(),
                bounds.minZ(),
                bounds.maxX() + 1,
                bounds.maxY() + 1,
                bounds.maxZ() + 1
        );
        return level.getEntities((Entity) null, box, entity -> true).stream()
                .map(entity -> entity.getUUID())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizedDigest(
            ServerLevel level,
            BlockPos origin,
            Map<BlockPos, BlockState> expectedPositions
    ) {
        List<String> lines = new ArrayList<>();
        for (BlockPos pos : expectedPositions.keySet()) {
            BlockPos relative = pos.subtract(origin);
            BlockState state = level.getBlockState(pos);
            lines.add(relative.getX() + "," + relative.getY() + "," + relative.getZ()
                    + "=" + blockId(state) + state.getValues());
        }
        lines.sort(Comparator.naturalOrder());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static void clearGrowthCells(ServerLevel level, BlockPos center) {
        for (Direction direction : Direction.values()) {
            setBlock(level, center.relative(direction), Blocks.AIR.defaultBlockState());
        }
    }

    /**
     * The shared empty template sits over generated terrain. Filling the bounded test envelope with
     * replaceable stone proves that every expected AIR cell is excavated by the production writer.
     */
    private static void preparePlacementFixture(ServerLevel level, Surface02Plan plan) {
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    level.getChunk(pos);
                    setBlock(level, pos, Blocks.STONE.defaultBlockState());
                }
            }
        }
    }

    private static List<ResourceLocation> growthProducts(ServerLevel level, BlockPos center) {
        List<ResourceLocation> products = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(center.relative(direction));
            if (!state.isAir()) {
                products.add(blockId(state));
            }
        }
        return List.copyOf(products);
    }

    private static Map<ResourceLocation, Integer> censusEnvelope(ServerLevel level, Surface02Plan plan) {
        Surface02TerrainContext.Bounds bounds = Surface02TerrainContext.canonicalEnvelope(plan.origin())
                .allowedBounds();
        Map<ResourceLocation, Integer> census = new HashMap<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    ResourceLocation id = blockId(level.getBlockState(new BlockPos(x, y, z)));
                    census.merge(id, 1, Integer::sum);
                }
            }
        }
        return Map.copyOf(census);
    }

    private static Set<ResourceLocation> allRankedBuddingIds() {
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>();
        for (String material : IoeBuddingBlocks.GEORE_MATERIALS) {
            for (BuddingRank rank : BuddingRank.values()) {
                ids.add(IoeBuddingBlocks.blockId(material, rank));
            }
        }
        for (BuddingRank rank : BuddingRank.values()) {
            ids.add(id("ae2", rank.path() + "_budding_quartz"));
        }
        return Set.copyOf(ids);
    }

    private static Set<ResourceLocation> allMineralIds() {
        LinkedHashSet<ResourceLocation> ids = IoeBuddingBlocks.GEORE_MATERIALS.stream()
                .map(material -> id("geore", material + "_block"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        ids.add(id("ae2", "quartz_block"));
        return Set.copyOf(ids);
    }

    private static GeOreRankedBuddingBlock requireGeOreRankedBlock(ResourceLocation blockId) {
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElseThrow();
        if (!(block instanceof GeOreRankedBuddingBlock ranked)) {
            throw new IllegalStateException(blockId + " is not a GeOreRankedBuddingBlock");
        }
        return ranked;
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.setBlock(pos, state, 3) && !level.getBlockState(pos).equals(state)) {
            throw new IllegalStateException("Could not prepare GameTest fixture at " + pos);
        }
    }

    private static BlockState resolveBlockState(ResourceLocation blockId) {
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .orElseThrow(() -> new IllegalStateException("Missing runtime block " + blockId))
                .defaultBlockState();
    }

    private static BlockPos primaryOrigin(GameTestHelper helper) {
        BlockPos reference = helper.absolutePos(new BlockPos(0, 24, 0));
        ChunkPos chunk = new ChunkPos(reference);
        return new BlockPos(chunk.getMinBlockX() + 7, reference.getY(), chunk.getMinBlockZ());
    }

    private static void assertLoaded(GameTestHelper helper, String modId) {
        helper.assertTrue(ModList.get().isLoaded(modId), "Required runtime mod is not loaded: " + modId);
        LOGGER.info(
                "IOE_RUNTIME mod={} version={}",
                modId,
                ModList.get().getModContainerById(modId)
                        .map(container -> container.getModInfo().getVersion().toString())
                        .orElse("MISSING")
        );
    }

    private static ResourceLocation blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private record PlacedCase(
            Surface02Plan plan,
            ExpeditionSiteBlockPlan adapted,
            String digest
    ) {
    }

    private record GrowthObservation(long seed, ResourceLocation growthProduct) {
    }
}
