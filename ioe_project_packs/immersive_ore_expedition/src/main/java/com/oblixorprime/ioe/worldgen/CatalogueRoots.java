package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-06: a supported trunk with deep or staged forks, optionally joined by one lateral connection. */
final class CatalogueRoots {
    private CatalogueRoots() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean staged = variant == CatalogueTerraces.Variant.ROOTS_STAGED;
        boolean joined = variant == CatalogueTerraces.Variant.ROOTS_JOINED;
        var air = new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,8);
        room(air,6,12,15,4,5);
        room(air,5,7,15,8,10);
        room(air,5,6,15,10,12);
        room(air,3,6,15,11,12);
        room(air,6,7,9,4,8);
        room(air,5,6,9,8,10);
        room(air,3,6,9,10,11);
        room(air,6,9,9,6,7);
        room(air,8,9,9,7,12);
        room(air,9,11,9,11,12);
        well(air,6,7,6,7,staged ? 9 : 3,17);
        if (staged) {
            // The northern branch skirts the trunk's solid backing, then forks downward.
            room(air,3,7,9,4,5);
            room(air,3,4,9,4,7);
            well(air,2,3,6,7,3,11);
            room(air,3,4,3,4,7);
        } else {
            room(air,6,7,3,4,7);
            room(air,3,7,3,4,5);
            room(air,6,9,3,6,7);
        }
        room(air,8,9,3,staged ? 8 : 7,12);
        room(air,9,11,3,11,12);
        if (staged || joined) {
            room(air,8,12,9,8,9);
            room(air,8,12,3,8,9);
            well(air,11,12,8,9,3,11);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        // This trunk is inside the footprint: its backing must be explicit, even beside a branch.
        for(int z=6;z<=7;z++) {
            for(int y=staged ? 9 : 3;y<=15;y++) {
                blocks.put(base.offset(5,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(6,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            blocks.put(base.offset(7,14,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(7,8,z),Blocks.STONE.defaultBlockState());
        }
        if (staged) link(blocks,base,2,3,6,7,Direction.EAST);
        if (staged || joined) link(blocks,base,12,11,8,9,Direction.WEST);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }

    private static void link(Map<BlockPos,BlockState> blocks,BlockPos base,int ladderX,int landingX,
                             int z1,int z2,Direction facing) {
        for(int z=z1;z<=z2;z++) {
            for(int y=3;y<=9;y++) blocks.put(base.offset(ladderX,y,z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
            blocks.put(base.offset(landingX,8,z),Blocks.STONE.defaultBlockState());
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
