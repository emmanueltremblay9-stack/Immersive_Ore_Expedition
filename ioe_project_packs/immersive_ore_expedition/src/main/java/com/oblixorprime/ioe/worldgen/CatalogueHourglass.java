package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-11: two offset funnel volumes joined at a narrow shoulder, double neck or visual waist. */
final class CatalogueHourglass {
    private CatalogueHourglass() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean split=variant==CatalogueTerraces.Variant.HOURGLASS_SPLIT;
        boolean balcony=variant==CatalogueTerraces.Variant.HOURGLASS_BALCONY;
        var air=new LinkedHashSet<BlockPos>();
        funnelBand(air,4,11,4,11,3,5);
        well(air,5,9,5,10,6,8);
        well(air,split?5:6,split?10:9,6,9,9,11);
        well(air,5,9,5,10,12,14);
        // The upper basin is offset and narrower than the lower one.
        funnelBand(air,3,9,3,10,15,17);
        room(air,3,6,3,4,5);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,5,8,9,4,5);
        room(air,6,7,9,4,9);
        room(air,5,6,9,8,11);
        room(air,3,6,9,10,11);
        room(air,6,11,9,8,9);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,6,7,15,1,7);
        room(air,6,12,15,4,5);
        room(air,4,5,15,6,12);
        room(air,3,5,15,11,12);
        room(air,4,8,15,8,9);
        if(balcony) {
            well(air,11,12,8,9,3,17);
            room(air,6,10,15,6,9);
        }
        else if(split) {
            well(air,5,6,6,7,3,17);
            well(air,9,10,6,7,3,17);
        } else well(air,8,9,6,7,3,17);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,5,8,8,4,5);
        floor(blocks,base,6,7,8,4,9);
        floor(blocks,base,5,6,8,8,11);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,6,11,8,8,9);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,6,7,14,1,7);
        floor(blocks,base,6,12,14,4,5);
        floor(blocks,base,4,5,14,6,12);
        floor(blocks,base,3,5,14,11,12);
        floor(blocks,base,4,8,14,8,9);
        if(balcony) {
            floor(blocks,base,6,10,14,6,9);
            ladder(blocks,base,12,13,11,8,9);
        } else if(split) {
            for(int y=8;y<=12;y++) for(int z=6;z<=9;z++)
                blocks.put(base.offset(7,y,z),Blocks.STONE.defaultBlockState());
            for(int y=3;y<=15;y++) for(int z=6;z<=7;z++) {
                blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            for(int y:new int[]{8,14}) for(int z=6;z<=7;z++)
                blocks.put(base.offset(6,y,z),Blocks.STONE.defaultBlockState());
            ladder(blocks,base,10,11,9,6,7);
        } else ladder(blocks,base,9,10,8,6,7);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int backing,int landing,int z1,int z2) {
        for(int z=z1;z<=z2;z++) {
            for(int y=3;y<=15;y++) {
                blocks.put(base.offset(backing,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            for(int y:new int[]{8,14}) blocks.put(base.offset(landing,y,z),Blocks.STONE.defaultBlockState());
        }
    }
    private static void funnelBand(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
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
