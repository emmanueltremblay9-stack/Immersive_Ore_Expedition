package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-24: three interleaved wall-rooted beaks, with a root window or an open upper notch. */
final class CatalogueTeeth {
    private CatalogueTeeth() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean window = variant == CatalogueTerraces.Variant.TEETH_WINDOW;
        boolean notch = variant == CatalogueTerraces.Variant.TEETH_NOTCH;
        var air = new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,4);
        room(air,5,10,15,3,4);
        room(air,10,12,15,3,5);
        room(air,9,10,15,3,8);
        room(air,7,10,15,7,8);
        room(air,5,8,15,8,9);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        // First descent is beyond the upper point; it arrives south of the opposite middle beak.
        well(air,5,6,10,11,9,17);
        room(air,5,12,9,4,5);
        room(air,5,6,9,4,10);
        room(air,5,12,9,8,9);
        room(air,3,6,9,10,11);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        // Second descent is north of the middle beak; the final contour goes around the low tip.
        well(air,11,12,4,5,3,11);
        room(air,3,12,3,4,5);
        room(air,8,9,3,4,12);
        room(air,9,12,3,11,12);
        if (window) room(air,11,12,9,6,7);
        if (notch) room(air,5,6,15,5,7);
        for(int x=7;x<=8;x++) air.add(new BlockPos(x,13,6));
        for(int y=6;y<=7;y++) for(int z=6;z<=7;z++) air.add(new BlockPos(7,y,z));
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        // The upper notch leaves the common rock base intact, so both unequal prongs stay attached.
        for(int x=0;x<=8;x++) for(int z=5;z<=(x<=6 ? 7 : 6);z++) for(int y=14;y<=17;y++) {
            if(notch && x>=5 && x<=6 && y>=15) continue;
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        }
        for(int x=7;x<=14;x++) for(int z=6;z<=7;z++) for(int y=8;y<=12;y++) {
            if(window && x>=11 && x<=12 && y>=9 && y<=11) continue;
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        }
        for(int x=0;x<=7;x++) for(int z=6;z<=7;z++) for(int y=2;y<=5;y++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        link(blocks,base,6,5,10,11,9,15,14);
        link(blocks,base,12,11,4,5,3,9,8);
        return blocks;
    }

    private static void link(Map<BlockPos,BlockState> blocks,BlockPos base,int ladderX,int landingX,
                             int z1,int z2,int y1,int y2,int floorY) {
        for(int z=z1;z<=z2;z++) {
            for(int y=y1;y<=y2;y++) blocks.put(base.offset(ladderX,y,z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            blocks.put(base.offset(landingX,floorY,z),Blocks.STONE.defaultBlockState());
        }
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
}
