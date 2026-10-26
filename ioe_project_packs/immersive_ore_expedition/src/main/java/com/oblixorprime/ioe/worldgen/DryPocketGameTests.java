package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class DryPocketGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void dryPocketLimitsAndRollbackPreserveRewardAndRoute(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) continue;
            for (int count = 0; count <= 5; count++) {
                var shape = ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER,
                        origin, SiteQuality.DRY, null, null, RandomSource.create(0));
                var pocket = DrySitePockets.attach(shape, family.pocketBlockId(), count, 42);
                helper.assertTrue(pocket.blocks().equals(DrySitePockets.attach(shape, family.pocketBlockId(), count, 42).blocks()),
                        "Residual positions changed on repeated planning");
                helper.assertTrue(pocket.oreBlockCount() == count && pocket.oreNodeHeartCount() == 0 && pocket.oreNodeCount() == 0,
                        "DRY budget or zero-node invariant violated");
                var plan = DrySiteRewards.attach(pocket, true, 42);
                helper.assertTrue(plan.oreBlockCount() == count && plan.blockEntityPayloads().size() == 1,
                        "Reward must not overwrite any residue");
                plan.blocks().keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()));
                var applied = IoeExpeditionPlanPlacement.apply(level, plan).orElseThrow();
                helper.assertTrue(applied.rollback(level) && applied.rollback(level), "Residual compensation failed");
                helper.assertTrue(plan.blocks().keySet().stream().allMatch(pos -> level.getBlockState(pos).is(Blocks.STONE)),
                        "Compensation left residual ores behind");
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(4)).isEmpty(),
                        "DRY compensation duplicated a reward or resource");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void dryPocketRejectsInvalidBudgetsAndMissingMaterial(GameTestHelper helper) {
        var shape = ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER,
                helper.absolutePos(new BlockPos(8, 8, 8)), SiteQuality.DRY, null, null, RandomSource.create(0));
        for (int count : new int[]{-1, 6}) {
            boolean rejected = false;
            try { DrySitePockets.attach(shape, BuddingResourceFamily.GEORE_IRON.pocketBlockId(), count, 0); }
            catch (IllegalArgumentException expected) { rejected = true; }
            helper.assertTrue(rejected, "Out-of-range DRY budget accepted");
        }
        boolean rejected = false;
        try { DrySitePockets.attach(shape, net.minecraft.resources.ResourceLocation.parse("ioe_missing:ore"), 1, 0); }
        catch (IllegalStateException expected) { rejected = true; }
        helper.assertTrue(rejected, "Missing profile material was silently replaced");
        helper.succeed();
    }
}
