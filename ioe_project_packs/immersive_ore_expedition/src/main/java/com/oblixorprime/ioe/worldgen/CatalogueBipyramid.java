package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-20: broad faceted belt between displaced narrow tips, not an hourglass. */
final class CatalogueBipyramid {
    private CatalogueBipyramid() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean truncated=variant==CatalogueTerraces.Variant.BIPYRAMID_TRUNCATED;
        boolean divided=variant==CatalogueTerraces.Variant.BIPYRAMID_DIVIDED;
        var air=new LinkedHashSet<BlockPos>();
        facets(air,5,8,7,10,3,5);
        facets(air,5,9,6,10,6,8);
        facets(air,4,11,4,11,9,11);
        facets(air,5,10,4,9,12,14);
        facets(air,7,10,4,7,15,17);
        room(air,3,6,3,4,7);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,3,12,9,4,5);
        room(air,6,7,9,4,9);
        room(air,6,11,9,8,9);
        room(air,3,9,9,10,11);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,6,7,15,1,7);
        room(air,5,12,15,4,5);
        room(air,7,11,15,6,7);
        room(air,5,6,15,6,12);
        room(air,3,5,15,11,12);
        well(air,11,12,6,7,9,17);
        if(truncated) {
            room(air,7,11,15,4,9);
            well(air,5,6,6,7,9,17);
        }
        if(divided) {
            well(air,5,6,6,7,3,11);
            well(air,8,10,8,9,3,11);
        } else well(air,2,3,6,7,3,11);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,3,12,8,4,5);
        floor(blocks,base,6,7,8,4,9);
        floor(blocks,base,6,11,8,8,9);
        floor(blocks,base,3,9,8,10,11);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,6,7,14,1,7);
        floor(blocks,base,5,12,14,4,5);
        floor(blocks,base,7,11,14,6,7);
        floor(blocks,base,5,6,14,6,12);
        floor(blocks,base,3,5,14,11,12);
        if(truncated) floor(blocks,base,7,11,14,4,9);
        ladder(blocks,base,12,13,11,6,7,9,15,Direction.WEST);
        if(truncated) ladder(blocks,base,5,4,6,6,7,9,15,Direction.EAST);
        if(divided) {
            for(int y=6;y<=8;y++) floor(blocks,base,7,7,y,6,9);
            ladder(blocks,base,5,4,6,6,7,3,9,Direction.EAST);
            ladder(blocks,base,10,11,9,8,9,3,9,Direction.WEST);
        } else ladder(blocks,base,2,1,3,6,7,3,9,Direction.EAST);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int backing,int landing,int z1,int z2,int y1,int y2,Direction facing) {
        for(int z=z1;z<=z2;z++) {
            for(int y=y1;y<=y2;y++) {
                blocks.put(base.offset(backing,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
            }
            for(int y:new int[]{8,14}) if(y>=y1 && y<=y2) blocks.put(base.offset(landing,y,z),Blocks.STONE.defaultBlockState());
        }
    }
    private static void facets(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) {
            if((x==x1 || x==x2) && (z==z1 || z==z2)) continue;
            for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
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
