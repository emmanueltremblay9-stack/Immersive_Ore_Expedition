package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class NetherDepth32GameTests {
    @GameTest(template="expedition_worldgen_empty",batch="ioe_depth32",timeoutTicks=200)
    public static void externalPrototypeBoundsRoofAndPortal(GameTestHelper helper) {
        var level=helper.getLevel().getServer().getLevel(Level.NETHER);
        if(!NetherDepth32Prototype.requested()) {
            helper.assertTrue(level.getMinBuildHeight()==0,"Normal Nether minimum changed");
            helper.succeed();return;
        }
        helper.assertTrue(level.getMinBuildHeight()==-32 && level.getMaxBuildHeight()==256,"Prototype pack not active or upper boundary changed");
        helper.assertTrue(level.getMinBuildHeight()+level.dimensionType().logicalHeight()==128,"Logical portal ceiling moved");
        helper.assertTrue(level.getChunkSource().getGenerator().getSeaLevel()==32,"Lava sea moved");
        var base=new BlockPos(-900008,-16,-900008);
        level.getChunk(base); // Isolated qualification fixture, never a production load.
        int rock=0;
        for(int dx=0;dx<8;dx++) for(int dz=0;dz<8;dz++) {
            var pos=base.offset(dx,0,dz);
            helper.assertTrue(level.getBlockState(pos.atY(-32)).is(Blocks.BEDROCK),"Bottom bedrock missing");
            helper.assertTrue(level.getBlockState(pos.atY(127)).is(Blocks.BEDROCK),"Roof bedrock moved");
            if(level.getBlockState(pos).is(Blocks.NETHERRACK))rock++;
        }
        helper.assertTrue(rock>0,"New depth contains no substrate");
        var portal=level.getPortalForcer().createPortal(base,Direction.Axis.X);
        helper.assertTrue(portal.isPresent(),"Prototype portal creation failed");
        helper.assertTrue(portal.get().minCorner.getY()>=-32 && portal.get().minCorner.getY()<128,"Portal outside logical vertical bounds");
        helper.assertTrue(helper.getLevel().getMinBuildHeight()==-64 && helper.getLevel().getMaxBuildHeight()==320,"Overworld dimension changed");
        com.mojang.logging.LogUtils.getLogger().info("IOE depth32 bounds: min=-32 upper=256 lava=32 roof=127 bottom=-32 negativeSubstrate={} portalY={} overworldUnchanged=true",rock,portal.get().minCorner.getY());
        helper.succeed();
    }
}
