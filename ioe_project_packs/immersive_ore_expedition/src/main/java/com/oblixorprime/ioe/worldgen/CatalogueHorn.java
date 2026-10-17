package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-15: internally turning sections widen from a narrow neck into a lower mouth. */
final class CatalogueHorn {
    private CatalogueHorn() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean lobes=variant==CatalogueTerraces.Variant.HORN_LOBES;
        boolean awning=variant==CatalogueTerraces.Variant.HORN_AWNING;
        var air=new LinkedHashSet<BlockPos>();
        // The long axis turns from north/south at the neck to east/west below.
        well(air,6,7,3,6,15,17);
        well(air,6,8,4,6,12,14);
        well(air,5,10,5,8,9,11);
        well(air,4,10,5,9,6,8);
        well(air,3,11,5,11,3,5);
        if(!lobes) room(air,7,8,3,4,5);
        room(air,3,4,3,4,5);
        room(air,5,12,3,11,12);
        room(air,6,7,15,1,4);
        room(air,6,8,15,6,7);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        room(air,8,12,15,4,5);
        room(air,11,12,15,4,7);
        room(air,5,12,9,4,5);
        room(air,11,12,9,4,12);
        room(air,5,12,9,11,12);
        room(air,3,4,9,10,11);
        well(air,11,12,6,7,3,17);
        well(air,5,6,11,12,3,17);
        if(awning) room(air,8,10,9,7,9);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,3,11,2,5,11);
        floor(blocks,base,7,8,2,4,5);
        floor(blocks,base,3,4,2,4,5);
        floor(blocks,base,5,12,2,11,12);
        floor(blocks,base,6,7,14,1,6);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,8,12,14,4,5);
        floor(blocks,base,11,12,14,4,7);
        floor(blocks,base,5,12,8,4,5);
        floor(blocks,base,11,12,8,4,12);
        floor(blocks,base,5,12,8,11,12);
        floor(blocks,base,3,4,8,10,11);
        // Thick upper lip, independent of the owned resource masks.
        for(int y=6;y<=8;y++) floor(blocks,base,3,10,y,4,4);
        if(lobes) for(int y=2;y<=7;y++) floor(blocks,base,7,8,y,5,9);
        if(awning) for(int y=6;y<=8;y++) floor(blocks,base,8,10,y,7,9);
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
        for(int y:new int[]{8,14}) {
            floor(blocks,base,11,11,y,6,7);
            floor(blocks,base,5,6,y,11,11);
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
