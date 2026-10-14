package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.AABB;
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
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(origin.below()).inflate(2)).isEmpty(),
                "Compensation ejected the unopened reward as an item entity");
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void generatedDryRewardCannotEscapeRepeatedRollback(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        var level = helper.getLevel();
        var plan = DrySiteRewards.attach(ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER,
                origin, SiteQuality.DRY, null, null, RandomSource.create(0)), true, 42);
        plan.blocks().keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()));
        for (int attempt = 0; attempt < 2; attempt++) {
            var applied = IoeExpeditionPlanPlacement.apply(level, plan).orElseThrow();
            var container = (RandomizableContainerBlockEntity) level.getBlockEntity(origin.below());
            container.unpackLootTable(null);
            int count = 0;
            for (int slot = 0; slot < container.getContainerSize(); slot++) count += container.getItem(slot).getCount();
            helper.assertTrue(count == 1, "DRY reward must generate exactly one seed before compensation");
            helper.assertTrue(applied.rollback(level) && applied.rollback(level), "Repeated compensation failed");
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(origin.below()).inflate(2)).isEmpty(),
                    "Rolled-back reward escaped into the world on attempt " + attempt);
            helper.assertTrue(level.getBlockEntity(origin.below()) == null, "Rolled-back container survived");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void acceptedDryRewardSurvivesContainerReloadWithoutReroll(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(8, 8, 8));
        var level = helper.getLevel();
        var plan = DrySiteRewards.attach(ExpeditionSiteBlueprints.plan(ExpeditionSiteType.ORE_LOAD_CHAMBER,
                origin, SiteQuality.DRY, null, null, RandomSource.create(0)), true, 42);
        plan.blocks().keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()));
        var applied = IoeExpeditionPlanPlacement.apply(level, plan).orElseThrow();
        applied.accept();
        BlockPos cache = origin.below();
        var container = (RandomizableContainerBlockEntity) level.getBlockEntity(cache);
        helper.assertTrue(applied.rollback(level) && level.getBlockEntity(cache) == container,
                "Compensation must not alter an accepted reward");
        for (int pass = 0; pass < 2; pass++) {
            var saved = container.saveWithFullMetadata(level.registryAccess());
            var reloaded = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(cache,
                    level.getBlockState(cache), saved, level.registryAccess());
            helper.assertTrue(reloaded instanceof RandomizableContainerBlockEntity, "Reward container failed NBT reload");
            level.removeBlockEntity(cache);
            level.setBlockEntity(reloaded);
            container = (RandomizableContainerBlockEntity) reloaded;
            int seeds = 0;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                var stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                            .toString().equals("ae2cs:resonating_seed"), "Unexpected DRY reward");
                    seeds += stack.getCount();
                    container.removeItemNoUpdate(slot);
                }
            }
            helper.assertTrue(seeds == (pass == 0 ? 1 : 0), "Reward rerolled after extraction and NBT reload");
        }
        helper.succeed();
    }
}
