package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-35: a continuous chimney beside a protected spine and terminal wall lodges. */
final class CatalogueChimney {
    private CatalogueChimney() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean balcony = variant == CatalogueTerraces.Variant.CHIMNEY_BALCONY;
        boolean rear = variant == CatalogueTerraces.Variant.CHIMNEY_REAR;
        var air = new LinkedHashSet<BlockPos>();
        well(air,7,10,6,9,3,17);
        well(air,4,5,6,7,3,17);
        room(air,4,5,3,4,12);
        room(air,3,5,3,4,5);
        room(air,4,12,3,11,12);
        room(air,5,7,3,8,9);
        room(air,4,5,9,4,7);
        room(air,5,8,9,4,5);
        room(air,5,6,9,8,10);
        room(air,3,6,9,10,11);
        room(air,5,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,6,7,15,1,4);
        room(air,4,5,15,4,7);
        room(air,4,12,15,4,5);
        room(air,7,8,15,4,7);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        if (balcony) room(air,5,10,9,11,12);
        if (rear) {
            room(air,5,13,15,11,12);
            well(air,12,13,11,12,9,17);
            room(air,9,13,9,11,12);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        for(int z=6;z<=7;z++) {
            for(int y=3;y<=15;y++) {
                blocks.put(base.offset(3,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(4,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            for(int foot:new int[]{3,9,15}) {
                blocks.put(base.offset(5,foot-1,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(6,foot-1,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(6,foot,z),Blocks.STONE.defaultBlockState());
                for(int y=foot+1;y<=foot+2;y++) blocks.put(base.offset(6,y,z),Blocks.AIR.defaultBlockState());
            }
        }
        floor(blocks,base,5,12,8,8,9);
        floor(blocks,base,7,8,14,4,7);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        if (rear) for(int z=11;z<=12;z++) {
            for(int y=9;y<=15;y++) blocks.put(base.offset(13,y,z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            blocks.put(base.offset(12,14,z),Blocks.STONE.defaultBlockState());
        }
        return blocks;
    }

    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
}
