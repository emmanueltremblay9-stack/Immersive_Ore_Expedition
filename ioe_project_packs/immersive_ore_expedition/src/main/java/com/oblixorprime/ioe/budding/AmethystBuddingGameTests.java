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
public final class AmethystBuddingGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void fiveRecoverableHeartsKeepVanillaProductsAndDoNotAddRecipes(GameTestHelper helper) {
        var family = BuddingResourceFamily.AMETHYST;
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4));
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
        var plain = new ItemStack(Items.DIAMOND_PICKAXE);
        var hearts = new HashSet<Block>();
        helper.assertTrue(IoeGeOreBuddingBlocks.available(family), "Vanilla-backed amethyst unavailable");
        for (BuddingRank rank : BuddingRank.values()) {
            Block heart = IoeGeOreBuddingBlocks.block(family, rank);
            var id = ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, rank.path() + "_budding_amethyst");
            helper.assertTrue(BuiltInRegistries.BLOCK.getKey(heart).equals(id)
                    && BuiltInRegistries.ITEM.getKey(heart.asItem()).equals(id), "Missing distinct amethyst block/item identity");
            helper.assertTrue(heart.asItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() == heart,
                    "Amethyst rank has no usable BlockItem");
            hearts.add(heart);
            for (var tool : List.of(plain, silk)) {
                Block expected = rank == BuddingRank.FLAWLESS ? IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED)
                        : tool == silk ? heart : rank.degradedRank().map(lower -> IoeGeOreBuddingBlocks.block(family, lower)).orElse(Blocks.AMETHYST_BLOCK);
                var drops = Block.getDrops(heart.defaultBlockState(), level, pos, null, null, tool);
                helper.assertTrue(drops.size() == 1 && drops.getFirst().is(expected.asItem()) && drops.getFirst().getCount() == 1,
                        "Profile A heart harvest mismatch: " + rank);
            }
        }
        helper.assertTrue(hearts.size() == 5, "Amethyst phases alias one heart");
        var products = List.of(Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER);
        for (int i = 0; i < products.size(); i++) {
            Block product = products.get(i);
            helper.assertTrue(BuiltInRegistries.BLOCK.getOptional(family.growthProductIds().get(i)).orElseThrow() == product,
                    "Amethyst growth must use actual vanilla blocks");
            var drops = Block.getDrops(product.defaultBlockState(), level, pos, null, null, silk);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(product.asItem()) && drops.getFirst().getCount() == 1,
                    "Vanilla Silk Touch crystal harvest changed");
            var plainDrops = Block.getDrops(product.defaultBlockState(), level, pos, null, null, plain);
            helper.assertTrue(i == 3 ? plainDrops.size() == 1 && plainDrops.getFirst().is(Items.AMETHYST_SHARD)
                    && plainDrops.getFirst().getCount() == 4 : plainDrops.isEmpty(), "Vanilla plain crystal harvest changed");
        }
        helper.assertTrue(BuddingBlockIdentity.of(Blocks.BUDDING_AMETHYST).isEmpty(), "Native budding must not become an IOE node");
        helper.assertTrue(Block.getDrops(Blocks.BUDDING_AMETHYST.defaultBlockState(), level, pos, null, null, silk).isEmpty(),
                "Native budding loot must remain untouched");
        for (var recipe : level.getRecipeManager().getRecipes()) {
            var output = recipe.value().getResultItem(level.registryAccess());
            helper.assertTrue(hearts.stream().noneMatch(heart -> output.is(heart.asItem())), "Unapproved amethyst-heart recipe");
        }
        helper.succeed();
    }
}
