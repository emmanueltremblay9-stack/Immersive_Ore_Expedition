package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-23: two offset vertical loops around distinct cores, joined at a supported central landing. */
final class CatalogueEight {
    private CatalogueEight() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean returning = variant == CatalogueTerraces.Variant.EIGHT_RETURN;
        boolean slit = variant == CatalogueTerraces.Variant.EIGHT_SLIT;
        var air = new LinkedHashSet<BlockPos>();
        // Upper loop extends south; the entry branches from its western arm.
        room(air,6,7,15,1,7);
        room(air,3,6,15,6,7);
        room(air,5,6,15,6,12);
        room(air,5,12,15,9,10);
        room(air,11,12,15,4,10);
        room(air,3,6,15,11,12);
        // Four arms meet in J; its floor stays closed, without a central fall shaft.
        room(air,6,7,9,6,9);
        room(air,3,7,9,6,7);
        room(air,6,11,9,4,5);
        room(air,6,7,9,4,7);
        room(air,3,7,9,10,11);
        room(air,6,7,9,8,11);
        room(air,7,12,9,8,9);
        // N2's fixed back mask separates this terminal branch from the western arm.
        room(air,9,10,9,9,12);
        room(air,9,11,9,11,12);
        // Lower loop advances north, around its own rock core.
        room(air,3,4,3,4,11);
        room(air,3,12,3,4,5);
        room(air,11,12,3,4,9);
        room(air,8,9,3,8,12);
        room(air,8,12,3,8,9);
        room(air,8,11,3,11,12);
        well(air,2,3,6,7,9,17);
        well(air,11,12,4,5,9,17);
        well(air,2,3,10,11,3,11);
        well(air,11,12,8,9,3,11);
        if (returning) well(air,2,3,4,5,3,11);
        if (slit) room(air,3,12,3,7,8);
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        // Explicit cores cannot disappear when the source terrain contains a cave.
        for(int x=6;x<=8;x++) for(int y=12;y<=13;y++) for(int z=8;z<=9;z++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        for(int x=6;x<=8;x++) for(int y=3;y<=6;y++) for(int z=6;z<=8;z++) {
            if (x>=8 && z>=8 || slit && y<=5 && z>=7 && z<=8) continue;
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        }
        link(blocks,base,2,3,6,7,9,15,14,Direction.EAST);
        link(blocks,base,12,11,4,5,9,15,14,Direction.WEST);
        link(blocks,base,2,3,10,11,3,9,8,Direction.EAST);
        link(blocks,base,12,11,8,9,3,9,8,Direction.WEST);
        if (returning) link(blocks,base,2,3,4,5,3,9,8,Direction.EAST);
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++)
            air.add(new BlockPos(x,y,z));
    }
    private static void link(Map<BlockPos,BlockState> blocks,BlockPos base,int ladderX,int landingX,
                             int z1,int z2,int y1,int y2,int floorY,Direction facing) {
        for(int z=z1;z<=z2;z++) {
            for(int y=y1;y<=y2;y++)
                blocks.put(base.offset(ladderX,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
            blocks.put(base.offset(landingX,floorY,z),Blocks.STONE.defaultBlockState());
        }
    }
}
