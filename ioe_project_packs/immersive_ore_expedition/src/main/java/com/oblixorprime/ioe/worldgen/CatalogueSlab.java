package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-36: two shared volumes divided by one rock slab with three distinct openings. */
final class CatalogueSlab {
    private CatalogueSlab() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean third = variant == CatalogueTerraces.Variant.SLAB_THIRD;
        boolean joined = variant == CatalogueTerraces.Variant.SLAB_JOINED;
        var slab = new LinkedHashSet<BlockPos>();
        footprint(slab,5,8,4,9);
        footprint(slab,9,10,4,5);
        footprint(slab,3,4,6,9);
        footprint(slab,4,5,10,12);
        footprint(slab,3,5,11,12);
        footprint(slab,11,13,6,7);
        footprint(slab,12,13,4,5);
        footprint(slab,10,12,4,5);
        footprint(slab,7,8,10,11);
        footprint(slab,9,9,11,11);
        var holes = new LinkedHashSet<BlockPos>();
        footprint(holes,3,4,6,7);
        footprint(holes,11,13,6,7);
        footprint(holes,7,8,10,11);
        footprint(holes,9,9,11,11);
        if(joined) footprint(holes,3,13,6,7);
        var air = new LinkedHashSet<BlockPos>();
        for(var cell:slab) for(int y=15;y<=17;y++) air.add(cell.atY(y));
        for(var cell:holes) for(int y=9;y<=17;y++) air.add(cell.atY(y));
        // Keep the broad lower return; omit the unused north-east shoulder.
        room(air,5,8,3,4,7);
        room(air,5,10,3,8,9);
        well(air,6,8,6,8,6,8);
        well(air,12,13,6,7,3,17);
        room(air,11,12,3,6,9);
        room(air,3,6,3,4,5);
        room(air,9,12,3,11,12);
        room(air,8,9,3,8,12);
        room(air,5,12,9,4,5);
        room(air,3,4,9,4,10);
        room(air,3,6,9,10,11);
        room(air,11,12,9,4,9);
        room(air,5,12,9,8,9);
        room(air,9,12,9,11,12);
        room(air,8,9,9,8,12);
        well(air,7,10,6,7,9,11);
        room(air,6,7,15,1,4);
        room(air,10,12,15,4,5);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        for(var cell:slab) if(!holes.contains(cell)) for(int y=12;y<=14;y++)
            blocks.put(base.offset(cell.atY(y)),Blocks.STONE.defaultBlockState());
        floor(blocks,base,5,12,8,4,5);
        floor(blocks,base,3,4,8,4,10);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,11,12,8,4,9);
        floor(blocks,base,5,12,8,8,9);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,8,9,8,8,12);
        for(int z=6;z<=7;z++) for(int y=3;y<=15;y++) {
            if(y>=9) {
                blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            blocks.put(base.offset(14,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(13,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int z=6;z<=7;z++) for(int y:new int[]{8,14}) {
            blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,y,z),Blocks.STONE.defaultBlockState());
        }
        // The rear L-shaped view has one open viewing face and a safe bypass to the west.
        for(int x=7;x<=8;x++) if(!third)
            blocks.put(base.offset(x,15,9),Blocks.STONE.defaultBlockState());
        if(third) for(int z=10;z<=11;z++) {
            for(int y=9;y<=15;y++) {
                blocks.put(base.offset(6,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(7,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            blocks.put(base.offset(8,14,z),Blocks.STONE.defaultBlockState());
        }
        if(joined) floor(blocks,base,7,8,14,6,7);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void footprint(LinkedHashSet<BlockPos> cells,int x1,int x2,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) cells.add(new BlockPos(x,0,z));
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) {
        well(air,x1,x2,z1,z2,y,y+2);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
