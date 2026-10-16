package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-31: one hollow vestibule joining four unequal side arms and two supported vertical arms. */
final class CatalogueAxial {
    private CatalogueAxial() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean diagonal = variant == CatalogueTerraces.Variant.AXIAL_RETURN;
        boolean bent = variant == CatalogueTerraces.Variant.AXIAL_BENT;
        var air = new LinkedHashSet<BlockPos>();
        well(air,5,12,4,9,9,13);
        room(air,10,11,9,1,4);
        room(air,10,13,9,6,7);
        room(air,2,5,9,4,5);
        room(air,5,6,9,9,12);
        room(air,3,6,9,10,11);
        room(air,11,12,9,6,9);
        room(air,8,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        well(air,4,5,6,7,3,11);
        room(air,4,5,3,4,12);
        room(air,3,5,3,4,5);
        room(air,4,12,3,11,12);
        room(air,6,7,15,1,4);
        room(air,6,10,15,3,4);
        room(air,10,12,15,4,5);
        room(air,11,12,15,4,9);
        room(air,5,12,15,7,8);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        if (bent) well(air,11,12,8,9,9,17);
        else well(air,8,9,6,7,9,17);
        if (diagonal) {
            room(air,10,13,9,1,2);
            room(air,12,13,9,2,6);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        for(int z=6;z<=7;z++) {
            for(int y=3;y<=9;y++) {
                blocks.put(base.offset(6,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            blocks.put(base.offset(4,8,z),Blocks.STONE.defaultBlockState());
        }
        if (bent) {
            for(int z=8;z<=9;z++) {
                for(int y=9;y<=15;y++) blocks.put(base.offset(12,y,z),
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
                blocks.put(base.offset(11,14,z),Blocks.STONE.defaultBlockState());
                // A supported observation landing and rock sill prevent a direct fall into the hub.
                blocks.put(base.offset(11,10,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(10,10,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(10,11,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(10,12,z),Blocks.AIR.defaultBlockState());
                blocks.put(base.offset(10,13,z),Blocks.AIR.defaultBlockState());
            }
        } else for(int x=8;x<=9;x++) {
            for(int y=9;y<=15;y++) {
                blocks.put(base.offset(x,y,5),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,6),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
            }
            blocks.put(base.offset(x,14,7),Blocks.STONE.defaultBlockState());
        }
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
}
