package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/** Real adjacent AE nodes and public energy storage; no mocked grid or filled Fixer buffer. */
@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class FlawlessFixerGridGameTests {
    private static final double CELL_CHARGE_AE = 600_000;
    private static final int ATTEMPT_FE = 1_000_000;

    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 12_000)
    public static void realAeGridPowersRepresentativeFamiliesAndBothEntroRecipes(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("extendedae")) { helper.succeed(); return; }
        var level = helper.getLevel();
        var charged = BuiltInRegistries.ITEM.get(id("ae2:charged_certus_quartz_crystal"));
        var dustTag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id("c:dusts/ender_pearl"));
        var dust = BuiltInRegistries.ITEM.getTag(dustTag).orElseThrow().iterator().next().value();
        String[][] scenarios = {
                {"ae2:flawed_budding_quartz", "ae2:flawless_budding_quartz"},
                {"immersive_ore_expedition:flawed_budding_iron", "immersive_ore_expedition:flawless_budding_iron"},
                {"immersive_ore_expedition:flawed_budding_amethyst", "immersive_ore_expedition:flawless_budding_amethyst"},
                {"immersive_ore_expedition:flawed_budding_glowstone", "immersive_ore_expedition:flawless_budding_glowstone"},
                {"extendedae:entro_budding_mostly", "immersive_ore_expedition:flawless_budding_entroized_fluix"},
                {"extendedae:entro_budding_mostly", "extendedae:entro_budding_fully"}
        };
        var fixtures = new ArrayList<Fixture>();
        for (int i=0;i<scenarios.length;i++) {
            var pos = helper.absolutePos(new BlockPos(4 + 6*(i%3), 4, 5 + 8*(i/3)));
            helper.assertTrue(level.hasChunkAt(pos), "Grid fixture must stay in already loaded test chunks");
            level.setBlockAndUpdate(pos, block("extendedae:crystal_fixer").defaultBlockState());
            var fixer = level.getBlockEntity(pos);
            var front = (Direction) call(fixer,"getFront");
            var cellPos = pos.relative(front.getOpposite());
            var target = pos.relative(front);
            helper.assertTrue(level.hasChunkAt(cellPos) && level.hasChunkAt(target), "Grid fixture crossed an unloaded boundary");
            level.setBlockAndUpdate(cellPos,block("ae2:dense_energy_cell").defaultBlockState());
            var input = block(scenarios[i][0]);
            var output = block(scenarios[i][1]);
            level.setBlockAndUpdate(target,input.defaultBlockState());
            // Keep fuel absent until the nodes are ready, so no earlier tick snapshots a different test budget.
            fixtures.add(new Fixture(pos,cellPos,target,fixer,level.getBlockEntity(cellPos),input,output,
                    new ItemStack(i==5?dust:charged),i!=5));
        }
        // Let AE2 initialize and physically join the adjacent nodes through its normal first-tick lifecycle.
        later(helper, () -> awaitNetworks(helper,fixtures,0));
    }

    private static void awaitNetworks(GameTestHelper helper, List<Fixture> fixtures, int elapsed) throws Exception {
        var grids = new java.util.HashSet<Object>();
        for (var fixture : fixtures) {
            var node = call(call(fixture.fixer,"getMainNode"),"getNode");
            var cellNode = call(call(fixture.cell,"getMainNode"),"getNode");
            if (node == null || cellNode == null || call(node,"getGrid") != call(cellNode,"getGrid")) {
                helper.assertTrue(elapsed<100,"Native adjacent AE grid failed to initialize");
                later(helper, () -> awaitNetworks(helper,fixtures,elapsed+1));
                return;
            }
            grids.add(call(node,"getGrid"));
        }
        helper.assertTrue(grids.size()==fixtures.size(),"Independent grid fixtures unexpectedly connected");
        helper.assertTrue(FlawlessFixerConfig.COST_FE.get()==ATTEMPT_FE, "Expected the real default million-FE budget");
        {
            for (var fixture : fixtures) {
                var node = call(call(fixture.fixer,"getMainNode"),"getNode");
                var grid = call(node,"getGrid");
                fixture.energyService = call(grid,"getEnergyService");
                helper.assertTrue(Math.abs((double) call(fixture.energyService,"getIdlePowerUsage"))<1e-9,
                        "Grid fixture has an unrelated idle power drain");
                helper.assertTrue(power(fixture.fixer)==0 && power(fixture.cell)==0,"Fixture was not initially unpowered");
                call(call(fixture.fixer,"getInternalInventory"),"setItemDirect",
                        new Class<?>[]{int.class,ItemStack.class},0,fixture.fuel.copy());
                call(fixture.fixer,"onChanged");
                // Snapshot the unchanged default budget while unpowered. No energy or progress is supplied to the Fixer.
                for (int i=0;i<3;i++) call(fixture.fixer,"tickingRequest",
                        new Class<?>[]{Class.forName("appeng.api.networking.IGridNode"),int.class},node,1);
                if (fixture.premium) {
                    var ledger = fixture.fixer.getPersistentData().getCompound("ioe_flawless_payment");
                    helper.assertTrue(ledger.getDouble("cost")==ATTEMPT_FE && ledger.getDouble("paid")==0,
                            "Unpowered premium attempt did not snapshot its budget");
                } else {
                    helper.assertTrue((int) call(fixture.fixer,"getProgress")==0,"Ordinary repair progressed without power");
                }
                Object modulate = enumValue("appeng.api.config.Actionable","MODULATE");
                double overflow = (double) call(fixture.cell,"injectAEPower",
                        new Class<?>[]{double.class,modulate.getClass()},CELL_CHARGE_AE,modulate);
                helper.assertTrue(overflow==0 && power(fixture.cell)==CELL_CHARGE_AE,"Native cell rejected the fixture charge");
                call(fixture.fixer,"onChanged");
            }
        }
        // From here on, only the real AE tick manager executes the Fixers.
        later(helper, () -> observeAttempts(helper,fixtures,0));
    }

    private static void observeAttempts(GameTestHelper helper, List<Fixture> fixtures, int elapsed) throws Exception {
        boolean allComplete = true;
        for (var fixture : fixtures) {
            helper.assertTrue(power(fixture.fixer)==0,"Fixer buffer was filled: this no longer proves the grid debit path");
            helper.assertTrue(Math.abs((double) call(fixture.energyService,"getIdlePowerUsage"))<1e-9,
                    "Unrelated idle drain invalidates the exact grid accounting fixture");
            var ledger = fixture.fixer.getPersistentData().getCompound("ioe_flawless_payment");
            if (ledger.getDouble("paid")>0 && ledger.getDouble("paid")<ATTEMPT_FE) fixture.observedPartial = true;
            var inv = call(fixture.fixer,"getInternalInventory");
            var fuel = (ItemStack) call(inv,"getStackInSlot",new Class<?>[]{int.class},0);
            double remaining = power(fixture.cell);
            if (!fuel.isEmpty()) {
                allComplete = false;
                helper.assertTrue(!fixture.complete,"A completed attempt regained fuel");
                continue;
            }
            double debit = CELL_CHARGE_AE-remaining;
            if (fixture.premium) {
                helper.assertTrue(fixture.observedPartial,"No partial grid-funded payment was observed");
                helper.assertTrue(Math.abs(FlawlessFixerPayment.convert("AE","FE",debit)-ATTEMPT_FE)<1e-6,
                        "Real grid did not pay the exact configured FE budget: "+debit);
            } else {
                var multiplier = enumValue("appeng.api.config.PowerMultiplier","CONFIG");
                double lower = (double) call(multiplier,"multiply",new Class<?>[]{double.class},5000.0);
                double upper = (double) call(multiplier,"multiply",new Class<?>[]{double.class},5100.0);
                helper.assertTrue(debit>=lower-1e-6 && debit<=upper+1e-6,
                        "Native Entro repair no longer uses its ordinary progress/energy cost: "+debit);
            }
            helper.assertFalse(fixture.fixer.getPersistentData().contains("ioe_flawless_payment"),"Completed attempt retained paid credit");
            var result = helper.getLevel().getBlockState(fixture.target).getBlock();
            helper.assertTrue(result==fixture.input || result==fixture.output,"Grid-powered attempt changed family or recipe output");
            if (fixture.complete) helper.assertTrue(remaining==fixture.completedBalance,"Grid charged a completed attempt twice");
            fixture.complete = true;
            fixture.completedBalance = remaining;
        }
        if (allComplete) {
            // Keep the energized grids ticking after all fuel is consumed to catch an extra debit.
            later(helper, () -> verifyQuietGrids(helper,fixtures,0));
        } else {
            helper.assertTrue(elapsed<11_000,"AE tick manager did not complete the grid-powered attempts");
            later(helper, () -> observeAttempts(helper,fixtures,elapsed+1));
        }
    }

    private static void verifyQuietGrids(GameTestHelper helper, List<Fixture> fixtures, int elapsed) throws Exception {
        for (var fixture : fixtures)
            helper.assertTrue(power(fixture.cell)==fixture.completedBalance && power(fixture.fixer)==0,
                    "Completed grid-powered attempt continued debiting energy");
        if (elapsed<10) { later(helper, () -> verifyQuietGrids(helper,fixtures,elapsed+1)); return; }
        for (var fixture : fixtures) {
            helper.getLevel().removeBlock(fixture.pos,false);
            helper.getLevel().removeBlock(fixture.cellPos,false);
            helper.getLevel().removeBlock(fixture.target,false);
        }
        helper.succeed();
    }

    /** Native IE generation -> NeoForge FE capability -> AE acceptor -> grid -> native Entro repair. */
    @GameTest(template = "expedition_worldgen_empty", timeoutTicks = 3000)
    public static void externalThermoelectricGeneratorPowersAeGrid(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("extendedae") || !ModList.get().isLoaded("immersiveengineering")) {
            helper.succeed(); return;
        }
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(10,8,10));
        level.setBlockAndUpdate(pos,block("extendedae:crystal_fixer").defaultBlockState());
        var fixer = level.getBlockEntity(pos);
        var front = (Direction) call(fixer,"getFront");
        var cellPos = pos.relative(front.getOpposite());
        var acceptorPos = cellPos.below();
        var generatorPos = acceptorPos.below();
        var hotPos = generatorPos.east();
        var coldPos = generatorPos.west();
        var target = pos.relative(front);
        var positions = List.of(pos,cellPos,acceptorPos,generatorPos,hotPos,coldPos,target);
        for (var part : positions) helper.assertTrue(level.hasChunkAt(part),"External generator fixture crossed an unloaded boundary");
        level.setBlockAndUpdate(cellPos,block("ae2:energy_cell").defaultBlockState());
        level.setBlockAndUpdate(acceptorPos,block("ae2:energy_acceptor").defaultBlockState());
        var input = block("extendedae:entro_budding_mostly");
        var output = block("extendedae:entro_budding_fully");
        level.setBlockAndUpdate(target,input.defaultBlockState());
        var cell = level.getBlockEntity(cellPos);
        helper.assertTrue(power(cell)==0 && power(fixer)==0,"External fixture did not start empty");
        level.setBlockAndUpdate(generatorPos,block("immersiveengineering:thermoelectric_generator").defaultBlockState());
        // Stable solid sources: no flowing fluid, forced generation call, injected charge or config change.
        level.setBlockAndUpdate(hotPos,net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(coldPos,net.minecraft.world.level.block.Blocks.BLUE_ICE.defaultBlockState());
        var inlet = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                acceptorPos,Direction.DOWN);
        helper.assertTrue(inlet!=null && inlet.canReceive(),"AE acceptor exposes no FE inlet toward the generator");
        var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,id("c:dusts/ender_pearl"));
        var dust = BuiltInRegistries.ITEM.getTag(tag).orElseThrow().iterator().next().value();
        var fixture = new Fixture(pos,cellPos,target,fixer,cell,input,output,new ItemStack(dust),false);
        later(helper, () -> observeExternalGenerator(helper,fixture,positions,acceptorPos,0,false,false));
    }

    private static void observeExternalGenerator(GameTestHelper helper, Fixture fixture, List<BlockPos> positions,
            BlockPos acceptorPos, int elapsed, boolean fueled, boolean partial) throws Exception {
        helper.assertTrue(elapsed<2900,"Native thermoelectric supply did not finish the native Entro repair");
        helper.assertTrue(power(fixture.fixer)==0,"External fixture bypassed its AE network through the Fixer buffer");
        helper.assertFalse(fixture.fixer.getPersistentData().contains("ioe_flawless_payment"),"Native repair acquired premium credit");
        if (!fueled) {
            var node = call(call(fixture.fixer,"getMainNode"),"getNode");
            var cellNode = call(call(fixture.cell,"getMainNode"),"getNode");
            var acceptor = helper.getLevel().getBlockEntity(acceptorPos);
            var acceptorNode = call(call(acceptor,"getMainNode"),"getNode");
            if (node!=null && cellNode!=null && acceptorNode!=null && power(fixture.cell)>0) {
                helper.assertTrue(call(node,"getGrid")==call(cellNode,"getGrid")
                        && call(node,"getGrid")==call(acceptorNode,"getGrid"),"Generator supplied a different AE grid");
                call(call(fixture.fixer,"getInternalInventory"),"setItemDirect",
                        new Class<?>[]{int.class,ItemStack.class},0,fixture.fuel.copy());
                call(fixture.fixer,"onChanged");
                fueled=true;
            }
        } else {
            int progress = (int) call(fixture.fixer,"getProgress");
            partial |= progress>0 && progress<100;
            var fuel = (ItemStack) call(call(fixture.fixer,"getInternalInventory"),"getStackInSlot",new Class<?>[]{int.class},0);
            if (fuel.isEmpty()) {
                helper.assertTrue(partial,"No generator-powered native repair progress observed");
                var result = helper.getLevel().getBlockState(fixture.target).getBlock();
                helper.assertTrue(result==fixture.input || result==fixture.output,"External supply selected a different Entro recipe");
                for (var part : positions) helper.getLevel().removeBlock(part,false);
                helper.succeed(); return;
            }
        }
        boolean nextFueled=fueled, nextPartial=partial;
        later(helper, () -> observeExternalGenerator(helper,fixture,positions,acceptorPos,elapsed+1,nextFueled,nextPartial));
    }

    private static final class Fixture {
        final BlockPos pos, cellPos, target;
        final BlockEntity fixer, cell;
        final Block input, output;
        final ItemStack fuel;
        final boolean premium;
        Object energyService;
        boolean observedPartial, complete;
        double completedBalance;
        Fixture(BlockPos pos, BlockPos cellPos, BlockPos target, BlockEntity fixer, BlockEntity cell,
                Block input, Block output, ItemStack fuel, boolean premium) {
            this.pos=pos; this.cellPos=cellPos; this.target=target; this.fixer=fixer; this.cell=cell;
            this.input=input; this.output=output; this.fuel=fuel; this.premium=premium;
        }
    }
    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
    private static void later(GameTestHelper helper, CheckedAction action) {
        helper.runAfterDelay(1, () -> {
            try { action.run(); } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Unsupported native AE grid API",failure);
            } catch (Exception failure) { throw new RuntimeException(failure); }
        });
    }
    private static double power(Object owner) throws Exception { return (double) call(owner,"getAECurrentPower"); }
    private static ResourceLocation id(String value) { return ResourceLocation.parse(value); }
    private static Block block(String value) { return BuiltInRegistries.BLOCK.getOptional(id(value)).orElseThrow(); }
    private static Object enumValue(String type, String name) throws Exception { return Class.forName(type).getField(name).get(null); }
    private static Object call(Object owner, String name, Class<?>[] types, Object... args) throws Exception {
        var method=owner.getClass().getMethod(name,types); method.setAccessible(true); return method.invoke(owner,args);
    }
    private static Object call(Object owner, String name) throws Exception { return call(owner,name,new Class<?>[0]); }
}
