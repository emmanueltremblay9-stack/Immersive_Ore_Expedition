package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-26: one rooted rock stem, a visitable cap and a supported route below its overhang. */
final class CatalogueMushroom {
    private CatalogueMushroom() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean slit = variant == CatalogueTerraces.Variant.MUSHROOM_SLIT;
        boolean lobes = variant == CatalogueTerraces.Variant.MUSHROOM_LOBES;
        var cap = new LinkedHashSet<BlockPos>();
        footprint(cap,6,10,3,4);
        footprint(cap,5,11,5,6);
        footprint(cap,4,11,7,8);
        footprint(cap,4,6,9,10);
        footprint(cap,3,6,11,12);
        var splitCap = new LinkedHashSet<BlockPos>();
        footprint(splitCap,7,11,3,7);
        footprint(splitCap,3,7,9,12);
        footprint(splitCap,6,8,7,8);
        var air = new LinkedHashSet<BlockPos>();
        for(var cell:cap) {
            for(int y=9;y<=11;y++) air.add(cell.atY(y));
            for(int y=15;y<=17;y++) air.add(cell.atY(y));
            if(lobes && !splitCap.contains(cell)) for(int y=12;y<=14;y++) air.add(cell.atY(y));
        }
        if(lobes) for(var cell:splitCap) for(int y=15;y<=17;y++) air.add(cell.atY(y));
        // A broad walkable base and upper underside surround a narrower, non-walkable
        // viewing waist. The single stem remains exposed on every side through it.
        room(air,5,10,3,4,9);
        well(air,6,9,5,8,6,8);
        room(air,3,6,3,4,5);
        room(air,11,12,3,4,9);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,5,12,9,4,5);
        room(air,5,6,9,4,10);
        room(air,3,6,9,10,11);
        room(air,11,12,9,4,9);
        room(air,5,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,6,8,15,1,3);
        room(air,7,8,15,3,5);
        room(air,10,12,15,4,5);
        room(air,12,13,15,4,8);
        well(air,12,13,6,7,3,17);
        if(slit) {
            well(air,5,6,3,7,12,14);
            well(air,5,6,5,6,9,17);
        }
        var blocks = CatalogueTerraces.finish(center,air,false);
        var base = CatalogueTerraces.base(center);
        for(var cell:(lobes ? splitCap : cap)) for(int y=12;y<=14;y++) {
            if(slit && cell.getX()>=5 && cell.getX()<=6 && cell.getZ()>=3 && cell.getZ()<=7) continue;
            blocks.put(base.offset(cell.atY(y)),Blocks.STONE.defaultBlockState());
        }
        for(int x=7;x<=8;x++) for(int z=6;z<=7;z++) for(int y=2;y<=11;y++)
            blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        floor(blocks,base,5,12,8,4,5);
        floor(blocks,base,5,6,8,4,10);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,11,12,8,4,9);
        floor(blocks,base,5,12,8,8,9);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        for(int z=6;z<=7;z++) {
            for(int y=3;y<=15;y++) blocks.put(base.offset(13,y,z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            blocks.put(base.offset(12,8,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,14,z),Blocks.STONE.defaultBlockState());
        }
        if(slit) for(int z=5;z<=6;z++) {
            for(int y=9;y<=15;y++) {
                blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            blocks.put(base.offset(6,14,z),Blocks.STONE.defaultBlockState());
        }
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
