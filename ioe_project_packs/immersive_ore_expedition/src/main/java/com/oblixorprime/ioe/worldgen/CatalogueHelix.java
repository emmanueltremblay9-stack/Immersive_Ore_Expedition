package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-07: two vertically separated windings with explicit strand transfers. */
final class CatalogueHelix {
    private CatalogueHelix() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean second=variant==CatalogueTerraces.Variant.HELIX_TWO;
        boolean offset=variant==CatalogueTerraces.Variant.HELIX_OFFSET;
        var air=new LinkedHashSet<BlockPos>();
        // Entry strand: upper west -> middle north -> lower east/south.
        room(air,6,7,15,1,4);
        room(air,3,7,15,4,5);
        room(air,3,8,15,6,7);
        room(air,offset?7:5,offset?8:6,15,8,12);
        room(air,3,offset?8:6,15,11,12);
        well(air,3,4,6,7,9,17);
        room(air,3,12,9,4,5);
        room(air,3,4,9,4,7);
        well(air,11,12,4,5,3,11);
        room(air,11,12,3,6,9);
        room(air,8,12,3,8,9);
        room(air,8,9,3,8,12);
        room(air,9,11,3,11,12);
        // Opposite strand: upper north/east -> middle south -> lower west/north.
        room(air,10,12,15,4,5);
        room(air,11,12,15,6,9);
        well(air,11,12,8,9,9,17);
        room(air,8,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,5,11,9,11,12);
        room(air,3,6,9,10,11);
        well(air,5,6,11,12,3,11);
        room(air,3,4,3,4,11);
        room(air,3,6,3,10,11);
        // The middle transfer is the sole connection in A/C.
        room(air,8,9,9,6,7);
        if(second) room(air,9,10,15,6,7);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,3,7,14,4,5);
        floor(blocks,base,3,8,14,6,7);
        floor(blocks,base,offset?7:5,offset?8:6,14,8,12);
        floor(blocks,base,3,offset?8:6,14,11,12);
        floor(blocks,base,3,12,8,4,5);
        floor(blocks,base,3,4,8,4,7);
        floor(blocks,base,10,12,14,4,5);
        floor(blocks,base,11,12,14,6,9);
        floor(blocks,base,8,12,8,8,9);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,5,11,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,8,9,8,6,7);
        if(second) floor(blocks,base,9,10,14,6,7);
        if(offset) {
            for(int y=3;y<=17;y++) {
                int x=y<=6?6:y<=11?5:4;
                floor(blocks,base,x,x+1,y,y<=11?7:8,9);
            }
        } else for(int y=3;y<=13;y++) floor(blocks,base,6,7,y,7,9);
        ladder(blocks,base,3,2,6,7,9,15,Direction.EAST);
        floor(blocks,base,4,4,8,6,7);
        floor(blocks,base,4,4,14,6,7);
        ladder(blocks,base,12,13,4,5,3,9,Direction.WEST);
        floor(blocks,base,11,11,8,4,5);
        ladder(blocks,base,12,13,8,9,9,15,Direction.WEST);
        floor(blocks,base,11,11,8,8,9);
        floor(blocks,base,11,11,14,8,9);
        for(int x=5;x<=6;x++) for(int y=3;y<=9;y++) {
            blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        floor(blocks,base,5,6,8,11,11);
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
