package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingSitePlan;
import com.oblixorprime.ioe.budding.IronBuddingBlock;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class IronBuddingSiteGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void canonicalIronPlansRetainBudgetsAcrossFallback(GameTestHelper helper) {
        var ore = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("geore:iron_block")).orElseThrow();
        BlockPos origin = new BlockPos(8, 80, 8);
        for (int seed = 0; seed < 64; seed++) {
            BuddingSitePlan initial = BuddingSitePlan.forQuality(SiteQuality.MOTHERLODE, RandomSource.create(seed), 0);
            for (SiteQuality quality : SiteQuality.values()) {
                if (!quality.isProductive()) continue;
                BuddingSitePlan budget = quality == SiteQuality.MOTHERLODE ? initial : initial.downgradeTo(quality, 0);
                ExpeditionSiteBlockPlan plan = IronBuddingSitePlans.plan(ExpeditionSiteType.MINER_CAMP, origin, budget, seed,
                        ProspectorCampContext.vanillaFallback(origin, quality));
                helper.assertTrue(plan.oreNodeHeartCount() == budget.nodeRanks().size(), "Lost Iron hearts");
                helper.assertTrue(plan.blocks().values().stream().filter(state -> state.is(ore)).count() == budget.totalOreBlocks(),
                        "Lost canonical ore budget");
                helper.assertTrue(plan.oreBlockCount() == budget.totalOreBlocks() + budget.nodeRanks().size(),
                        "Legacy total must count all heart ranks");
                long flawless = plan.blocks().values().stream().filter(state -> state.getBlock() instanceof IronBuddingBlock block
                        && block.rank() == BuddingRank.FLAWLESS).count();
                helper.assertTrue(flawless == budget.nodeRanks().stream().filter(rank -> rank == BuddingRank.FLAWLESS).count(),
                        "Geometry changed the single Flawless selection");
                helper.assertTrue(plan.isConnectedExpeditionSite(), "Iron site must retain its connected envelope");
            }
        }
        helper.succeed();
    }
}
