package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-27 A/B/C topology, normalized to the existing envelope and seven canonical node masks. */
final class CatalogueSiphon {
    private CatalogueSiphon() { }

    static Map<BlockPos, BlockState> geometry(BlockPos center, CatalogueTerraces.Variant variant) {
        boolean split = variant == CatalogueTerraces.Variant.SIPHON_SPLIT;
        boolean shoulder = variant == CatalogueTerraces.Variant.SIPHON_SHOULDER;
        var air = new LinkedHashSet<BlockPos>();
        // Entry side: the existing entrance, marker and upper-west alcove remain on this bank.
        room(air,6,7,15,1,7);
        room(air,2,7,15,6,7);
        room(air,5,6,15,8,12);
        room(air,2,6,15,11,12);
        // Opposite upper end: never join it directly to the entry-side gallery.
        room(air,10,12,15,3,shoulder ? 8 : 6);
        // Separate middle alcoves and backed landings on each bank.
        room(air,3,6,9,6,7);
        room(air,5,6,9,6,11);
        room(air,2,6,9,9,11);
        room(air,10,12,9,4,9);
        room(air,8,12,9,9,12);
        room(air,7,11,9,4,5);
        if (shoulder) room(air,8,12,9,6,9);
        // The only inter-bank passage is the lower elbow; no water or hydraulic mechanism.
        room(air,2,6,3,3,4);
        room(air,8,12,3,11,12);
        if (split) {
            room(air,3,5,3,4,9);
            room(air,5,10,3,4,5);
            room(air,5,10,3,8,9);
            room(air,9,12,3,4,9);
            room(air,8,9,3,8,12);
        } else {
            room(air,5,6,3,3,7);
            room(air,3,10,3,6,7);
            room(air,9,12,3,4,7);
            room(air,8,9,3,6,12);
        }
        well(air,2,3,6,7,3,17);
        well(air,11,12,4,5,3,shoulder ? 11 : 17);
        if (shoulder) well(air,11,12,7,8,9,17);
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        ladder(blocks,base,2,6,7,3,15,Direction.EAST);
        landing(blocks,base,3,6,7,8);
        landing(blocks,base,3,6,7,14);
        ladder(blocks,base,12,4,5,3,shoulder ? 9 : 15,Direction.WEST);
        landing(blocks,base,11,4,5,8);
        if (shoulder) {
            ladder(blocks,base,12,7,8,9,15,Direction.WEST);
            landing(blocks,base,11,7,8,8);
            landing(blocks,base,11,7,8,14);
        } else landing(blocks,base,11,4,5,14);
        if (split) for (int x = 6; x <= 8; x++) for (int z = 6; z <= 7; z++)
            for (int y = 2; y <= 6; y++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air, int minX, int maxX, int feet, int minZ, int maxZ) {
        well(air,minX,maxX,minZ,maxZ,feet,feet+2);
    }
    private static void well(LinkedHashSet<BlockPos> air, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            for (int y = minY; y <= maxY; y++) air.add(new BlockPos(x,y,z));
    }
    private static void ladder(Map<BlockPos,BlockState> blocks, BlockPos base, int x, int minZ, int maxZ,
                               int minY, int maxY, Direction facing) {
        for (int z = minZ; z <= maxZ; z++) for (int y = minY; y <= maxY; y++)
            blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
    }
    private static void landing(Map<BlockPos,BlockState> blocks, BlockPos base, int x, int minZ, int maxZ, int y) {
        for (int z = minZ; z <= maxZ; z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
}
