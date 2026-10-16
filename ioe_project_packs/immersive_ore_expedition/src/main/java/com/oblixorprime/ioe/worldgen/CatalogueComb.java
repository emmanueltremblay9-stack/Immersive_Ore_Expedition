package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-14: three parallel grooves with unequal floors/roofs and an offset, stepped collector. */
final class CatalogueComb {
    private CatalogueComb() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean window = variant == CatalogueTerraces.Variant.COMB_WINDOW;
        boolean divided = variant == CatalogueTerraces.Variant.COMB_DIVIDED;
        var air = new LinkedHashSet<BlockPos>();
        well(air,3,4,6,9,3,10);
        well(air,3,4,10,12,3,17);
        well(air,1,2,11,12,3,17);
        room(air,3,4,3,4,7);
        room(air,6,7,15,1,7);
        room(air,5,6,15,6,12);
        room(air,2,6,15,11,12);
        well(air,6,7,4,10,5,11);
        well(air,12,13,4,7,10,17);
        well(air,12,13,8,10,10,17);
        well(air,12,13,11,12,3,17);
        room(air,11,13,3,11,12);
        room(air,11,13,9,11,12);
        room(air,11,13,15,4,5);
        // The collector crosses the left lamina to the south and the right one to the north.
        room(air,3,6,9,10,11);
        room(air,5,6,9,8,11);
        room(air,9,13,10,4,5);
        well(air,8,8,4,5,10,12);
        if (window) {
            room(air,3,6,3,9,10);
            well(air,5,5,9,10,4,6);
        }
        if (divided) {
            room(air,8,9,5,4,9);
            room(air,6,9,5,4,5);
            room(air,6,9,5,8,9);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        floor(blocks,base,2,4,8,10,12);
        floor(blocks,base,2,4,14,10,12);
        floor(blocks,base,6,7,8,4,10);
        floor(blocks,base,11,12,8,11,12);
        floor(blocks,base,12,13,14,4,12);
        floor(blocks,base,11,11,14,4,5);
        floor(blocks,base,12,13,9,4,10);
        for(int z=4;z<=5;z++) {
            blocks.put(base.offset(8,8,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(8,9,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        }
        link(blocks,base,1,2,11,12,3,15,8,Direction.EAST);
        floor(blocks,base,2,2,14,11,12);
        link(blocks,base,13,12,11,12,3,15,8,Direction.WEST);
        floor(blocks,base,12,12,14,11,12);
        if (divided) {
            for(int x=6;x<=7;x++) for(int y=6;y<=8;y++) for(int z=6;z<=7;z++)
                blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
            // finish() already supplies the continuous three-high outer wall of the side detour.
        }
        // The common north descent leaves C's bottom interruption meaningful without a second shaft.
        link(blocks,base,6,7,4,5,5,9,8,Direction.EAST);
        if (window) for(int z=9;z<=10;z++) {
            blocks.put(base.offset(5,2,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(5,3,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            blocks.put(base.offset(6,3,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(6,4,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        }
        return blocks;
    }

    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void link(Map<BlockPos,BlockState> blocks,BlockPos base,int ladderX,int landingX,
                             int z1,int z2,int y1,int y2,int floorY,Direction facing) {
        for(int z=z1;z<=z2;z++) {
            for(int y=y1;y<=y2;y++) blocks.put(base.offset(ladderX,y,z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
            blocks.put(base.offset(landingX,floorY,z),Blocks.STONE.defaultBlockState());
        }
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
}
