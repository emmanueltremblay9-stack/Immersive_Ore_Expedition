package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.*;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class CertusBuddingGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void certusCanonicalGeometryRanksAndDeterminism(GameTestHelper helper) {
        BlockPos origin = new BlockPos(8, 80, 8);
        for (int seed = 0; seed < 64; seed++) {
            var mother = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, RandomSource.create(seed), 0);
            for (SiteQuality quality : SiteQuality.values()) {
                if (!quality.isProductive()) continue;
                var budget = quality == SiteQuality.MOTHERLODE ? mother : mother.downgradeTo(quality, 0);
                var context = ProspectorCampContext.vanillaFallback(origin, quality);
                var plan = CertusBuddingSitePlans.plan(ExpeditionSiteType.MINER_CAMP, origin, budget, seed, context);
                helper.assertTrue(plan.equals(CertusBuddingSitePlans.plan(ExpeditionSiteType.MINER_CAMP,
                        origin, budget, seed, context)), "Certus planning is not deterministic");
                verify(helper, plan, budget);
            }
        }
        var selected = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, new LegacyRandomSource(0) {
            @Override public int nextInt(int bound) { return 0; }
        }, 0);
        for (SiteQuality quality : SiteQuality.values()) {
            if (!quality.isProductive()) continue;
            var budget = quality == SiteQuality.MOTHERLODE ? selected : selected.downgradeTo(quality, 0);
            verify(helper, CertusBuddingSitePlans.plan(ExpeditionSiteType.MINER_CAMP, origin, budget, 42,
                    ProspectorCampContext.vanillaFallback(origin, quality)), budget);
        }
        helper.succeed();
    }

    private static void verify(GameTestHelper helper, ExpeditionSiteBlockPlan plan, BuddingSitePlan budget) {
        var ore = BuiltInRegistries.BLOCK.getOptional(NativeCertusBudding.QUARTZ).orElseThrow();
        helper.assertTrue(plan.oreNodeHeartCount() == budget.nodeRanks().size(), "Wrong native heart count");
        helper.assertTrue(plan.blocks().values().stream().filter(state -> state.is(ore)).count() == budget.totalOreBlocks(),
                "Wrong surrounding quartz budget");
        helper.assertTrue(plan.oreBlockCount() == budget.totalOreBlocks() + budget.nodeRanks().size(), "Lost mixed-rank accounting");
        for (BuddingRank rank : BuddingRank.values()) {
            helper.assertTrue(plan.blocks().values().stream().filter(state -> state.is(NativeCertusBudding.block(rank))).count()
                    == budget.nodeRanks().stream().filter(expected -> expected == rank).count(), "Wrong native rank population");
        }
        var metadata = BuddingPlanMetadata.nodes(plan);
        helper.assertTrue(metadata.size() == budget.nodeRanks().size(), "Missing Certus metadata");
        for (int i = 0; i < metadata.size(); i++) {
            var node = metadata.get(i);
            helper.assertTrue(node.index() == i + 1 && node.count() == metadata.size()
                    && node.initialOre() == budget.oreBlocksPerNode() && node.family().equals(NativeCertusBudding.FAMILY),
                    "Invalid native node provenance");
        }
        helper.assertTrue(plan.isConnectedExpeditionSite(), "Disconnected Certus site");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void certusDryLimitsAndProductiveRollback(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        for (int count = 0; count <= 5; count++) {
            var shape = ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER, origin,
                    SiteQuality.DRY, null, null, RandomSource.create(0));
            var pocket = DrySitePockets.attach(shape, NativeCertusBudding.QUARTZ, count, 42);
            helper.assertTrue(pocket.equals(DrySitePockets.attach(shape, NativeCertusBudding.QUARTZ, count, 42)),
                    "Non-deterministic DRY Certus pocket");
            helper.assertTrue(pocket.oreBlockCount() == count && pocket.oreNodeHeartCount() == 0
                    && BuddingPlanMetadata.nodes(pocket).isEmpty(), "Invalid DRY Certus budget");
            for (boolean reward : new boolean[]{false, true}) {
                var plan = DrySiteRewards.attach(pocket, reward, 42);
                helper.assertTrue(plan.oreBlockCount() == count && plan.blockEntityPayloads().size() == (reward ? 1 : 0),
                        "Seed reward changed the DRY residue count");
                rollback(helper, plan);
            }
        }
        var budget = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, new LegacyRandomSource(0) {
            @Override public int nextInt(int bound) { return 0; }
        }, 0);
        rollback(helper, CertusBuddingSitePlans.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER, origin,
                budget, 42, ProspectorCampContext.vanillaFallback(origin, SiteQuality.MOTHERLODE)));
        helper.succeed();
    }

    private static void rollback(GameTestHelper helper, ExpeditionSiteBlockPlan plan) {
        var level = helper.getLevel();
        plan.blocks().keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()));
        var applied = IoeExpeditionPlanPlacement.apply(level, plan).orElseThrow();
        helper.assertTrue(applied.rollback(level) && applied.rollback(level), "Certus rollback is not idempotent");
        helper.assertTrue(plan.blocks().keySet().stream().allMatch(pos -> level.getBlockState(pos).is(Blocks.STONE)),
                "Rollback left a Certus heart, residue or reward");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(plan.chamberCenter()).inflate(12)).isEmpty(),
                "Rollback dropped/duplicated resources");
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void certusAndEntroFeatureTransactions(GameTestHelper helper) throws java.io.IOException {
        var level = helper.getLevel();
        int index = 0;
        for (String profileName : List.of("certus", "entroized_fluix")) {
            for (SiteQuality requested : SiteQuality.values()) {
                if (requested.isProductive() && !ModList.get().isLoaded("immersiveengineering")) continue;
                ExpeditionLocatorService.index(level).clear();
                ChunkPos chunk = new ChunkPos(helper.absolutePos(new BlockPos(128 + index++ * 128, 24, 128)));
                BlockPos origin = new BlockPos(chunk.getMinBlockX() + 4, 41, chunk.getMinBlockZ() + 6);
                ExpeditionWorldgenGameTests.fillTestChunk(level, chunk);
                var id = ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, profileName);
                var definition = level.registryAccess().registryOrThrow(BiomeMineResourceDefinition.REGISTRY_KEY).getOptional(id).orElseThrow();
                var profile = new BiomeMineResourceProfile(ResourceLocation.parse("minecraft:plains"), id, definition, 1);
                long seed = 0;
                for (;;) {
                    var random = RandomSource.create(seed);
                    var quality = SiteQualityRoll.DEFAULT.roll(random);
                    long planSeed = random.nextLong();
                    if (quality == requested && (quality != SiteQuality.DRY
                            || DrySiteReward.roll(quality, RandomSource.create(planSeed ^ 0x53454544L)))) break;
                    seed++;
                }
                var random = RandomSource.create(seed);
                SiteQualityRoll.DEFAULT.roll(random);
                long planSeed = random.nextLong();
                boolean staged = new ExpeditionSiteFeature(ExpeditionSiteType.MINER_CAMP,
                        (ignoredLevel, ignoredPos) -> new BiomeMineResourceProfile.Resolution(Optional.of(profile), BiomeMineResourceProfile.Failure.NONE)).place(
                        new FeaturePlaceContext<>(Optional.empty(), level, level.getChunkSource().getGenerator(),
                                RandomSource.create(seed), origin, NoneFeatureConfiguration.INSTANCE));
                helper.assertTrue(staged, "Special-profile Feature.place failed: " + profileName + " " + requested);
                helper.assertTrue(ExpeditionLocatorService.index(level).sites().isEmpty(), "Provisional metadata escaped staging");
                helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level, chunk).confirmedSites() == 1, "Commit failed");
                com.oblixorprime.ioe.expeditionlocator.BuddingPersistenceRuntimeChecks.reloadFromDisk(level);
                var site = ExpeditionLocatorService.index(level).sites().stream().filter(value -> value.pos().equals(origin)).findFirst().orElseThrow();
                SiteQuality actual = site.quality().orElseThrow();
                var budget = BuddingSitePlan.forQuality(actual, RandomSource.create(planSeed ^ 0x49524f4eL), 0);
                boolean certus = profileName.equals("certus");
                helper.assertTrue(site.buddingNodes().size() == (certus ? budget.nodeRanks().size() : 0), "Wrong persisted heart count");
                long quartz = 0;
                int seeds = 0;
                var seedItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("ae2cs:resonating_seed")).orElseThrow();
                var quartzBlock = BuiltInRegistries.BLOCK.getOptional(NativeCertusBudding.QUARTZ).orElseThrow();
                for (BlockPos pos : BlockPos.betweenClosed(chunk.getMinBlockX(), 0, chunk.getMinBlockZ(), chunk.getMaxBlockX(), 47, chunk.getMaxBlockZ())) {
                    var state = level.getBlockState(pos);
                    if (state.is(quartzBlock)) quartz++;
                    if (!certus) helper.assertTrue(!BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("extendedae")
                            && !state.is(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("ae2:fluix_block"))), "Entro acquired physical residues/hearts");
                    if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
                        for (int slot = 0; slot < container.getContainerSize(); slot++) if (container.getItem(slot).is(seedItem)) seeds += container.getItem(slot).getCount();
                    }
                }
                helper.assertTrue(quartz == (certus ? actual == SiteQuality.DRY ? DryPocketRoll.forSite(planSeed) : budget.totalOreBlocks() : 0),
                        "Wrong committed Certus/Entro physical budget");
                helper.assertTrue(seeds == (actual == SiteQuality.DRY ? 1 : 0), "Special-profile seed reward lost or duplicated");
                for (var node : site.buddingNodes()) helper.assertTrue(node.family().equals(NativeCertusBudding.FAMILY)
                        && node.initialOre() == budget.oreBlocksPerNode(), "Wrong persisted Certus identity");
                helper.assertTrue(IoePendingExpeditionSites.confirmLoadedChunk(level, chunk).confirmedSites() == 0, "Reconfirmed a consumed transaction");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void nativeFlawlessReplacementIsNewChunkOnly(GameTestHelper helper) {
        var level = helper.getLevel();
        ChunkPos oldChunk = new ChunkPos(helper.absolutePos(new BlockPos(80, 24, 80)));
        ChunkPos newChunk = new ChunkPos(oldChunk.x + 4, oldChunk.z);
        var flawless = NativeCertusBudding.block(BuddingRank.FLAWLESS).defaultBlockState();
        var flawed = NativeCertusBudding.block(BuddingRank.FLAWED);
        BlockPos oldPos = new BlockPos(oldChunk.getMinBlockX() + 8, 30, oldChunk.getMinBlockZ() + 8);
        BlockPos newPos = new BlockPos(newChunk.getMinBlockX() + 8, 30, newChunk.getMinBlockZ() + 8);
        // Finish any first-load work caused by allocating the test chunk before treating it as existing.
        level.getChunk(oldChunk.x, oldChunk.z);
        IoeNewChunkOreGuard.sanitizeLoadedChunk(level, oldChunk, true);
        level.setBlockAndUpdate(oldPos, flawless);
        level.setBlockAndUpdate(newPos, flawless);
        IoeNewChunkOreGuard.scheduleChunk(level, oldChunk, false);
        helper.assertFalse(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, oldChunk, true), "Existing chunk entered guard");
        helper.assertTrue(level.getBlockState(oldPos).equals(flawless), "Existing meteorite was retroactively changed");
        List<String> preserved = List.of("damaged_budding_quartz", "chipped_budding_quartz", "flawed_budding_quartz",
                "sky_stone_block", "fluix_block", "quartz_block", "small_quartz_bud", "medium_quartz_bud", "large_quartz_bud", "quartz_cluster");
        for (int i = 0; i < preserved.size(); i++) {
            var state = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("ae2:" + preserved.get(i))).orElseThrow().defaultBlockState();
            level.setBlock(newPos.offset(i % 5 - 2, 2, i / 5), state, 2);
        }
        IoeNewChunkOreGuard.scheduleChunk(level, newChunk, true);
        helper.assertTrue(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, newChunk, false), "New chunk was not scanned");
        helper.assertTrue(level.getBlockState(newPos).is(flawed), "New meteorite Flawless was not replaced by native Flawed");
        for (int i = 0; i < preserved.size(); i++) helper.assertTrue(BuiltInRegistries.BLOCK.getKey(level.getBlockState(newPos.offset(i % 5 - 2, 2, i / 5)).getBlock())
                .equals(ResourceLocation.parse("ae2:" + preserved.get(i))), "Guard changed normal meteorite progression");
        IoeNewChunkOreGuard.sanitizeLoadedChunk(level, newChunk, true);
        // A later load must not retroactively sanitize even a newly placed Flawless.
        level.setBlockAndUpdate(newPos, flawless);
        IoeNewChunkOreGuard.scheduleChunk(level, newChunk, false);
        helper.assertFalse(IoeNewChunkOreGuard.sanitizeLoadedChunk(level, newChunk, true), "Reload incorrectly restarted replacement");
        helper.assertTrue(level.getBlockState(newPos).equals(flawless), "Reload changed an existing block");
        ChunkPos authorizedChunk = new ChunkPos(newChunk.x + 4, newChunk.z);
        BlockPos origin = new BlockPos(authorizedChunk.getMinBlockX() + 4, 41, authorizedChunk.getMinBlockZ() + 6);
        var budget = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, new LegacyRandomSource(0) {
            @Override public int nextInt(int bound) { return 0; }
        }, 0);
        var plan = CertusBuddingSitePlans.plan(ExpeditionSiteType.MINER_CAMP, origin, budget, 42,
                ProspectorCampContext.vanillaFallback(origin, SiteQuality.MOTHERLODE));
        // Exact IOE placement authorization protects the selected Motherlode heart in both passes.
        IoeOrePlacementAuthorization.authorize(level.dimension(), plan);
        for (var node : BuddingPlanMetadata.nodes(plan)) level.setBlock(node.pos(), plan.blocks().get(node.pos()), 2);
        IoeNewChunkOreGuard.scheduleChunk(level, authorizedChunk, true);
        IoeNewChunkOreGuard.sanitizeLoadedChunk(level, authorizedChunk, false);
        IoeNewChunkOreGuard.sanitizeLoadedChunk(level, authorizedChunk, true);
        helper.assertTrue(BuddingPlanMetadata.nodes(plan).stream().allMatch(node -> level.getBlockState(node.pos()).equals(plan.blocks().get(node.pos()))),
                "Guard destroyed an authorized IOE Motherlode heart");
        helper.succeed();
    }
}
