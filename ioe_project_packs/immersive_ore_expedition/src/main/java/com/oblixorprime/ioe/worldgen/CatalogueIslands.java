package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-09: unequal host-rock islands, a roofed fork, or a connected depression. */
final class CatalogueIslands {
    private CatalogueIslands() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean slab=variant==CatalogueTerraces.Variant.ISLANDS_SLAB;
        boolean hollow=variant==CatalogueTerraces.Variant.ISLANDS_HOLLOW;
        var air=new LinkedHashSet<BlockPos>();
        room(air,3,4,3,4,11);
        room(air,5,12,3,4,9);
        room(air,5,10,3,10,12);
        room(air,11,11,3,11,12);
        // Do not excavate inside the retained slab core before putting it back.
        if(!slab) well(air,7,8,6,11,6,11);
        if(slab) well(air,7,8,9,11,6,11);
        if(!slab) well(air,5,6,6,7,6,11);
        if(!slab) well(air,9,10,8,9,12,13);
        if(slab) well(air,9,10,9,9,6,10);
        room(air,6,7,15,1,4);
        room(air,6,11,15,4,5);
        room(air,6,8,15,6,7);
        room(air,6,7,15,8,12);
        room(air,3,7,15,11,12);
        room(air,11,12,15,4,7);
        room(air,5,12,9,4,5);
        room(air,11,12,9,4,9);
        room(air,5,11,9,11,12);
        room(air,3,6,9,10,11);
        if(!slab) room(air,7,8,9,4,11);
        else well(air,11,12,6,9,3,11);
        well(air,11,12,6,7,3,17);
        well(air,5,6,11,12,3,17);
        if(hollow) well(air,5,6,6,7,2,5);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,3,4,2,4,11);
        floor(blocks,base,5,12,2,4,9);
        floor(blocks,base,5,10,2,10,12);
        floor(blocks,base,11,11,2,11,12);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,6,11,14,4,5);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,6,7,14,8,12);
        floor(blocks,base,3,7,14,11,12);
        floor(blocks,base,11,12,14,4,7);
        floor(blocks,base,5,12,8,4,5);
        floor(blocks,base,11,12,8,4,9);
        floor(blocks,base,5,11,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        if(!slab) floor(blocks,base,7,8,8,4,11);
        if(!hollow) for(int y=2;y<=8;y++) floor(blocks,base,5,6,y,6,7);
        if(!slab) for(int y=2;y<=11;y++) floor(blocks,base,9,10,y,8,9);
        for(int y=6;y<=8;y++) floor(blocks,base,7,8,y,10,12);
        for(int y=2;y<=8;y++) floor(blocks,base,7,8,y,13,13);
        if(slab) for(int x=3;x<=12;x++) {
            int roof=x<=4?6:x<=6?8:x<=8?10:x<=10?11:12;
            for(int y=roof;y<=roof+2;y++) floor(blocks,base,x,x,y,6,9);
            if(x>=5 && x<=10) for(int y=2;y<roof;y++) floor(blocks,base,x,x,y,6,8);
        }
        // A two-wide lower elbow passes under the east shoulder, outside the N4 mask.
        if(slab) for(int x=8;x<=10;x++) for(int y=3;y<=5;y++)
            blocks.put(base.offset(x,y,8),Blocks.AIR.defaultBlockState());
        if(hollow) {
            floor(blocks,base,5,6,1,6,7);
            for(int y=2;y<=3;y++) for(int x=5;x<=6;x++) for(int z=6;z<=7;z++)
                blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
            for(int x=5;x<=6;x++) {
                blocks.put(base.offset(x,2,7),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
            }
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
