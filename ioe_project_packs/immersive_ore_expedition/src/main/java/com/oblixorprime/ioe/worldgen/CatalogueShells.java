package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-21: distinct inner and outer voids, one window, opposite windows or an open sector. */
final class CatalogueShells {
    private CatalogueShells() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean opposed=variant==CatalogueTerraces.Variant.SHELLS_OPPOSED;
        boolean opened=variant==CatalogueTerraces.Variant.SHELLS_OPEN;
        var air=new LinkedHashSet<BlockPos>();
        room(air,6,9,4,7,9);
        room(air,6,9,9,7,9);
        well(air,6,7,7,8,4,11);
        room(air,3,12,3,4,5);
        room(air,6,12,9,4,5);
        room(air,6,12,15,4,5);
        room(air,11,12,3,4,8);
        room(air,11,12,9,4,9);
        room(air,11,12,15,4,7);
        room(air,3,11,3,11,12);
        room(air,3,11,9,11,12);
        room(air,3,6,15,11,12);
        room(air,3,4,3,4,12);
        room(air,3,4,9,10,11);
        room(air,6,7,15,1,4);
        room(air,5,8,15,6,9);
        room(air,5,6,15,9,12);
        room(air,8,12,15,6,7);
        well(air,3,4,8,9,5,11);
        well(air,11,12,7,9,5,11);
        well(air,6,8,4,5,5,11);
        well(air,11,12,6,7,3,17);
        well(air,5,6,11,12,3,17);
        room(air,10,12,9,opened?7:8,opened?8:9);
        if(opposed) {
            room(air,5,6,4,8,9);
            room(air,3,4,4,8,9);
        }
        if(opened) {
            well(air,10,10,7,8,4,11);
            room(air,11,12,4,7,8);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,9,3,7,9);
        floor(blocks,base,6,9,8,7,9);
        floor(blocks,base,6,12,8,4,5);
        floor(blocks,base,6,12,14,4,5);
        floor(blocks,base,11,12,8,4,9);
        floor(blocks,base,11,12,14,4,7);
        floor(blocks,base,3,11,8,11,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,3,4,8,10,11);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,5,8,14,6,9);
        floor(blocks,base,5,6,14,9,12);
        floor(blocks,base,8,12,14,6,7);
        floor(blocks,base,6,9,13,7,9);
        floor(blocks,base,10,10,8,opened?7:8,opened?8:9);
        if(opposed) {
            floor(blocks,base,5,6,3,8,9);
            stairRow(blocks,base,3,4,3,8,Direction.SOUTH);
            stairRow(blocks,base,3,4,3,9,Direction.NORTH);
        }
        if(opened) {
            floor(blocks,base,10,10,3,7,8);
            for(int z=7;z<=8;z++) {
                blocks.put(base.offset(11,2,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(11,3,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
            }
            for(int y=4;y<=11;y++) for(int z:new int[]{6,9})
                blocks.put(base.offset(10,y,z),Blocks.STONE.defaultBlockState());
        }
        for(int y=3;y<=15;y++) {
            for(int z=6;z<=7;z++) {
                blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            for(int x=5;x<=6;x++) {
                blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            }
        }
        for(int y=4;y<=9;y++) for(int x=6;x<=7;x++) {
            blocks.put(base.offset(x,y,6),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,7),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,11,11,y,6,7);
            floor(blocks,base,5,6,y,11,11);
        }
        floor(blocks,base,6,7,8,8,8);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void stairRow(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z,Direction facing) {
        for(int x=x1;x<=x2;x++) {
            blocks.put(base.offset(x,y-1,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
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
