package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class IronBuddingGameTests {
    private IronBuddingGameTests() {
    }

    /** Compare actual loaded AE2 behavior, including random draw order, with the Iron implementation. */
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void ironGrowthMatchesLoadedAe2(GameTestHelper helper) {
        BlockPos ironPos = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos certusPos = helper.absolutePos(new BlockPos(10, 4, 4));
        for (BuddingRank rank : BuddingRank.values()) {
            for (Direction direction : Direction.values()) {
                for (int stage = -3; stage <= 5; stage++) {
                    for (int growth : new int[]{0, 1}) {
                        for (int decay : new int[]{0, 1}) {
                            Block iron = IoeIronBuddingBlocks.block(rank);
                            Block certus = block("ae2:" + rank.path() + "_budding_quartz");
                            helper.getLevel().setBlockAndUpdate(ironPos, iron.defaultBlockState());
                            helper.getLevel().setBlockAndUpdate(certusPos, certus.defaultBlockState());
                            for (Direction face : Direction.values()) {
                                helper.getLevel().setBlockAndUpdate(ironPos.relative(face), Blocks.STONE.defaultBlockState());
                                helper.getLevel().setBlockAndUpdate(certusPos.relative(face), Blocks.STONE.defaultBlockState());
                            }
                            helper.getLevel().setBlockAndUpdate(ironPos.relative(direction), neighbor(true, stage, direction));
                            helper.getLevel().setBlockAndUpdate(certusPos.relative(direction), neighbor(false, stage, direction));
                            ScriptedRandom ironRandom = new ScriptedRandom(growth, direction.ordinal(), decay);
                            ScriptedRandom certusRandom = new ScriptedRandom(growth, direction.ordinal(), decay);
                            iron.defaultBlockState().randomTick(helper.getLevel(), ironPos, ironRandom);
                            certus.defaultBlockState().randomTick(helper.getLevel(), certusPos, certusRandom);
                            helper.assertTrue(ironRandom.bounds.equals(certusRandom.bounds), "Native random draw order differs");
                            BlockState actualIron = helper.getLevel().getBlockState(ironPos);
                            BlockState actualCertus = helper.getLevel().getBlockState(certusPos);
                            String ironRank = actualIron.is(Blocks.IRON_BLOCK) ? "storage"
                                    : ((IronBuddingBlock) actualIron.getBlock()).rank().path();
                            String certusRank = actualCertus.is(block("ae2:quartz_block")) ? "storage"
                                    : BuiltInRegistries.BLOCK.getKey(actualCertus.getBlock()).getPath().replace("_budding_quartz", "");
                            helper.assertTrue(ironRank.equals(certusRank), "Native degradation differs for " + rank);
                            BlockState ironGrowth = helper.getLevel().getBlockState(ironPos.relative(direction));
                            BlockState certusGrowth = helper.getLevel().getBlockState(certusPos.relative(direction));
                            String ironId = BuiltInRegistries.BLOCK.getKey(ironGrowth.getBlock()).getPath().replace("iron", "quartz");
                            String certusId = BuiltInRegistries.BLOCK.getKey(certusGrowth.getBlock()).getPath();
                            helper.assertTrue(ironId.equals(certusId) && ironGrowth.getValues().equals(certusGrowth.getValues()),
                                    "Native growth stage, facing or waterlogging differs");
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void ironRecipesLoadAndCannotRestoreFlawless(GameTestHelper helper) {
        for (String path : List.of("aggregator/damaged_budding_iron", "transform/chipped_budding_iron", "transform/flawed_budding_iron")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id("immersive_ore_expedition:" + path)).isPresent(),
                    "Missing loaded Iron recipe: " + path);
        }
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id("ae2cs:aggregator/flawless_budding_quartz")).isEmpty(),
                "AE2CS Flawless recipe must be disabled");
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            var result = recipe.value().getResultItem(helper.getLevel().registryAccess());
            helper.assertTrue(!result.is(IoeIronBuddingBlocks.block(BuddingRank.FLAWLESS).asItem()),
                    "No recipe may create Flawless Iron");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void ironLootAndAccelerationMatchNativeRanks(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var acceleration = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                id("ae2:growth_acceleratable"));
        for (BuddingRank rank : BuddingRank.values()) {
            Block iron = IoeIronBuddingBlocks.block(rank);
            helper.assertTrue(iron.defaultBlockState().is(acceleration) && iron.defaultBlockState().isRandomlyTicking(),
                    "Iron must support AE2 growth acceleration");
            for (boolean silk : new boolean[]{false, true}) {
                var tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
                if (silk) tool.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
                Block expected = rank == BuddingRank.FLAWLESS ? IoeIronBuddingBlocks.block(BuddingRank.FLAWED)
                        : silk ? iron : rank.degradedRank().map(IoeIronBuddingBlocks::block).orElse(Blocks.IRON_BLOCK);
                var drops = Block.getDrops(iron.defaultBlockState(), helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)),
                        null, null, tool);
                helper.assertTrue(drops.size() == 1 && drops.getFirst().is(expected.asItem()) && drops.getFirst().getCount() == 1,
                        "Iron loot must preserve Silk Touch and Flawless downgrade rules");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void ironRegistersFourDistinctBlocksAndItems(GameTestHelper helper) {
        var blocks = new java.util.HashSet<Block>();
        var items = new java.util.HashSet<net.minecraft.world.item.Item>();
        for (BuddingRank rank : BuddingRank.values()) {
            Block block = IoeIronBuddingBlocks.block(rank);
            ResourceLocation expected = id("immersive_ore_expedition:" + rank.path() + "_budding_iron");
            helper.assertTrue(BuiltInRegistries.BLOCK.getKey(block).equals(expected), "Wrong registered Iron block id");
            helper.assertTrue(block instanceof IronBuddingBlock iron && iron.rank() == rank, "Wrong functional Iron rank");
            helper.assertTrue(block.asItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() == block,
                    "Each rank needs its own usable BlockItem");
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(block.asItem()).equals(expected), "Wrong Iron item id");
            blocks.add(block);
            items.add(block.asItem());
        }
        helper.assertTrue(blocks.size() == 4 && items.size() == 4, "Ranks must not alias one registered block/item");
        helper.succeed();
    }

    /** Exercise AE2's loaded transform engine, including input consumption and the restoration ceiling. */
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void ironRestorationConsumesInputsAndStopsAtFlawed(GameTestHelper helper) throws ReflectiveOperationException {
        Class<?> logic = Class.forName("appeng.recipes.transform.TransformLogic");
        var transform = logic.getMethod("tryTransform", net.minecraft.world.entity.item.ItemEntity.class,
                java.util.function.Predicate.class);
        var fluid = Class.forName("appeng.recipes.transform.TransformCircumstance")
                .getMethod("isFluid", net.minecraft.world.level.material.Fluid.class);
        java.util.function.Predicate<Object> water = circumstance -> {
            try {
                return (boolean) fluid.invoke(circumstance, net.minecraft.world.level.material.Fluids.WATER);
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError("Loaded AE2 transform API changed", failure);
            }
        };
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        var area = new net.minecraft.world.phys.AABB(pos).inflate(1);
        var charged = BuiltInRegistries.ITEM.getOptional(id("ae2:charged_certus_quartz_crystal")).orElseThrow();
        for (BuddingRank rank : BuddingRank.values()) {
            var catalyst = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(), pos.getX() + 0.5,
                    pos.getY() + 0.5, pos.getZ() + 0.5, new net.minecraft.world.item.ItemStack(charged, 2));
            var source = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(), pos.getX() + 0.5,
                    pos.getY() + 0.5, pos.getZ() + 0.5,
                    new net.minecraft.world.item.ItemStack(IoeIronBuddingBlocks.block(rank), 2));
            helper.getLevel().addFreshEntity(catalyst);
            helper.getLevel().addFreshEntity(source);
            boolean restored = (boolean) transform.invoke(null, catalyst, water);
            boolean allowed = rank == BuddingRank.DAMAGED || rank == BuddingRank.CHIPPED;
            helper.assertTrue(restored == allowed, "Native restoration ceiling violated for " + rank);
            helper.assertTrue(catalyst.getItem().getCount() == (allowed ? 1 : 2)
                            && source.getItem().getCount() == (allowed ? 1 : 2),
                    "Restoration must consume exactly one of each input, and nothing when refused");
            var outputs = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area,
                    entity -> entity != catalyst && entity != source);
            if (allowed) {
                Block expected = IoeIronBuddingBlocks.block(rank == BuddingRank.DAMAGED ? BuddingRank.CHIPPED : BuddingRank.FLAWED);
                helper.assertTrue(outputs.size() == 1 && outputs.getFirst().getItem().is(expected.asItem())
                        && outputs.getFirst().getItem().getCount() == 1, "Wrong restored block or duplicated output");
            } else {
                helper.assertTrue(outputs.isEmpty(), "Flawed/Flawless inputs must produce no upgraded item");
            }
            catalyst.discard();
            source.discard();
            outputs.forEach(net.minecraft.world.entity.Entity::discard);
        }
        helper.succeed();
    }

    private static BlockState neighbor(boolean iron, int stage, Direction direction) {
        if (stage == -3) return Blocks.STONE.defaultBlockState();
        if (stage == -2) return Blocks.WATER.defaultBlockState();
        if (stage == -1) return Blocks.AIR.defaultBlockState();
        List<ResourceLocation> products = (iron ? BuddingResourceFamily.GEORE_IRON : BuddingResourceFamily.CERTUS_QUARTZ).growthProductIds();
        int index = stage >= 4 ? 0 : stage;
        return BuiltInRegistries.BLOCK.getOptional(products.get(index)).orElseThrow().defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, stage == 4 ? direction.getOpposite() : direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, stage == 5);
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(id(id)).orElseThrow();
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }

    private static final class ScriptedRandom extends LegacyRandomSource {
        private final int[] values;
        private final List<Integer> bounds = new ArrayList<>();
        private int index;

        private ScriptedRandom(int... values) {
            super(0);
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            bounds.add(bound);
            if (index >= values.length || values[index] >= bound) {
                throw new AssertionError("Unexpected native random draw: " + bound);
            }
            return values[index++];
        }
    }
}
