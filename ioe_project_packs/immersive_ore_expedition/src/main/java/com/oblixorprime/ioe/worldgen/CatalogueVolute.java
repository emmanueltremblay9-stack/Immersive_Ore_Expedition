package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-32: an open, tightening volute with separated outer and inner portions. */
final class CatalogueVolute {
    private CatalogueVolute() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean window=variant==CatalogueTerraces.Variant.VOLUTE_WINDOW;
        boolean fork=variant==CatalogueTerraces.Variant.VOLUTE_FORK;
        var air=new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,4);
        room(air,3,11,15,4,5);
        room(air,3,8,15,6,7);
        room(air,5,6,15,8,12);
        room(air,3,5,15,11,12);
        well(air,3,4,6,7,9,17);
        room(air,3,6,9,6,7);
        room(air,5,6,9,8,12);
        room(air,5,12,9,11,12);
        room(air,3,6,9,10,11);
        room(air,5,6,9,4,7);
        room(air,6,8,9,4,5);
        room(air,8,9,9,8,12);
        room(air,8,12,9,8,9);
        well(air,11,12,8,9,3,11);
        room(air,8,12,3,8,9);
        room(air,5,9,3,7,9);
        room(air,9,10,4,8,9);
        room(air,5,6,3,4,7);
        room(air,3,4,3,4,5);
        room(air,7,8,3,8,12);
        room(air,9,11,3,11,12);
        if(window) {
            well(air,5,6,10,11,3,11);
            room(air,5,6,3,7,11);
            room(air,2,3,9,10,13);
            room(air,2,6,9,12,13);
        }
        if(fork) {
            room(air,7,8,5,4,6);
            room(air,7,8,4,7,7);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,3,11,14,4,5);
        floor(blocks,base,3,8,14,6,7);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,5,14,11,12);
        floor(blocks,base,3,6,8,6,7);
        floor(blocks,base,5,6,8,8,12);
        floor(blocks,base,5,12,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,5,6,8,4,7);
        floor(blocks,base,6,8,8,4,5);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,8,12,8,8,9);
        for(int y=6;y<=13;y++) floor(blocks,base,7,10,y,6,7);
        ladder(blocks,base,3,2,6,7,9,15,Direction.EAST);
        floor(blocks,base,4,4,8,6,7);
        floor(blocks,base,4,4,14,6,7);
        ladder(blocks,base,12,13,8,9,3,9,Direction.WEST);
        floor(blocks,base,11,11,8,8,9);
        // A short supported rise precedes the inward terminal, without adding a winding.
        for(int z=8;z<=9;z++) {
            blocks.put(base.offset(10,2,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(9,2,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(10,3,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
            blocks.put(base.offset(9,3,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        }
        if(window) {
            ladder(blocks,base,5,4,10,11,3,9,Direction.EAST);
            floor(blocks,base,6,6,8,10,11);
            floor(blocks,base,2,3,8,10,13);
            floor(blocks,base,2,6,8,12,13);
        }
        if(fork) {
            floor(blocks,base,7,8,4,4,6);
            for(int x=7;x<=8;x++) {
                blocks.put(base.offset(x,2,7),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,3,7),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
                blocks.put(base.offset(x,3,6),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,4,6),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
            }
            for(int x=7;x<=8;x++) for(int y=4;y<=6;y++)
                blocks.put(base.offset(x,y,7),Blocks.AIR.defaultBlockState());
            // Reopen the terminal above its stairs without cutting the middle floor at Y=8.
            for(int x=7;x<=8;x++) for(int y=5;y<=7;y++) for(int z=4;z<=6;z++)
                blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
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
