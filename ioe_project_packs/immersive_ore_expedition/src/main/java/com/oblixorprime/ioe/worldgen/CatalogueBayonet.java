package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-02: a narrow descending fracture with alternating elbows and supported transitions. */
final class CatalogueBayonet {
    private CatalogueBayonet() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean split = variant == CatalogueTerraces.Variant.BAYONET_SPLIT;
        boolean shoulder = variant == CatalogueTerraces.Variant.BAYONET_SHOULDER;
        var air = new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,5);
        room(air,6,12,15,4,5);
        room(air,11,12,15,4,8);
        room(air,5,12,15,7,8);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        // Reverse direction across the middle landing; terminal alcoves stay separate.
        room(air,3,12,9,6,7);
        room(air,6,7,9,4,7);
        room(air,5,6,9,7,10);
        room(air,3,6,9,10,11);
        room(air,8,9,9,7,12);
        room(air,9,12,9,11,12);
        room(air,3,10,3,6,7);
        room(air,3,4,3,4,7);
        room(air,8,9,3,7,12);
        room(air,9,12,3,11,12);
        well(air,11,12,6,7,9,17);
        well(air,2,3,6,7,3,11);
        if (split) {
            well(air,11,12,8,9,3,11);
            room(air,8,12,9,8,9);
            room(air,8,12,3,8,9);
        }
        if (shoulder) {
            room(air,9,12,15,6,9);
            room(air,11,12,15,9,12);
        }
        var blocks = CatalogueTerraces.finish(center,air);
        if (split) {
            var base = CatalogueTerraces.base(center);
            for(int z=8;z<=9;z++) {
                for(int y=3;y<=9;y++) blocks.put(base.offset(12,y,z),
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
                blocks.put(base.offset(11,8,z),Blocks.STONE.defaultBlockState());
            }
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
