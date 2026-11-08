package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class FlawlessFixerGameTests {
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void entroIdentityIsPermanentAndHarvestsMostly(GameTestHelper helper) {
        var key = id("immersive_ore_expedition:flawless_budding_entroized_fluix");
        if (!ModList.get().isLoaded("extendedae")) {
            helper.assertFalse(BuiltInRegistries.BLOCK.containsKey(key), "Entro identity registered without ExtendedAE");
            for (BuddingResourceFamily family : BuddingResourceFamily.values())
                helper.assertFalse(helper.getLevel().getRecipeManager().byKey(id(
                        "immersive_ore_expedition:fixer/flawless_budding_"+family.key())).isPresent(),
                        "Optional Fixer recipe loaded without dependency");
            helper.succeed(); return;
        }
        var block = BuiltInRegistries.BLOCK.get(key);
        helper.assertTrue(block instanceof EntroFlawlessBuddingBlock, "Distinct permanent Entro identity missing");
        var identity = BuddingBlockIdentity.of(block).orElseThrow();
        helper.assertTrue(identity.rank()==BuddingRank.FLAWLESS
                        && identity.family().equals(BuddingResourceFamily.ENTROIZED_FLUIX.identity()),
                "Jade identity confused permanent Entro with native Fully or ordinary Fluix");
        var mostly = BuiltInRegistries.BLOCK.get(id("extendedae:entro_budding_mostly"));
        var fully = BuiltInRegistries.BLOCK.get(id("extendedae:entro_budding_fully"));
        helper.assertTrue(block != fully && FlawlessFixerPayment.isFlawlessAttempt(mostly,block),
                "Permanent Entro aliases Fully or does not recognize its Flawed input");
        helper.assertFalse(FlawlessFixerPayment.isFlawlessAttempt(mostly,fully), "Ordinary Entro repair acquired premium cost");
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 5, 5));
        level.setBlockAndUpdate(pos,block.defaultBlockState());
        var growth = new net.minecraft.world.level.levelgen.LegacyRandomSource(0) {
            @Override public int nextInt(int bound) { return 0; }
        };
        for (int i=0;i<20;i++) {
            int stage = i % 4;
            if (stage == 0) level.removeBlock(pos.below(),false);
            ((EntroFlawlessBuddingBlock) block).randomTick(block.defaultBlockState(),level,pos,growth);
            helper.assertTrue(level.getBlockState(pos.below()).is(BuiltInRegistries.BLOCK.get(
                            BuddingResourceFamily.ENTROIZED_FLUIX.growthProductIds().get(stage))),
                    "Entro growth changed family or skipped a growth stage");
            helper.assertTrue(level.getBlockState(pos).is(block),"Permanent Entro degraded");
        }
        var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        for (boolean silk : new boolean[]{false,true}) {
            var tool = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
            if (silk) tool.enchant(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);
            var drops = net.minecraft.world.level.block.Block.getDrops(block.defaultBlockState(),level,pos,null,null,tool);
            helper.assertTrue(drops.size()==1 && drops.getFirst().is(mostly.asItem()) && drops.getFirst().getCount()==1,
                    "Entro Flawless must harvest as native Mostly, including Silk Touch");
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void entroFuelSelectsExactlyOneRecipeAndPreservesNativeRepair(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("extendedae")) { helper.succeed(); return; }
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(id("extendedae:crystal_fixer")).defaultBlockState());
        var host = level.getBlockEntity(pos);
        var target = pos.relative((Direction) invoke(host,"getFront"));
        var mostly = BuiltInRegistries.BLOCK.get(id("extendedae:entro_budding_mostly"));
        var fully = BuiltInRegistries.BLOCK.get(id("extendedae:entro_budding_fully"));
        var charged = BuiltInRegistries.ITEM.get(id("ae2:charged_certus_quartz_crystal"));
        var dustTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id("c:dusts/ender_pearl"));
        var dusts = BuiltInRegistries.ITEM.getTag(dustTag).orElseThrow();
        helper.assertTrue(dusts.size()>0 && !new ItemStack(charged).is(dustTag), "Entro fuels overlap or dust tag is empty");
        var fuels = new java.util.ArrayList<net.minecraft.world.item.Item>();
        fuels.add(charged);
        dusts.forEach(holder -> fuels.add(holder.value()));
        fuels.add(charged); // Re-select premium after ordinary repair in the same machine.
        for (var fuel : fuels) {
            boolean premium = fuel == charged;
            var expected = id(premium ? "immersive_ore_expedition:fixer/flawless_budding_entroized_fluix"
                    : "extendedae:fixer/entro_mostly");
            level.setBlockAndUpdate(target,mostly.defaultBlockState());
            var inv = invoke(host,"getInternalInventory");
            invoke(inv,"setItemDirect",new Class<?>[]{int.class,ItemStack.class},0,new ItemStack(fuel));
            invoke(host,"onChanged");
            var ctx = invoke(host,"getContext");
            var matches = new java.util.ArrayList<ResourceLocation>();
            for (var recipe : level.getRecipeManager().getRecipes()) {
                if (id("extendedae:crystal_fixer").equals(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.value().getType()))
                        && (boolean) invoke(ctx,"testRecipe",new Class<?>[]{net.minecraft.world.item.crafting.RecipeHolder.class},recipe))
                    matches.add(recipe.id());
            }
            helper.assertTrue(matches.equals(java.util.List.of(expected)), "Ambiguous or wrong Entro fuel selection: "+matches);
            invoke(ctx,"findRecipe");
            var field = ctx.getClass().getField("currentRecipe"); field.setAccessible(true);
            var selected = (net.minecraft.world.item.crafting.RecipeHolder<?>) field.get(ctx);
            helper.assertTrue(selected != null && selected.id().equals(expected), "Native discovery selected another recipe");
            helper.assertTrue(Math.abs((double) invoke(selected.value(),"getChance")-(premium ? .05 : .70))<1e-9,
                    "Native repair or normalized fabrication chance changed");
            Object handled = FlawlessFixerPayment.tick(host);
            helper.assertTrue((handled != null)==premium, "Ordinary repair entered the premium payment path");
            if (!premium) {
                helper.assertFalse(host.getPersistentData().contains("ioe_flawless_payment"), "Ordinary repair retained premium credit");
                // Complete the native executor's progress fixture; no claim about native energy cost.
                invoke(host,"setProgress",new Class<?>[]{int.class},100);
                tick(host);
                helper.assertTrue(((ItemStack) invoke(inv,"getStackInSlot",new Class<?>[]{int.class},0)).isEmpty(),
                        "Native ordinary repair did not consume its dust");
                helper.assertTrue(level.getBlockState(target).is(mostly) || level.getBlockState(target).is(fully),
                        "Ordinary Entro repair manufactured a permanent heart");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 100)
    public static void nativeFixerPaysOnceAcrossSaveLoadAndKeepsFuelRoll(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("extendedae")) { helper.succeed(); return; }
        verifyAttempt(helper, "extendedae:fixer/certus_flawed", "ae2:flawed_budding_quartz", "ae2:flawless_budding_quartz");
        verifyAttempt(helper, "immersive_ore_expedition:fixer/flawless_budding_entroized_fluix",
                "extendedae:entro_budding_mostly", "immersive_ore_expedition:flawless_budding_entroized_fluix");
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (!IoeGeOreBuddingBlocks.available(family)) continue;
            verifyAttempt(helper, "immersive_ore_expedition:fixer/flawless_budding_" + family.key(),
                    "immersive_ore_expedition:flawed_budding_" + family.key(),
                    "immersive_ore_expedition:flawless_budding_" + family.key());
        }
        helper.succeed();
    }

    private static void verifyAttempt(GameTestHelper helper, String recipeId, String inputId, String outputId) throws Exception {
        int oldCost = FlawlessFixerConfig.COST_FE.get();
        try {
            FlawlessFixerConfig.COST_FE.set(1000);
            var level = helper.getLevel();
            BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
            var state = BuiltInRegistries.BLOCK.get(id("extendedae:crystal_fixer")).defaultBlockState();
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, state);
            BlockEntity host = level.getBlockEntity(pos);
            helper.assertTrue(host != null, "Native Crystal Fixer missing");
            Direction front = (Direction) invoke(host, "getFront");
            BlockPos target = pos.relative(front);
            var flawed = BuiltInRegistries.BLOCK.get(id(inputId));
            level.setBlockAndUpdate(target, flawed.defaultBlockState());
            var fuel = BuiltInRegistries.ITEM.get(id("ae2:charged_certus_quartz_crystal"));
            Object inv = invoke(host, "getInternalInventory");
            invoke(inv, "setItemDirect", new Class<?>[]{int.class, ItemStack.class}, 0, new ItemStack(fuel));
            // Discover through the native executor. With no power, neither payment nor fuel may advance.
            for (int i=0;i<3;i++) tick(host);
            helper.assertTrue(((ItemStack) invoke(inv, "getStackInSlot", new Class<?>[]{int.class}, 0)).getCount()==1,
                    "Unpowered attempt consumed fuel");
            Object ae = enumValue("appeng.api.config.PowerUnit", "AE");
            Object modulate = enumValue("appeng.api.config.Actionable", "MODULATE");
            invoke(host,"injectExternalPower",new Class<?>[]{ae.getClass(),double.class,modulate.getClass()},ae,4000.0,modulate);
            double before = (double) invoke(host, "getAECurrentPower");
            tick(host);
            double paid = host.getPersistentData().getCompound("ioe_flawless_payment").getDouble("paid");
            double firstDebit = before - (double) invoke(host, "getAECurrentPower");
            helper.assertTrue(paid > 0 && paid < 1000, "Expected partial FE payment");
            var nbt = host.saveWithFullMetadata(level.registryAccess());
            level.removeBlockEntity(pos);
            host = BlockEntity.loadStatic(pos,state,nbt,level.registryAccess());
            level.setBlockEntity(host);
            helper.assertTrue(host.getPersistentData().getCompound("ioe_flawless_payment").getDouble("paid")==paid,
                    "Partial payment lost during serialization");
            // Resume the actual saved machine without replacing its fuel or energy.
            inv = invoke(host, "getInternalInventory");
            helper.assertTrue(((ItemStack) invoke(inv, "getStackInSlot", new Class<?>[]{int.class}, 0)).getCount()==1,
                    "Saved fuel was lost during reload");
            helper.assertTrue(Math.abs((double) invoke(host, "getAECurrentPower")-(before-firstDebit))<0.000001,
                    "Saved energy was lost during reload");
            invoke(host,"onChanged");
            before = (double) invoke(host, "getAECurrentPower");
            FlawlessFixerConfig.COST_FE.set(2000); // In-flight attempt keeps its original 1000 FE budget.
            for (int i=0;i<20;i++) tick(host);
            double after = (double) invoke(host, "getAECurrentPower");
            helper.assertTrue(Math.abs(FlawlessFixerPayment.convert("AE","FE",firstDebit+before-after)-1000)<0.000001,
                    "Attempt charged a different FE total or charged twice after reload");
            inv = invoke(host,"getInternalInventory");
            helper.assertTrue(((ItemStack) invoke(inv,"getStackInSlot",new Class<?>[]{int.class},0)).isEmpty(),
                    "Native attempt did not consume its one charged Certus fuel");
            helper.assertFalse(host.getPersistentData().contains("ioe_flawless_payment"),"Completed attempt retained paid credit");
            var recipe = level.getRecipeManager().byKey(id(recipeId)).orElseThrow().value();
            helper.assertTrue((double) invoke(recipe,"getChance")==0.05,"Native five percent chance changed");
            var miss = new net.minecraft.world.level.levelgen.LegacyRandomSource(0) {
                @Override public int nextInt(int bound) { return bound-1; }
            };
            helper.assertFalse((boolean) invoke(recipe,"roll",new Class<?>[]{net.minecraft.util.RandomSource.class},miss),
                    "Native failure roll was bypassed");
            var out = level.getBlockState(target).getBlock();
            helper.assertTrue(out==flawed || out==BuiltInRegistries.BLOCK.get(id(outputId)),
                    "Attempt changed mineral family");
        } finally { FlawlessFixerConfig.COST_FE.set(oldCost); }
    }
    private static void tick(BlockEntity host) throws Exception {
        invoke(host,"tickingRequest",new Class<?>[]{Class.forName("appeng.api.networking.IGridNode"),int.class},null,1);
    }
    private static ResourceLocation id(String value) { return ResourceLocation.parse(value); }
    private static Object enumValue(String type,String name) throws Exception { return Class.forName(type).getField(name).get(null); }
    private static Object invoke(Object object,String name) throws Exception { return invoke(object,name,new Class<?>[0]); }
    private static Object invoke(Object object,String name,Class<?>[] types,Object... args) throws Exception {
        var method=object.getClass().getMethod(name,types); method.setAccessible(true); return method.invoke(object,args);
    }
}
