package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-10: seven irregular pockets; two loops, a middle spine, or one extra diagonal link. */
final class CatalogueCells {
    private CatalogueCells() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean spine = variant == CatalogueTerraces.Variant.CELLS_SPINE;
        boolean diagonal = variant == CatalogueTerraces.Variant.CELLS_DIAGONAL;
        var air = new LinkedHashSet<BlockPos>();
        room(air,3,6,3,3,5);
        room(air,6,8,9,3,5);
        room(air,3,6,15,10,12);
        room(air,9,13,3,10,12);
        room(air,9,13,9,10,12);
        room(air,2,6,9,9,11);
        room(air,9,13,15,3,5);
        // Left branch: N1 -- N6 -- N3, with two distinct backed necks.
        room(air,3,8,3,4,5);
        well(air,7,8,4,5,3,11);
        room(air,3,8,9,4,5);
        room(air,3,4,9,4,7);
        well(air,2,3,6,7,9,17);
        room(air,3,6,15,6,7);
        room(air,5,6,15,6,12);
        room(air,3,6,15,11,12);
        // The middle spine visits N7. Rock gaps prevent a direct N6 -- N2 shortcut.
        room(air,7,8,9,4,8);
        room(air,5,8,9,8,9);
        room(air,5,6,9,8,10);
        room(air,5,10,9,11,12);
        // Right branch: N4 -- N2 -- N5, outside every reserved mineral mask.
        well(air,12,13,11,12,3,17);
        room(air,12,13,15,4,12);
        room(air,6,7,15,1,7);
        if (!spine) {
            room(air,5,12,15,6,7);
            room(air,7,8,3,4,12);
            room(air,7,13,3,11,12);
        }
        if (diagonal) {
            room(air,5,9,15,11,12);
            well(air,8,9,11,12,9,17);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        link(blocks,base,8,7,4,5,3,9,8,Direction.WEST);
        link(blocks,base,2,3,6,7,9,15,14,Direction.EAST);
        link(blocks,base,13,12,11,12,3,15,8,Direction.WEST);
        for(int z=11;z<=12;z++) blocks.put(base.offset(12,14,z),Blocks.STONE.defaultBlockState());
        if (diagonal) for(int x=8;x<=9;x++) {
            for(int y=9;y<=15;y++) blocks.put(base.offset(x,y,12),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            blocks.put(base.offset(x,14,11),Blocks.STONE.defaultBlockState());
        }
        return blocks;
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
