package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** GOV 60d1c94, BP-05 A/C, geometry version 1. No quality or reward random source. */
final class CatalogueTerraces {
    private CatalogueTerraces() { }
    static final BlockPos ENTRANCE = new BlockPos(7,15,1);
    static final List<BlockPos> HEARTS = List.of(new BlockPos(3, 3, 3), new BlockPos(11, 9, 11),
            new BlockPos(3, 15, 11), new BlockPos(11, 3, 11), new BlockPos(11, 15, 3),
            new BlockPos(7, 9, 3), new BlockPos(3, 9, 9));
    static final List<BlockPos> ORE = List.of(new BlockPos(-1,-1,0), new BlockPos(0,-1,0),
            new BlockPos(1,-1,0), new BlockPos(0,0,-1), new BlockPos(-1,0,-1),
            new BlockPos(1,0,-1), new BlockPos(0,-1,-1));
    static final List<BlockPos> AIR = List.of(new BlockPos(-1,0,0), new BlockPos(1,0,0),
            new BlockPos(0,1,0), new BlockPos(0,0,1));

    static BlockPos base(BlockPos center) { return center.offset(-7, -15, -7); }

    static Map<BlockPos, BlockState> geometry(BlockPos center, boolean split) {
        var air = new LinkedHashSet<BlockPos>();
        for (int y : new int[]{3, 9, 15}) {
            for (int x = y == 9 ? 3 : 2; x <= (y == 9 ? 12 : 11); x++) {
                for (int z = 2; z <= 12; z++) {
                    // Different, offset strata; not three identical rectangular rooms.
                    if (y == 15 && x <= 5 && z <= 7 || y == 3 && x >= 6 && z <= 6
                            || y == 9 && x >= 9 && z <= 5) continue;
                    if (split && y == 9 && z >= 6 && z <= 8 && (x < 6 || x > 8)) continue;
                    for (int h = 0; h < 3; h++) air.add(new BlockPos(x,y+h,z));
                }
            }
        }
        // Catalogue entrance E, with a two-wide approach into the upper terrace.
        for (int x = 6; x <= 7; x++) for (int y = 15; y <= 17; y++)
            air.add(new BlockPos(x,y,1));
        // Two-block-wide, backed ladder routes, offset east and west.
        for (int y = 9; y <= 17; y++) for (int x = 11; x <= 12; x++)
            for (int z = 6; z <= 7; z++) air.add(new BlockPos(x,y,z));
        for (int y = 3; y <= 11; y++) for (int x = 2; x <= 3; x++)
            for (int z = 6; z <= 7; z++) air.add(new BlockPos(x,y,z));
        if (split) {
            // C: the middle bridge is the only horizontal link between the two balconies.
            // Ladder landings join the north/south balconies without adding a second bridge.
            for (int y = 9; y <= 11; y++) for (int x = 3; x <= 5; x++)
                air.add(new BlockPos(x,y,5));
            for (int y = 9; y <= 11; y++) for (int x = 9; x <= 12; x++)
                air.add(new BlockPos(x,y,8));
        }
        // Grow the shell around the final growth clearances as well as the main rooms.
        for (BlockPos heart : HEARTS) {
            air.remove(heart);
            for (BlockPos offset : ORE) air.remove(heart.offset(offset));
            for (BlockPos offset : AIR) air.add(heart.offset(offset));
        }
        var blocks = new LinkedHashMap<BlockPos, BlockState>();
        BlockPos base = base(center);
        // Write a closed one-block shell, not the entire bounding box or unbounded terrain.
        for (BlockPos p : air) for (Direction d : Direction.values()) {
            BlockPos neighbor = p.relative(d);
            if (!air.contains(neighbor)) blocks.put(base.offset(neighbor), Blocks.STONE.defaultBlockState());
        }
        for (BlockPos p : air) blocks.put(base.offset(p), Blocks.AIR.defaultBlockState());
        for (int y = 9; y <= 15; y++) for (int z = 6; z <= 7; z++)
            blocks.put(base.offset(12,y,z), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
        for (int y = 3; y <= 9; y++) for (int z = 6; z <= 7; z++)
            blocks.put(base.offset(2,y,z), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
        for (int z = 6; z <= 7; z++) {
            blocks.put(base.offset(11,14,z), Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(3,8,z), Blocks.STONE.defaultBlockState());
        }
        // Reserve all seven alcoves even in DRY; quality must not choose the macroform.
        for (BlockPos heart : HEARTS) {
            blocks.put(base.offset(heart), Blocks.STONE.defaultBlockState());
            for (BlockPos offset : ORE) blocks.put(base.offset(heart).offset(offset), Blocks.STONE.defaultBlockState());
            for (BlockPos offset : AIR) blocks.put(base.offset(heart).offset(offset), Blocks.AIR.defaultBlockState());
        }
        blocks.put(center.below(), Blocks.CALCITE.defaultBlockState());
        return blocks;
    }

    static Map<BlockPos, BlockState> nodes(BlockPos center, BlockState ore,
                     BlockState heart, int total, int nodes) {
        int peripheral = switch (nodes) {
            case 3 -> 4;
            case 4 -> 5;
            case 5 -> 6;
            case 7 -> 7;
            default -> throw new IllegalArgumentException("Catalogue nodes require 3/4/5/7 hearts");
        };
        if (total != nodes * (peripheral + 1))
            throw new IllegalArgumentException("Catalogue nodes require exact canonical peripheral budgets");
        var blocks = new LinkedHashMap<BlockPos, BlockState>();
        for (BlockPos local : HEARTS.subList(0, nodes)) {
            BlockPos pos = base(center).offset(local);
            blocks.put(pos, heart);
            for (BlockPos offset : ORE.subList(0, peripheral)) blocks.put(pos.offset(offset), ore);
            for (BlockPos offset : AIR) blocks.put(pos.offset(offset), Blocks.AIR.defaultBlockState());
        }
        return blocks;
    }
}
