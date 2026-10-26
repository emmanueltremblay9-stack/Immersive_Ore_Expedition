package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-29: interleaved branched networks with only explicitly admitted transfers. */
final class CatalogueNetworks {
    private CatalogueNetworks() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean second=variant==CatalogueTerraces.Variant.NETWORKS_TWO;
        boolean high=variant==CatalogueTerraces.Variant.NETWORKS_HIGH;
        var air=new LinkedHashSet<BlockPos>();
        // P: low N1, middle N6, high N3 and a separate upper terminal branch.
        well(air,5,6,6,7,3,17);
        room(air,3,6,3,4,5);
        room(air,5,6,3,4,7);
        room(air,5,6,9,4,7);
        room(air,6,7,9,4,5);
        room(air,6,7,15,1,4);
        room(air,2,7,15,4,5);
        room(air,6,8,15,6,7);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        // Q: low N4, middle N2/N7, and high N5.
        well(air,11,12,6,7,3,17);
        room(air,8,12,3,6,7);
        room(air,8,9,3,8,12);
        room(air,9,11,3,11,12);
        room(air,11,12,9,6,9);
        room(air,8,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,3,11,9,11,12);
        room(air,3,6,9,10,11);
        room(air,10,11,15,4,7);
        if(!high) room(air,7,9,9,6,7);
        if(second) room(air,7,8,3,6,7);
        if(high) {
            room(air,9,10,15,6,7);
            room(air,2,3,15,6,9);
            room(air,2,6,15,8,9);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,5,6,8,4,7);
        floor(blocks,base,6,7,8,4,5);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,2,7,14,4,5);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,11,12,8,6,9);
        floor(blocks,base,8,12,8,8,9);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,3,11,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,10,11,14,4,7);
        if(!high) floor(blocks,base,7,9,8,6,7);
        if(high) {
            floor(blocks,base,9,10,14,6,7);
            floor(blocks,base,2,3,14,6,9);
            floor(blocks,base,2,6,14,8,9);
        }
        for(int y=3;y<=15;y++) for(int z=6;z<=7;z++) {
            blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,6,6,y,6,7);
            floor(blocks,base,11,11,y,6,7);
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
