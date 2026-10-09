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
public final class GeOreBuddingGameTests {
    private GeOreBuddingGameTests() {
    }

    /** Compare every available GeOre family against loaded AE2, including random draw order. */
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 200)
    public static void geOreGrowthMatchesLoadedAe2(GameTestHelper helper) {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
            BlockPos geOrePos = helper.absolutePos(new BlockPos(4, 4, 4));
            BlockPos certusPos = helper.absolutePos(new BlockPos(10, 4, 4));
            for (BuddingRank rank : BuddingRank.values()) {
                for (Direction direction : Direction.values()) {
                    for (int stage = -3; stage <= 5; stage++) {
                        for (int growth : new int[]{0, 1}) {
                            for (int decay : new int[]{0, 1}) {
                                Block geOre = IoeGeOreBuddingBlocks.block(family, rank);
                                Block certus = block("ae2:" + rank.path() + "_budding_quartz");
                                helper.getLevel().setBlockAndUpdate(geOrePos, geOre.defaultBlockState());
                                helper.getLevel().setBlockAndUpdate(certusPos, certus.defaultBlockState());
                                for (Direction face : Direction.values()) {
                                    helper.getLevel().setBlockAndUpdate(geOrePos.relative(face), Blocks.STONE.defaultBlockState());
                                    helper.getLevel().setBlockAndUpdate(certusPos.relative(face), Blocks.STONE.defaultBlockState());
                                }
                                helper.getLevel().setBlockAndUpdate(geOrePos.relative(direction), neighbor(family, stage, direction));
                                helper.getLevel().setBlockAndUpdate(certusPos.relative(direction), neighbor(BuddingResourceFamily.CERTUS_QUARTZ, stage, direction));
                                ScriptedRandom geOreRandom = new ScriptedRandom(growth, direction.ordinal(), decay);
                                ScriptedRandom certusRandom = new ScriptedRandom(growth, direction.ordinal(), decay);
                                geOre.defaultBlockState().randomTick(helper.getLevel(), geOrePos, geOreRandom);
                                certus.defaultBlockState().randomTick(helper.getLevel(), certusPos, certusRandom);
                                helper.assertTrue(geOreRandom.bounds.equals(certusRandom.bounds), "Native random draw order differs");
                                BlockState actualGeOre = helper.getLevel().getBlockState(geOrePos);
                                BlockState actualCertus = helper.getLevel().getBlockState(certusPos);
                                String geOreRank = actualGeOre.is(block(family.storageBlockId().toString())) ? "storage"
                                        : ((GeOreBuddingBlock) actualGeOre.getBlock()).rank().path();
                                String certusRank = actualCertus.is(block("ae2:quartz_block")) ? "storage"
                                        : BuiltInRegistries.BLOCK.getKey(actualCertus.getBlock()).getPath().replace("_budding_quartz", "");
                                helper.assertTrue(geOreRank.equals(certusRank), "Native degradation differs for " + family + ": " + rank);
                                BlockState geOreGrowth = helper.getLevel().getBlockState(geOrePos.relative(direction));
                                BlockState certusGrowth = helper.getLevel().getBlockState(certusPos.relative(direction));
                                String geOreId = BuiltInRegistries.BLOCK.getKey(geOreGrowth.getBlock()).getPath().replace(family.key(), "quartz");
                                String certusId = BuiltInRegistries.BLOCK.getKey(certusGrowth.getBlock()).getPath();
                                helper.assertTrue(geOreId.equals(certusId) && geOreGrowth.getValues().equals(certusGrowth.getValues()),
                                        "Native growth stage, facing or waterlogging differs for " + family + ": " + rank + ": " + stage);
                            }
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreRecipesLoadAndCannotRestoreFlawless(GameTestHelper helper) {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
            for (String path : List.of("aggregator/damaged_budding_" + family.key(), "transform/chipped_budding_" + family.key(), "transform/flawed_budding_" + family.key())) {
                helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id("immersive_ore_expedition:" + path)).isPresent(),
                        "Missing loaded GeOre recipe: " + path);
            }
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id("ae2cs:aggregator/flawless_budding_quartz")).isEmpty(),
                    "AE2CS Flawless recipe must be disabled");
            for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
                var result = recipe.value().getResultItem(helper.getLevel().registryAccess());
                helper.assertTrue(!result.is(IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWLESS).asItem()),
                        "No recipe may create Flawless GeOre");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreLootAndAccelerationMatchNativeRanks(GameTestHelper helper) {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
            var registry = helper.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            var acceleration = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    id("ae2:growth_acceleratable"));
            for (BuddingRank rank : BuddingRank.values()) {
                Block geOre = IoeGeOreBuddingBlocks.block(family, rank);
                helper.assertTrue(geOre.defaultBlockState().is(acceleration) && geOre.defaultBlockState().isRandomlyTicking(),
                        "GeOre must support AE2 growth acceleration");
                for (boolean silk : new boolean[]{false, true}) {
                    var tool = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
                    if (silk) tool.enchant(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
                    Block expected = rank == BuddingRank.FLAWLESS ? IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED)
                            : silk ? geOre : rank.degradedRank().map(lower -> IoeGeOreBuddingBlocks.block(family, lower)).orElse(block(family.storageBlockId().toString()));
                    var drops = Block.getDrops(geOre.defaultBlockState(), helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)),
                            null, null, tool);
                    helper.assertTrue(drops.size() == 1 && drops.getFirst().is(expected.asItem()) && drops.getFirst().getCount() == 1,
                            "GeOre loot must preserve Silk Touch and Flawless downgrade rules");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreRegistersFourDistinctBlocksAndItems(GameTestHelper helper) {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
            var blocks = new java.util.HashSet<Block>();
            var items = new java.util.HashSet<net.minecraft.world.item.Item>();
            for (BuddingRank rank : BuddingRank.values()) {
                Block block = IoeGeOreBuddingBlocks.block(family, rank);
                ResourceLocation expected = id("immersive_ore_expedition:" + rank.path() + "_budding_" + family.key());
                helper.assertTrue(BuiltInRegistries.BLOCK.getKey(block).equals(expected), "Wrong registered GeOre block id");
                helper.assertTrue(block instanceof GeOreBuddingBlock geOre && geOre.rank() == rank && geOre.family() == family, "Wrong functional GeOre rank");
                helper.assertTrue(block.asItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() == block,
                        "Each rank needs its own usable BlockItem");
                helper.assertTrue(BuiltInRegistries.ITEM.getKey(block.asItem()).equals(expected), "Wrong GeOre item id");
                blocks.add(block);
                items.add(block.asItem());
            }
            helper.assertTrue(blocks.size() == 4 && items.size() == 4, "Ranks must not alias one registered block/item");
        }
        helper.succeed();
    }

    /** Exercise AE2's loaded transform engine, including input consumption and the restoration ceiling. */
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreRestorationConsumesInputsAndStopsAtFlawed(GameTestHelper helper) throws ReflectiveOperationException {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE || !IoeGeOreBuddingBlocks.available(family)) continue;
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
                        new net.minecraft.world.item.ItemStack(IoeGeOreBuddingBlocks.block(family, rank), 2));
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
                    Block expected = IoeGeOreBuddingBlocks.block(family, rank == BuddingRank.DAMAGED ? BuddingRank.CHIPPED : BuddingRank.FLAWED);
                    helper.assertTrue(outputs.size() == 1 && outputs.getFirst().getItem().is(expected.asItem())
                            && outputs.getFirst().getItem().getCount() == 1, "Wrong restored block or duplicated output");
                } else {
                    helper.assertTrue(outputs.isEmpty(), "Flawed/Flawless inputs must produce no upgraded item");
                }
                catalyst.discard();
                source.discard();
                outputs.forEach(net.minecraft.world.entity.Entity::discard);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void geOreAvailabilityAndMissingResourcesFailClosed(GameTestHelper helper) {
        int families = 0;
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) continue;
            families++;
            boolean expected = family.dependenciesPresent(net.neoforged.fml.ModList.get()::isLoaded);
            helper.assertTrue(IoeGeOreBuddingBlocks.available(family) == expected,
                    "Unexpected availability for " + family);
            for (BuddingRank rank : BuddingRank.values()) {
                ResourceLocation blockId = id("immersive_ore_expedition:" + rank.path() + "_budding_" + family.key());
                helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(blockId) == expected,
                        "Dependency-gated block registration differs for " + blockId);
                helper.assertTrue(BuiltInRegistries.ITEM.containsKey(blockId) == expected,
                        "Dependency-gated item registration differs for " + blockId);
            }
            for (String recipe : List.of("aggregator/damaged_budding_", "transform/chipped_budding_", "transform/flawed_budding_")) {
                boolean recipeExpected = expected && (!recipe.startsWith("aggregator/")
                        || net.neoforged.fml.ModList.get().isLoaded("ae2cs"));
                helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                        id("immersive_ore_expedition:" + recipe + family.key())).isPresent() == recipeExpected,
                        "Dependency-gated recipe differs for " + family + ": " + recipe);
            }
            helper.assertTrue(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> true, resource -> true, resource -> true),
                    "Complete registry should activate " + family);
            for (String required : family.requiresImmersiveEngineering()
                    ? List.of("ae2", "geore", "immersiveengineering") : List.of("ae2", "geore")) {
                helper.assertTrue(!IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> !mod.equals(required),
                        resource -> true, resource -> true), "Missing dependency must disable " + family + ": " + required);
            }
            for (ResourceLocation missing : List.of(family.storageBlockId(), family.pocketBlockId())) {
                helper.assertTrue(!IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> true,
                        resource -> !resource.equals(missing), resource -> true), "Missing block must disable " + family + ": " + missing);
            }
            for (ResourceLocation incompatible : family.growthProductIds()) {
                helper.assertTrue(!IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> true, resource -> true,
                        resource -> !resource.equals(incompatible)), "Missing/incompatible growth must disable " + family);
            }
        }
        helper.assertTrue(families == 13, "All thirteen canonical GeOre families must be covered");
        helper.succeed();
    }

    private static BlockState neighbor(BuddingResourceFamily family, int stage, Direction direction) {
        if (stage == -3) return Blocks.STONE.defaultBlockState();
        if (stage == -2) return Blocks.WATER.defaultBlockState();
        if (stage == -1) return Blocks.AIR.defaultBlockState();
        List<ResourceLocation> products = family.growthProductIds();
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
