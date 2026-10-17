package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-28: one closed circuit through three unequal lobes with separated overpasses. */
final class CatalogueKnot {
    private CatalogueKnot() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean transfer=variant==CatalogueTerraces.Variant.KNOT_TRANSFER;
        boolean split=variant==CatalogueTerraces.Variant.KNOT_SPLIT;
        var air=new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,4);
        room(air,5,8,15,4,5);
        room(air,5,6,15,6,9);
        room(air,5,12,15,8,9);
        room(air,7,7,15,7,7);
        room(air,11,12,15,4,12);
        room(air,10,11,15,4,5);
        room(air,3,12,15,11,12);
        room(air,3,4,15,12,13);
        well(air,3,4,12,13,3,17);
        room(air,3,4,3,4,12);
        room(air,3,4,3,12,13);
        room(air,3,10,3,4,5);
        room(air,9,10,3,6,9);
        room(air,8,10,3,8,9);
        room(air,8,9,3,10,12);
        room(air,8,11,3,12,13);
        well(air,9,10,12,13,3,11);
        room(air,8,11,9,12,13);
        room(air,8,9,9,8,12);
        room(air,5,9,9,8,9);
        room(air,5,6,9,4,9);
        room(air,5,8,9,4,5);
        well(air,7,8,4,5,9,17);
        room(air,3,4,9,10,13);
        room(air,3,4,9,12,13);
        if(transfer) room(air,5,6,9,10,11);
        if(split) {
            room(air,6,7,3,6,9);
            room(air,6,9,3,8,9);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,5,8,14,4,5);
        floor(blocks,base,5,6,14,6,9);
        floor(blocks,base,5,12,14,8,9);
        floor(blocks,base,7,7,14,7,7);
        floor(blocks,base,11,12,14,4,12);
        floor(blocks,base,10,11,14,4,5);
        floor(blocks,base,3,12,14,11,12);
        floor(blocks,base,3,4,14,12,13);
        floor(blocks,base,3,4,2,4,12);
        floor(blocks,base,3,4,2,12,13);
        floor(blocks,base,3,10,2,4,5);
        floor(blocks,base,9,10,2,6,9);
        floor(blocks,base,8,10,2,8,9);
        floor(blocks,base,8,9,2,10,12);
        floor(blocks,base,8,11,2,12,13);
        floor(blocks,base,8,11,8,12,13);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,5,9,8,8,9);
        floor(blocks,base,5,6,8,4,9);
        floor(blocks,base,5,8,8,4,5);
        floor(blocks,base,3,4,8,10,13);
        floor(blocks,base,3,4,8,12,13);
        if(transfer) floor(blocks,base,5,6,8,10,11);
        if(split) {
            floor(blocks,base,6,7,2,6,9);
            floor(blocks,base,6,9,2,8,9);
        }
        // The lower lobe is a detour around this retained non-mineral blade.
        for(int y=2;y<=6;y++) floor(blocks,base,8,8,y,6,7);
        for(int x=3;x<=4;x++) for(int y=3;y<=15;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        for(int y:new int[]{8,14}) floor(blocks,base,3,4,y,12,12);
        for(int x=9;x<=10;x++) for(int y=3;y<=9;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        floor(blocks,base,9,10,8,12,12);
        for(int z=4;z<=5;z++) for(int y=9;y<=15;y++) {
            blocks.put(base.offset(9,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(8,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int y:new int[]{8,14}) floor(blocks,base,7,7,y,4,5);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int foot,int z1,int z2) { well(air,x1,x2,z1,z2,foot,foot+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
