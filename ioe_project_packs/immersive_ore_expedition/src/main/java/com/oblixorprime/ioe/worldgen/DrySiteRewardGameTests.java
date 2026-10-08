package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class DrySiteRewardGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void dryRewardUsesTheSamePlacementRollbackAsTheSite(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        var shape = ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER, origin,
                SiteQuality.DRY, null, null, RandomSource.create(0));
        helper.assertTrue(DrySiteRewards.attach(shape, false, 0) == shape,
                "A failed reward draw must not change the plan");
        var selected = DrySiteRewards.attach(shape, true, 42);
        helper.assertTrue(shape.blockEntityPayloads().isEmpty() && selected.blockEntityPayloads().size() == 1,
                "Reward planning must preserve the source and add exactly one payload");
        var level = helper.getLevel();
        selected.blocks().keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()));
        var applied = IoeExpeditionPlanPlacement.apply(level, selected).orElseThrow();
        helper.assertTrue(level.getBlockState(origin.below()).is(Blocks.CHEST)
                        && level.getBlockEntity(origin.below()) != null, "Reward container was not placed");
        helper.assertTrue(applied.rollback(level), "Reward placement failed compensation");
        helper.assertTrue(selected.blocks().keySet().stream().allMatch(pos -> level.getBlockState(pos).is(Blocks.STONE)),
                "Compensation did not restore the complete pre-placement state");
        helper.assertTrue(level.getBlockEntity(origin.below()) == null, "Compensation left a seed container behind");
        helper.succeed();
    }
}
