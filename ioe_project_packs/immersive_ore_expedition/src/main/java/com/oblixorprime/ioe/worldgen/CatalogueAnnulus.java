package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-04: unequal broken arcs around an intact non-mineral core. */
final class CatalogueAnnulus {
    private CatalogueAnnulus() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean bridge=variant==CatalogueTerraces.Variant.ANNULUS_BRIDGE;
        boolean crescents=variant==CatalogueTerraces.Variant.ANNULUS_CRESCENTS;
        var air=new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,4);
        room(air,crescents?3:5,11,15,4,5);
        room(air,11,12,15,4,9);
        room(air,6,8,15,6,7);
        room(air,3,6,15,11,12);
        room(air,5,11,9,4,5);
        room(air,10,11,9,4,9);
        room(air,11,12,9,8,12);
        room(air,3,12,9,11,12);
        room(air,3,4,9,10,11);
        room(air,3,4,3,4,12);
        room(air,3,12,3,11,12);
        room(air,3,10,3,4,5);
        well(air,5,6,11,12,3,crescents?11:17);
        if(crescents) {
            well(air,3,4,6,7,3,17);
            // The canonical east alcove masks separate the middle east arc from the rear.
            // Join that arc to the sole side neck, without creating another upper descent.
            room(air,3,5,9,4,5);
            room(air,3,4,9,5,7);
            room(air,3,4,15,6,9);
            room(air,3,6,15,8,9);
            room(air,5,6,15,8,12);
        } else {
            well(air,11,12,8,9,9,17);
            well(air,5,6,5,6,3,11);
        }
        var span=new LinkedHashSet<BlockPos>();
        if(bridge) {
            for(int z=6;z<=9;z++) room(span,14-z,16-z,15,z,z+1);
            room(span,5,6,15,10,12);
            air.addAll(span);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,crescents?3:5,11,14,4,5);
        floor(blocks,base,11,12,14,4,9);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,5,11,8,4,5);
        floor(blocks,base,10,11,8,4,9);
        floor(blocks,base,11,12,8,8,12);
        floor(blocks,base,3,12,8,11,12);
        floor(blocks,base,3,4,8,10,11);
        for(int y=6;y<=14;y++) floor(blocks,base,7,9,y,7,9);
        for(int y=3;y<=(crescents?9:15);y++) for(int x=5;x<=6;x++) {
            blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        floor(blocks,base,5,6,8,11,11);
        if(crescents) {
            for(int y=3;y<=15;y++) for(int z=6;z<=7;z++) {
                blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            floor(blocks,base,3,5,8,4,5);
            floor(blocks,base,4,4,8,6,7);
            floor(blocks,base,3,4,14,6,9);
            floor(blocks,base,3,6,14,8,9);
            floor(blocks,base,5,6,14,8,12);
            // Restore ladder mouths after laying the upper crescent floor.
            for(int z=6;z<=7;z++) blocks.put(base.offset(3,14,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        } else {
            floor(blocks,base,5,6,14,11,11);
            for(int y=3;y<=9;y++) for(int x=5;x<=6;x++) {
                blocks.put(base.offset(x,y,7),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,6),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            }
            floor(blocks,base,5,6,8,5,5);
            for(int y=9;y<=15;y++) for(int z=8;z<=9;z++) {
                blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            floor(blocks,base,11,11,14,8,9);
        }
        for(var p:span) if(p.getY()==15) blocks.put(base.offset(p).below(),Blocks.STONE.defaultBlockState());
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
