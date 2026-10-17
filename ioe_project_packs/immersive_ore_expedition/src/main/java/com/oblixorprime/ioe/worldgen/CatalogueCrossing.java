package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** GOV BP-16 A/B/C topology, fitted to the existing envelope and canonical node coordinates. */
final class CatalogueCrossing {
    private CatalogueCrossing() { }

    static Map<BlockPos, BlockState> geometry(BlockPos center, CatalogueTerraces.Variant variant) {
        boolean window = variant == CatalogueTerraces.Variant.CROSSING_WINDOW;
        boolean loop = variant == CatalogueTerraces.Variant.CROSSING_LOOP;
        var air = new LinkedHashSet<BlockPos>();
        // Upper east-west gallery, with separate ends serving the unchanged upper alcoves.
        room(air,2,12,15,6,7);
        room(air,5,6,15,8,12);
        room(air,2,6,15,11,12);
        room(air,10,12,15,2,6);
        room(air,6,7,15,1,6);
        // Lower north-south gallery and its two resource alcoves.
        room(air,6,7,3,2,12);
        room(air,2,6,3,3,4);
        room(air,8,12,3,11,12);
        room(air,3,6,3,window ? 5 : 6,window ? 6 : 7);
        if (loop) room(air,7,11,3,6,7);
        // One lateral route between the axes, folded around the solid central diaphragm.
        room(air,10,12,9,6,12);
        room(air,10,11,9,4,7);
        room(air,3,11,9,4,5);
        room(air,3,4,9,4,7);
        room(air,8,12,9,9,12);
        room(air,5,10,9,10,11);
        room(air,2,5,9,9,11);
        // Existing two-wide backed links; no hole or mandatory fall through the crossing.
        for (int y = loop ? 3 : 9; y <= 17; y++) for (int x = 11; x <= 12; x++)
            for (int z = window ? 7 : 6; z <= (window ? 8 : 7); z++) air.add(new BlockPos(x,y,z));
        for (int y = 3; y <= (loop ? 17 : 11); y++) for (int x = 2; x <= 3; x++)
            for (int z = window ? 5 : 6; z <= (window ? 6 : 7); z++) air.add(new BlockPos(x,y,z));
        if (window) for (int y = 3; y <= 17; y++) air.add(new BlockPos(7,y,8));
        var blocks = CatalogueTerraces.finish(center,air);
        var base = CatalogueTerraces.base(center);
        // Explicit rock separates both axes, including when the source terrain contains a void.
        for (int y = 8; y <= 9; y++) for (int x = 6; x <= 8; x++)
            for (int z = 6; z <= 8; z++) if (!window || x != 7 || z != 8)
                blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        if (window) {
            // Shift both landings, retaining two backed ladder lanes at each level.
            for (int y = 9; y <= 15; y++) {
                blocks.put(base.offset(12,y,6),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(12,y,8),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            for (int y = 3; y <= 9; y++) {
                blocks.put(base.offset(2,y,7),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(2,y,5),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            blocks.put(base.offset(11,14,8),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(3,8,5),Blocks.STONE.defaultBlockState());
            // Existing IOE structural palette: a collision guard beside the supported upper path.
            // Apply after its surroundings so neighbor-derived state cannot change during composition.
            BlockPos guard = base.offset(7,15,8);
            blocks.remove(guard);
            blocks.put(guard,Blocks.STONE_BRICK_WALL.defaultBlockState());
        }
        if (loop) {
            for (int z = 6; z <= 7; z++) {
                blocks.put(base.offset(3,14,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(11,8,z),Blocks.STONE.defaultBlockState());
                for (int y = 3; y <= 8; y++) blocks.put(base.offset(12,y,z),
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
                for (int y = 10; y <= 15; y++) blocks.put(base.offset(2,y,z),
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
        }
        return blocks;
    }

    private static void room(LinkedHashSet<BlockPos> air, int minX, int maxX, int feet, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            for (int y = feet; y < feet+3; y++) air.add(new BlockPos(x,y,z));
    }
}
