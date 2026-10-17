package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-38: five separated spokes join a hollow hub to an inclined rim. */
final class CatalogueWheel {
    private CatalogueWheel() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean blind=variant==CatalogueTerraces.Variant.WHEEL_BLIND;
        boolean split=variant==CatalogueTerraces.Variant.WHEEL_SPLIT;
        var air=new LinkedHashSet<BlockPos>();
        room(air,6,7,15,1,4);
        room(air,5,7,15,3,4);
        room(air,5,6,15,4,7);
        room(air,3,12,15,6,7);
        room(air,10,11,15,4,7);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        well(air,3,4,6,7,3,17);
        well(air,11,12,6,7,3,17);
        room(air,3,4,3,4,12);
        room(air,3,6,3,11,12);
        room(air,3,11,3,12,13);
        room(air,8,12,3,6,7);
        room(air,8,9,3,8,12);
        // The hub is a visitable chamber, not the solid centre of an annulus.
        room(air,6,9,9,7,9);
        room(air,3,6,9,6,7);
        room(air,5,7,9,4,5);
        room(air,5,6,9,4,7);
        room(air,11,12,9,7,9);
        if(!blind) room(air,10,10,9,8,9);
        room(air,5,6,9,9,13);
        room(air,3,6,9,10,11);
        room(air,8,9,9,9,12);
        room(air,8,11,9,12,13);
        well(air,7,8,6,7,9,17);
        well(air,5,6,12,13,3,11);
        well(air,9,10,12,13,3,11);
        if(split) room(air,3,9,3,8,9);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,5,7,14,3,4);
        floor(blocks,base,5,6,14,4,7);
        floor(blocks,base,3,12,14,6,7);
        floor(blocks,base,10,11,14,4,7);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,3,4,2,4,12);
        floor(blocks,base,3,6,2,11,12);
        floor(blocks,base,3,11,2,12,13);
        floor(blocks,base,8,12,2,6,7);
        floor(blocks,base,8,9,2,8,12);
        floor(blocks,base,6,9,8,7,9);
        floor(blocks,base,3,6,8,6,7);
        floor(blocks,base,5,7,8,4,5);
        floor(blocks,base,5,6,8,4,7);
        floor(blocks,base,11,12,8,7,9);
        if(!blind) floor(blocks,base,10,10,8,8,9);
        floor(blocks,base,5,6,8,9,13);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,8,9,8,9,12);
        floor(blocks,base,8,11,8,12,13);
        if(split) floor(blocks,base,3,9,2,8,9);
        // Retained host-rock sectors separate the spokes and the two low rim arcs.
        for(int y=2;y<=6;y++) floor(blocks,base,5,7,y,10,11);
        for(int y=9;y<=11;y++) {
            floor(blocks,base,7,7,y,10,11);
            floor(blocks,base,5,5,y,8,8);
        }
        for(int y=3;y<=15;y++) for(int z=6;z<=7;z++) {
            blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,4,4,y,6,7);
            floor(blocks,base,11,11,y,6,7);
        }
        for(int x=7;x<=8;x++) for(int y=9;y<=15;y++) {
            blocks.put(base.offset(x,y,5),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,6),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
        for(int y:new int[]{8,14}) floor(blocks,base,7,8,y,7,7);
        for(int x:new int[]{5,6,9,10}) for(int y=3;y<=9;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        floor(blocks,base,5,6,8,12,12);
        floor(blocks,base,9,10,8,12,12);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int foot,int z1,int z2) { well(air,x1,x2,z1,z2,foot,foot+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
