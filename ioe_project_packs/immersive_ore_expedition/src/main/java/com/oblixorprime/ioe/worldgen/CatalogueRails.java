package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-37: two rising side rails, three connected crossbars, unchanged mineral reservations. */
final class CatalogueRails {
    private CatalogueRails() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center, CatalogueTerraces.Variant variant) {
        boolean slopes = variant == CatalogueTerraces.Variant.RAILS_SLOPES;
        boolean fork = variant == CatalogueTerraces.Variant.RAILS_FORK;
        var air = new LinkedHashSet<BlockPos>();
        // Both rails rise toward +Z, using offset backed wells and real landings.
        room(air,2,6,3,3,5);
        room(air,2,5,9,4,10);
        room(air,2,5,14,6,9);
        room(air,10,12,4,4,8);
        room(air,10,12,10,4,9);
        room(air,10,12,15,4,12);
        well(air,2,3,4,5,3,11);
        well(air,2,3,6,7,9,16);
        well(air,11,12,4,5,4,12);
        well(air,11,12,8,9,10,17);
        // Upper crossbar and entry branch: only this crossbar connects the upper rails.
        room(air,4,12,15,11,12);
        room(air,4,5,15,10,10);
        room(air,2,5,15,11,12);
        room(air,6,7,15,1,11);
        // Fixed middle alcoves are terminal branches, not extra inter-rail crossings.
        room(air,3,8,9,4,5);
        room(air,8,12,9,11,12);
        room(air,8,12,10,9,9);
        room(air,8,9,10,10,10);
        // Lower east alcove stays at Y=3 despite the east rail's Y=4 landing.
        room(air,8,12,3,11,12);
        room(air,8,9,3,10,12);
        if (fork) {
            room(air,3,5,3,4,9);
            room(air,5,8,3,4,5);
            room(air,5,8,3,8,9);
            room(air,9,12,4,4,9);
        } else {
            room(air,3,6,3,4,5);
            room(air,7,12,4,4,5);
            room(air,8,12,4,6,8);
            room(air,8,9,4,9,9);
        }
        if (slopes) {
            room(air,3,3,9,6,7);
            room(air,4,4,10,6,7);
            room(air,5,8,11,6,7);
            room(air,9,12,10,6,7);
            // N7 keeps its original Y=9 front, reached by a terminal descent from the landing.
            room(air,6,7,11,8,8);
            room(air,6,7,10,9,9);
            room(air,4,7,9,10,10);
        } else {
            room(air,3,6,9,6,7);
            room(air,7,12,10,6,7);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        ladder(blocks,base,2,4,5,3,9,Direction.EAST);
        floor(blocks,base,3,3,8,4,5);
        ladder(blocks,base,2,6,7,9,14,Direction.EAST);
        floor(blocks,base,3,3,13,6,7);
        ladder(blocks,base,12,4,5,4,10,Direction.WEST);
        floor(blocks,base,11,11,9,4,5);
        ladder(blocks,base,12,8,9,10,15,Direction.WEST);
        floor(blocks,base,11,11,14,8,9);
        stairs(blocks,base,4,5,14,10,10,Direction.SOUTH);
        floor(blocks,base,4,5,14,11,12);
        stairs(blocks,base,8,9,9,10,10,Direction.NORTH);
        if (fork) {
            stairs(blocks,base,9,9,3,4,5,Direction.EAST);
            stairs(blocks,base,9,9,3,8,9,Direction.EAST);
        } else {
            stairs(blocks,base,7,7,3,4,5,Direction.EAST);
            stairs(blocks,base,8,9,3,9,9,Direction.NORTH);
        }
        if (slopes) {
            stairs(blocks,base,4,4,9,6,7,Direction.EAST);
            stairs(blocks,base,5,5,10,6,7,Direction.EAST);
            floor(blocks,base,6,7,10,6,7);
            stairs(blocks,base,8,8,10,6,7,Direction.WEST);
            stairs(blocks,base,6,7,10,8,8,Direction.NORTH);
            stairs(blocks,base,6,7,9,9,9,Direction.NORTH);
        } else stairs(blocks,base,7,7,9,6,7,Direction.EAST);
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int z1,int z2,int y1,int y2,Direction facing) {
        for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
    }
    private static void stairs(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2,Direction facing) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) {
            blocks.put(base.offset(x,y-1,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
        }
    }
}
