package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.HashSet;
import java.util.List;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class GlowstoneBuddingGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void approvedTwoOneHarvestKeepsCommonHeartRules(GameTestHelper helper) {
        var family = BuddingResourceFamily.GLOWSTONE;
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4));
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
        var plain = new ItemStack(Items.DIAMOND_PICKAXE);
        var hearts = new HashSet<Block>();
        helper.assertTrue(IoeGeOreBuddingBlocks.available(family), "Vanilla-backed glowstone unavailable");
        for (BuddingRank rank : BuddingRank.values()) {
            Block heart = IoeGeOreBuddingBlocks.block(family, rank);
            var id = ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, rank.path() + "_budding_glowstone");
            helper.assertTrue(BuiltInRegistries.BLOCK.getKey(heart).equals(id)
                    && BuiltInRegistries.ITEM.getKey(heart.asItem()).equals(id), "Missing distinct glowstone block/item identity");
            helper.assertTrue(heart.asItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() == heart,
                    "Glowstone rank has no usable BlockItem");
            hearts.add(heart);
            for (var tool : List.of(plain, silk)) {
                Block expected = rank == BuddingRank.FLAWLESS ? IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED)
                        : tool == silk ? heart : rank.degradedRank().map(lower -> IoeGeOreBuddingBlocks.block(family, lower)).orElse(Blocks.GLOWSTONE);
                var drops = Block.getDrops(heart.defaultBlockState(), level, pos, null, null, tool);
                helper.assertTrue(drops.size() == 1 && drops.getFirst().is(expected.asItem()) && drops.getFirst().getCount() == 1,
                        "Common IOE heart harvest mismatch: " + rank);
            }
        }
        helper.assertTrue(hearts.size() == 5, "Glowstone phases alias one heart");
        var products = family.growthProductIds().stream()
                .map(id -> BuiltInRegistries.BLOCK.getOptional(id).orElseThrow()).toList();
        helper.assertTrue(new HashSet<>(products).size() == 4, "Four distinct growth stages required");
        for (int i = 0; i < products.size(); i++) {
            Block product = products.get(i);
            helper.assertTrue(product instanceof net.minecraft.world.level.block.AmethystClusterBlock,
                    "Growth must retain vanilla cluster physics");
            helper.assertTrue(product.asItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() == product,
                    "Growth product must have its own BlockItem");
            var drops = Block.getDrops(product.defaultBlockState(), level, pos, null, null, silk);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(product.asItem()) && drops.getFirst().getCount() == 1,
                    "Silk Touch must recover the exact growth stage");
            for (var tool : List.of(plain, new ItemStack(Items.WOODEN_PICKAXE), ItemStack.EMPTY, new ItemStack(Items.DIAMOND_AXE))) {
                var normal = Block.getDrops(product.defaultBlockState(), level, pos, null, null, tool);
                int expected = tool.is(net.minecraft.tags.ItemTags.CLUSTER_MAX_HARVESTABLES) ? 2 : 1;
                helper.assertTrue(i == 3 ? normal.size() == 1 && normal.getFirst().is(Items.GLOWSTONE_DUST)
                        && normal.getFirst().getCount() == expected : normal.isEmpty(), "Approved 2/1 or immature loot mismatch");
            }
        }
        Block cluster = products.getLast();
        for (int fortune = 1; fortune <= 3; fortune++) {
            var pick = new ItemStack(Items.DIAMOND_PICKAXE);
            pick.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
            var axe = new ItemStack(Items.DIAMOND_AXE);
            axe.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
            // Same explicit seeds compare the loaded vanilla oracle without changing world RNG.
            for (long seed = 1; seed <= 64; seed++) {
                var glow = seededDrops(helper, cluster, pick, seed);
                var vanilla = seededDrops(helper, Blocks.AMETHYST_CLUSTER, pick, seed);
                helper.assertTrue(glow.size() == 1 && glow.getFirst().is(Items.GLOWSTONE_DUST)
                        && glow.getFirst().getCount() * 2 == vanilla.getFirst().getCount(),
                        "Fortune must retain vanilla ore_drops multiplier with base two");
                var other = seededDrops(helper, cluster, axe, seed);
                helper.assertTrue(other.size() == 1 && other.getFirst().getCount() == 1,
                        "Fortune must not multiply the non-pickaxe branch");
            }
        }
        for (var recipe : level.getRecipeManager().getRecipes()) {
            var output = recipe.value().getResultItem(level.registryAccess());
            helper.assertTrue(hearts.stream().noneMatch(heart -> output.is(heart.asItem())), "Unapproved glowstone-heart recipe");
        }
        helper.succeed();
    }
    private static java.util.List<ItemStack> seededDrops(GameTestHelper helper, Block block, ItemStack tool, long seed) {
        var level = helper.getLevel();
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE, block.defaultBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 4, 4))))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, tool)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        return level.getServer().reloadableRegistries().getLootTable(block.getLootTable()).getRandomItems(params, seed);
    }
}
