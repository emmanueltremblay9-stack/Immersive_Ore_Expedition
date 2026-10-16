package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** GOV BP-16 A topology, fitted to the existing envelope and canonical node coordinates. */
final class CatalogueCrossing {
    private CatalogueCrossing() { }

    static Map<BlockPos, BlockState> geometry(BlockPos center) {
        var air = new LinkedHashSet<BlockPos>();
        // Upper east-west gallery, with separate ends serving the unchanged upper alcoves.
        room(air,2,12,15,6,8);
        room(air,2,6,15,8,12);
        room(air,10,12,15,2,6);
        room(air,6,7,15,1,6);
        // Lower north-south gallery and its two resource alcoves.
        room(air,6,8,3,2,12);
        room(air,2,6,3,2,4);
        room(air,8,12,3,10,12);
        room(air,3,6,3,6,7);
        // One lateral route between the axes, folded around the solid central diaphragm.
        room(air,9,12,9,6,12);
        room(air,10,11,9,4,7);
        room(air,3,11,9,4,5);
        room(air,3,5,9,4,7);
        room(air,8,12,9,9,12);
        room(air,5,10,9,10,12);
        room(air,2,5,9,9,12);
        // Existing two-wide backed links; no hole or mandatory fall through the crossing.
        for (int y = 9; y <= 17; y++) for (int x = 11; x <= 12; x++)
            for (int z = 6; z <= 7; z++) air.add(new BlockPos(x,y,z));
        for (int y = 3; y <= 11; y++) for (int x = 2; x <= 3; x++)
            for (int z = 6; z <= 7; z++) air.add(new BlockPos(x,y,z));
        var blocks = CatalogueTerraces.finish(center,air);
        var base = CatalogueTerraces.base(center);
        // Explicit rock separates both axes, including when the source terrain contains a void.
        for (int y = 8; y <= 9; y++) for (int x = 6; x <= 8; x++)
            for (int z = 6; z <= 8; z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air, int minX, int maxX, int feet, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            for (int y = feet; y < feet+3; y++) air.add(new BlockPos(x,y,z));
    }
}
