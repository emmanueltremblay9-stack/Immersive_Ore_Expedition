package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.budding.BuddingSitePlan;
import com.oblixorprime.ioe.budding.GeOreBuddingBlock;
import com.oblixorprime.ioe.budding.IoeGeOreBuddingBlocks;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class GeOreBuddingSiteGameTests {
    private GeOreBuddingSiteGameTests() { }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 400)
    public static void geOrePlansPreserveCanonicalBudgetsAndMetadata(GameTestHelper helper) {
        BlockPos origin = new BlockPos(8, 80, 8);
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
            for (int seed = 0; seed < 64; seed++) {
                BuddingSitePlan initial = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, RandomSource.create(seed), 0);
                for (SiteQuality quality : SiteQuality.values()) {
                    if (!quality.isProductive()) continue;
                    BuddingSitePlan budget = quality == SiteQuality.MOTHERLODE ? initial : initial.downgradeTo(quality, 0);
                    ExpeditionSiteBlockPlan plan = GeOreBuddingSitePlans.plan(family, ExpeditionSiteType.MINER_CAMP,
                            origin, budget, seed, ProspectorCampContext.vanillaFallback(origin, quality));
                    verifyPlan(helper, family, budget, plan);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreFlawlessFallbackAndAbsentFamilyRefusal(GameTestHelper helper) {
        BlockPos origin = new BlockPos(8, 80, 8);
        BuddingSitePlan mother = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE,
                new LegacyRandomSource(0) {
                    @Override public int nextInt(int bound) { return 0; }
                }, 0);
        helper.assertTrue(mother.nodeRanks().getFirst() == BuddingRank.FLAWLESS, "Fixture must exercise a selected Flawless");
        BuddingSitePlan poor = mother.downgradeTo(SiteQuality.POOR, 0);
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) continue;
            if (!IoeGeOreBuddingBlocks.available(family)) {
                boolean rejected = false;
                try {
                    GeOreBuddingSitePlans.plan(family, ExpeditionSiteType.MINER_CAMP, origin, mother, 42,
                            ProspectorCampContext.vanillaFallback(origin, SiteQuality.MOTHERLODE));
                } catch (IllegalStateException expected) {
                    rejected = true;
                }
                helper.assertTrue(rejected, "Unavailable family must not silently use a different mineral: " + family);
                continue;
            }
            ExpeditionSiteBlockPlan selected = GeOreBuddingSitePlans.plan(family, ExpeditionSiteType.MINER_CAMP,
                    origin, mother, 42, ProspectorCampContext.vanillaFallback(origin, SiteQuality.MOTHERLODE));
            verifyPlan(helper, family, mother, selected);
            ExpeditionSiteBlockPlan downgraded = GeOreBuddingSitePlans.plan(family, ExpeditionSiteType.MINER_CAMP,
                    origin, poor, 42, ProspectorCampContext.vanillaFallback(origin, SiteQuality.POOR));
            verifyPlan(helper, family, poor, downgraded);
            helper.assertTrue(downgraded.blocks().values().stream().noneMatch(state ->
                            state.getBlock() instanceof GeOreBuddingBlock block && block.rank() == BuddingRank.FLAWLESS),
                    "Fallback must discard Flawless without retaining an eighth or orphaned heart");
        }
        helper.succeed();
    }

    private static void verifyPlan(GameTestHelper helper, BuddingResourceFamily family,
                                   BuddingSitePlan budget, ExpeditionSiteBlockPlan plan) {
        String context = family + ": " + budget.quality();
        var ore = BuiltInRegistries.BLOCK.getOptional(family.pocketBlockId()).orElseThrow();
        helper.assertTrue(plan.quality() == budget.quality(), "Wrong final quality: " + context);
        helper.assertTrue(plan.oreNodeHeartCount() == budget.nodeRanks().size(), "Lost hearts: " + context);
        helper.assertTrue(plan.blocks().values().stream().filter(state -> state.is(ore)).count() == budget.totalOreBlocks(),
                "Wrong ore budget: " + context);
        helper.assertTrue(plan.oreBlockCount() == budget.totalOreBlocks() + budget.nodeRanks().size(),
                "Legacy total must include all ranks: " + context);
        var hearts = plan.blocks().entrySet().stream()
                .filter(entry -> entry.getValue().getBlock() instanceof GeOreBuddingBlock).toList();
        helper.assertTrue(hearts.size() == budget.nodeRanks().size(), "Extra or missing hearts: " + context);
        for (BuddingRank rank : BuddingRank.values()) {
            helper.assertTrue(hearts.stream().filter(entry -> ((GeOreBuddingBlock) entry.getValue().getBlock()).rank() == rank).count()
                            == budget.nodeRanks().stream().filter(expected -> expected == rank).count(),
                    "Wrong rank population: " + context + ": " + rank);
        }
        var metadata = BuddingPlanMetadata.nodes(plan);
        helper.assertTrue(metadata.size() == hearts.size(), "Missing node metadata: " + context);
        helper.assertTrue(metadata.stream().map(node -> node.pos()).distinct().count() == hearts.size(),
                "Duplicate node metadata: " + context);
        for (int index = 0; index < metadata.size(); index++) {
            var node = metadata.get(index);
            helper.assertTrue(node.index() == index + 1 && node.count() == budget.nodeRanks().size()
                            && node.initialOre() == budget.oreBlocksPerNode() && node.family().equals(family.identity()),
                    "Incorrect canonical per-node metadata: " + context);
            helper.assertTrue(plan.blocks().get(node.pos()).getBlock() instanceof GeOreBuddingBlock block
                            && block.family() == family,
                    "Metadata must identify an actual heart of its family: " + context);
        }
        helper.assertTrue(plan.isConnectedExpeditionSite(), "Disconnected site envelope: " + context);
    }
}
