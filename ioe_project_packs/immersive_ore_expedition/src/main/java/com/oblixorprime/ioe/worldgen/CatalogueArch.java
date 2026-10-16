package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-34: unequal rooted piers, open lower crossing and a rising accessible deck. */
final class CatalogueArch {
    private CatalogueArch() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean window=variant==CatalogueTerraces.Variant.ARCH_WINDOW;
        boolean fork=variant==CatalogueTerraces.Variant.ARCH_FORK;
        var air=new LinkedHashSet<BlockPos>();
        well(air,7,9,4,8,3,11);
        well(air,7,9,5,6,3,13);
        well(air,3,12,4,4,3,12);
        well(air,3,12,7,8,3,12);
        well(air,12,12,5,6,3,14);
        room(air,3,6,3,3,4);
        room(air,2,4,3,7,8);
        // Supported west bypass keeps N1 independent of B's local window.
        room(air,1,2,3,4,8);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,6,12,9,4,5);
        room(air,8,9,9,4,9);
        room(air,5,12,9,7,8);
        room(air,5,6,9,8,11);
        room(air,3,6,9,10,11);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,3,4,13,4,8);
        room(air,5,5,14,4,8);
        room(air,6,11,15,4,8);
        room(air,6,7,15,1,4);
        room(air,11,12,15,7,8);
        room(air,2,4,13,7,8);
        well(air,5,6,10,11,9,17);
        room(air,5,6,15,11,12);
        room(air,3,5,15,11,12);
        well(air,1,2,7,8,3,15);
        well(air,12,13,7,8,3,17);
        if(window) {
            room(air,1,9,3,9,10);
            room(air,4,5,5,3,8);
            for(int z:new int[]{3,7}) {
                room(air,6,6,4,z,z+1);
                room(air,7,8,3,z,z+1);
            }
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,12,8,4,5);
        floor(blocks,base,8,9,8,4,9);
        floor(blocks,base,5,12,8,7,8);
        floor(blocks,base,5,6,8,8,11);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,3,4,12,4,8);
        floor(blocks,base,7,11,14,4,8);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,11,12,14,7,8);
        floor(blocks,base,2,4,12,7,8);
        floor(blocks,base,5,6,14,11,12);
        floor(blocks,base,3,5,14,11,12);
        rock(blocks,base,3,6,2,12,5,6);
        rock(blocks,base,10,11,2,14,5,6);
        stairs(blocks,base,5,13,4,8);
        stairs(blocks,base,6,14,4,8);
        if(fork) for(int x=7;x<=9;x++) for(int y=12;y<=17;y++)
            blocks.put(base.offset(x,y,6),Blocks.AIR.defaultBlockState());
        ladder(blocks,base,1,0,7,8,3,13,Direction.EAST);
        floor(blocks,base,2,2,12,7,8);
        ladder(blocks,base,13,14,7,8,3,15,Direction.WEST);
        floor(blocks,base,12,12,8,7,8);
        floor(blocks,base,12,12,14,7,8);
        ladder(blocks,base,6,7,10,11,9,15,Direction.WEST);
        floor(blocks,base,5,5,14,10,11);
        if(window) {
            for(int x=4;x<=5;x++) for(int y=5;y<=7;y++) for(int z=5;z<=6;z++)
                blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
            for(int z:new int[]{3,7}) {
                floor(blocks,base,4,5,4,z,z+1);
                stairs(blocks,base,5,4,z,z+1,Direction.WEST);
                stairs(blocks,base,6,3,z,z+1,Direction.WEST);
            }
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void stairs(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int y,int z1,int z2) {
        stairs(blocks,base,x,y,z1,z2,Direction.EAST);
    }
    private static void stairs(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int y,int z1,int z2,Direction facing) {
        for(int z=z1;z<=z2;z++) {
            blocks.put(base.offset(x,y-1,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
        }
    }
    private static void rock(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y1,int y2,int z1,int z2) {
        for(int y=y1;y<=y2;y++) floor(blocks,base,x1,x2,y,z1,z2);
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int back,int z1,int z2,int y1,int y2,Direction facing) {
        for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) {
            blocks.put(base.offset(back,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
        }
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
