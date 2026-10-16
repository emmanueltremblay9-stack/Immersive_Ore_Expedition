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

/** GOV 60d1c94, BP-02/03/05/06/10/11/14/16/17/20/23/24/26/27/31/34/35/36/37 A/B/C, geometry version 21. No quality or reward random source. */
final class CatalogueTerraces {
    private CatalogueTerraces() { }
    enum Variant {
        ALTERNATING, FAN, SPLIT, CROSSING, CROSSING_WINDOW, CROSSING_LOOP,
        SIPHON, SIPHON_SPLIT, SIPHON_SHOULDER, RAILS, RAILS_SLOPES, RAILS_FORK, PEARLS, PEARLS_SPLIT, PEARLS_SATELLITE, EIGHT, EIGHT_RETURN, EIGHT_SLIT, BAYONET, BAYONET_SPLIT, BAYONET_SHOULDER, ROOTS, ROOTS_STAGED, ROOTS_JOINED, CELLS, CELLS_SPINE, CELLS_DIAGONAL, COMB, COMB_WINDOW, COMB_DIVIDED, TEETH, TEETH_WINDOW, TEETH_NOTCH, CHIMNEY, CHIMNEY_BALCONY, CHIMNEY_REAR, AXIAL, AXIAL_RETURN, AXIAL_BENT, MUSHROOM, MUSHROOM_SLIT, MUSHROOM_LOBES, SLAB, SLAB_THIRD, SLAB_JOINED, HOURGLASS, HOURGLASS_SPLIT, HOURGLASS_BALCONY, BELL, BELL_FORK, BELL_WINDOW, BIPYRAMID, BIPYRAMID_TRUNCATED, BIPYRAMID_DIVIDED, ARCH, ARCH_WINDOW, ARCH_FORK;
        boolean isArch() { return this == ARCH || this == ARCH_WINDOW || this == ARCH_FORK; }
        boolean isBipyramid() { return this == BIPYRAMID || this == BIPYRAMID_TRUNCATED || this == BIPYRAMID_DIVIDED; }
        boolean isBell() { return this == BELL || this == BELL_FORK || this == BELL_WINDOW; }
        boolean isHourglass() { return this == HOURGLASS || this == HOURGLASS_SPLIT || this == HOURGLASS_BALCONY; }
        boolean isSlab() { return this == SLAB || this == SLAB_THIRD || this == SLAB_JOINED; }
        boolean isMushroom() { return this == MUSHROOM || this == MUSHROOM_SLIT || this == MUSHROOM_LOBES; }
        boolean isAxial() { return this == AXIAL || this == AXIAL_RETURN || this == AXIAL_BENT; }
        boolean isChimney() { return this == CHIMNEY || this == CHIMNEY_BALCONY || this == CHIMNEY_REAR; }
        boolean isTeeth() { return this == TEETH || this == TEETH_WINDOW || this == TEETH_NOTCH; }
        boolean isComb() { return this == COMB || this == COMB_WINDOW || this == COMB_DIVIDED; }
        boolean isCells() { return this == CELLS || this == CELLS_SPINE || this == CELLS_DIAGONAL; }
        boolean isRoots() { return this == ROOTS || this == ROOTS_STAGED || this == ROOTS_JOINED; }
        boolean isBayonet() { return this == BAYONET || this == BAYONET_SPLIT || this == BAYONET_SHOULDER; }
        boolean isCrossing() { return this == CROSSING || this == CROSSING_WINDOW || this == CROSSING_LOOP; }
        boolean isEight() { return this == EIGHT || this == EIGHT_RETURN || this == EIGHT_SLIT; }
        boolean isPearls() { return this == PEARLS || this == PEARLS_SPLIT || this == PEARLS_SATELLITE; }
        boolean isRails() { return this == RAILS || this == RAILS_SLOPES || this == RAILS_FORK; }
        boolean isSiphon() { return this == SIPHON || this == SIPHON_SPLIT || this == SIPHON_SHOULDER; }
    }
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

    static Map<BlockPos, BlockState> geometry(BlockPos center, Variant variant) {
        if (variant.isCrossing()) return CatalogueCrossing.geometry(center,variant);
        if (variant.isSiphon()) return CatalogueSiphon.geometry(center,variant);
        if (variant.isRails()) return CatalogueRails.geometry(center,variant);
        if (variant.isPearls()) return CataloguePearls.geometry(center,variant);
        if (variant.isEight()) return CatalogueEight.geometry(center,variant);
        if (variant.isBayonet()) return CatalogueBayonet.geometry(center,variant);
        if (variant.isRoots()) return CatalogueRoots.geometry(center,variant);
        if (variant.isCells()) return CatalogueCells.geometry(center,variant);
        if (variant.isComb()) return CatalogueComb.geometry(center,variant);
        if (variant.isTeeth()) return CatalogueTeeth.geometry(center,variant);
        if (variant.isChimney()) return CatalogueChimney.geometry(center,variant);
        if (variant.isAxial()) return CatalogueAxial.geometry(center,variant);
        if (variant.isMushroom()) return CatalogueMushroom.geometry(center,variant);
        if (variant.isSlab()) return CatalogueSlab.geometry(center,variant);
        if (variant.isHourglass()) return CatalogueHourglass.geometry(center,variant);
        if (variant.isBell()) return CatalogueBell.geometry(center,variant);
        if (variant.isBipyramid()) return CatalogueBipyramid.geometry(center,variant);
        if (variant.isArch()) return CatalogueArch.geometry(center,variant);
        boolean split = variant == Variant.SPLIT;
        var air = new LinkedHashSet<BlockPos>();
        for (int y : new int[]{3, 9, 15}) {
            for (int x = y == 9 ? 3 : 2; x <= (y == 9 ? 12 : 11); x++) {
                for (int z = 2; z <= 12; z++) {
                    // Different, offset strata; not three identical rectangular rooms.
                    if (y == 15 && x <= 5 && z <= 7 || y == 3 && x >= 6 && z <= 6
                            || y == 9 && x >= 9 && z <= 5) continue;
                    // Trim unused corners, retaining every node, landing and three-high route.
                    if (y == 15 && x >= 9 && z >= 9 || y == 3 && x <= 5 && z >= 9
                            || y == 9 && x <= 5 && z <= 4) continue;
                    // B: taper the three different ledges toward their common vertical opening.
                    if (variant == Variant.FAN && (y == 15 && x >= 10 && z == 8
                            || y == 9 && x == 12 && z == 12 || y == 3 && x == 6 && z >= 10)) continue;
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
            // C: open voids flank a supported bridge, rather than solid corridor walls.
            for (int y = 6; y <= 11; y++) for (int x : new int[]{4,5,9,10})
                for (int z = 6; z <= 8; z++) air.add(new BlockPos(x,y,z));
            for (int y = 6; y <= 7; y++) for (int x = 6; x <= 8; x++)
                for (int z = 6; z <= 8; z++) air.add(new BlockPos(x,y,z));
            // Ladder landings join the north/south balconies without adding a second bridge.
            for (int y = 9; y <= 11; y++) for (int x = 3; x <= 5; x++)
                air.add(new BlockPos(x,y,5));
            for (int y = 9; y <= 11; y++) for (int x = 9; x <= 12; x++)
                air.add(new BlockPos(x,y,8));
        }
        if (variant == Variant.FAN) {
            // One shared void joins all three fan ledges; the offset backed routes stay outside it.
            for (int y = 3; y <= 17; y++) for (int x = 8; x <= 9; x++)
                for (int z = 8; z <= 9; z++) air.add(new BlockPos(x,y,z));
        }
        return finish(center, air);
    }

    /** Shared canonical reservations, shell, supported links and cache marker. */
    static Map<BlockPos, BlockState> finish(BlockPos center, LinkedHashSet<BlockPos> air) {
        return finish(center,air,true);
    }

    static Map<BlockPos, BlockState> finish(BlockPos center, LinkedHashSet<BlockPos> air, boolean standardLinks) {
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
        if (standardLinks) {
        for (int y = 9; y <= 15; y++) for (int z = 6; z <= 7; z++)
            blocks.put(base.offset(12,y,z), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
        for (int y = 3; y <= 9; y++) for (int z = 6; z <= 7; z++)
            blocks.put(base.offset(2,y,z), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
        for (int z = 6; z <= 7; z++) {
            blocks.put(base.offset(11,14,z), Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(3,8,z), Blocks.STONE.defaultBlockState());
        }
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

    /** Raccord above the chamber roof, then a backed descent through E. */
    static Map<BlockPos, BlockState> access(BlockPos center, BlockPos shaftEnd,
                                           Map<BlockPos, BlockState> chamber) {
        if (shaftEnd.getY() != center.getY() + 4)
            throw new IllegalArgumentException("The catalogue shaft must end above the chamber roof");
        var air = new LinkedHashSet<BlockPos>();
        int y = shaftEnd.getY();
        BlockPos entry = base(center).offset(ENTRANCE);
        for (int x = Math.min(shaftEnd.getX(), entry.getX()); x <= Math.max(shaftEnd.getX(), entry.getX()); x++)
            for (int z = shaftEnd.getZ(); z <= shaftEnd.getZ()+1; z++)
                for (int h = 0; h < 3; h++) air.add(new BlockPos(x,y+h,z));
        for (int z = entry.getZ(); z <= shaftEnd.getZ()+1; z++)
            for (int x = entry.getX()-1; x <= entry.getX(); x++)
                for (int h = 0; h < 3; h++) air.add(new BlockPos(x,y+h,z));
        for (int dy = entry.getY(); dy <= y+2; dy++)
            for (int x = entry.getX()-1; x <= entry.getX(); x++) air.add(new BlockPos(x,dy,entry.getZ()));
        var blocks = new LinkedHashMap<BlockPos, BlockState>();
        for (BlockPos p : air) for (Direction direction : Direction.values()) {
            BlockPos wall = p.relative(direction);
            // Do not wall off the doorway where E meets the upper terrace.
            if (!air.contains(wall) && !chamber.getOrDefault(wall, Blocks.STONE.defaultBlockState()).isAir())
                blocks.put(wall, Blocks.STONE.defaultBlockState());
        }
        for (BlockPos p : air) blocks.put(p, Blocks.AIR.defaultBlockState());
        for (int dy = entry.getY(); dy <= y; dy++) for (int x = entry.getX()-1; x <= entry.getX(); x++) {
            BlockPos ladder = new BlockPos(x,dy,entry.getZ());
            blocks.put(ladder.north(), Blocks.STONE.defaultBlockState());
            blocks.put(ladder, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
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
