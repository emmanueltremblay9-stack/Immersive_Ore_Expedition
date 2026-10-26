package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-39: nine shared-height intersections around four retained rock cores. */
final class CatalogueGrid {
    private CatalogueGrid() { }
    private static final int[] X={3,7,11};
    private static final int[] Z={4,8,12};
    private static final int[][] HEIGHT={{3,5,7},{5,7,9},{9,11,15}};
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean offset=variant==CatalogueTerraces.Variant.GRID_OFFSET;
        boolean closed=variant==CatalogueTerraces.Variant.GRID_CLOSED;
        var air=new LinkedHashSet<BlockPos>();
        var floors=new LinkedHashMap<BlockPos,BlockState>();
        var steps=new LinkedHashMap<BlockPos,BlockState>();
        for(int row=0;row<3;row++) for(int col=0;col<3;col++)
            room(air,floors,X[col],X[col]+1,HEIGHT[row][col],Z[row],Z[row]+1);
        // Two rises fit entirely inside the two-cell gaps; intersections stay flat.
        for(int row=0;row<3;row++) for(int col=0;col<2;col++) {
            if(row==2 && col==1 || closed && row==1 && col==1) continue;
            for(int n=1;n<=2;n++) for(int z=Z[row];z<=Z[row]+1;z++)
                stair(air,steps,X[col]+1+n,z,HEIGHT[row][col]+n,Direction.EAST);
        }
        for(int col=0;col<3;col++) {
            if(offset && col==0) continue;
            for(int n=1;n<=2;n++) for(int x=X[col];x<=X[col]+1;x++)
                stair(air,steps,x,5+n,HEIGHT[0][col]+n,Direction.SOUTH);
        }
        if(offset) {
            room(air,floors,1,4,3,4,5);
            room(air,floors,1,4,5,8,9);
            for(int n=1;n<=2;n++) for(int x=1;x<=2;x++)
                stair(air,steps,x,5+n,3+n,Direction.SOUTH);
        }
        well(air,3,4,10,11,5,11);
        well(air,7,8,10,11,7,13);
        well(air,11,12,8,9,9,17);
        room(air,floors,11,12,15,10,13);
        well(air,9,10,12,13,11,17);
        // Rear side retreats retain N4 and N2 below the high intersection.
        well(air,11,12,12,13,3,17);
        room(air,floors,6,7,15,1,4);
        room(air,floors,6,8,15,4,5);
        room(air,floors,5,12,15,6,7);
        room(air,floors,10,11,15,4,5);
        room(air,floors,11,12,15,6,9);
        room(air,floors,5,6,15,8,12);
        room(air,floors,3,6,15,11,12);
        well(air,7,8,4,5,9,17);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floors.forEach((p,state)->blocks.put(base.offset(p),state));
        for(int y=2;y<=13;y++) {
            solid(blocks,base,5,6,y,6,7);
            solid(blocks,base,9,10,y,6,7);
            solid(blocks,base,5,6,y,10,11);
            solid(blocks,base,9,9,y,10,11);
            solid(blocks,base,10,10,y,10,10); // X10/Z11 is a fixed N2/N4 AIR face.
        }
        if(closed) for(int y=2;y<=17;y++) solid(blocks,base,9,10,y,8,9);
        ladder(blocks,base,4,5,10,11,5,9);
        solid(blocks,base,3,3,8,10,11);
        ladder(blocks,base,8,9,10,11,7,11);
        solid(blocks,base,7,7,10,10,11);
        ladder(blocks,base,12,13,8,9,9,15);
        solid(blocks,base,11,11,8,8,9);
        solid(blocks,base,11,11,14,8,9);
        rearLadders(blocks,base,9,10,11,15);
        solid(blocks,base,9,10,14,12,12);
        rearLadders(blocks,base,11,12,3,15);
        for(int y:new int[]{2,8,14}) solid(blocks,base,11,12,y,12,12);
        ladder(blocks,base,8,9,4,5,9,15);
        for(int y:new int[]{8,14}) solid(blocks,base,7,7,y,4,5);
        steps.forEach((p,state)->blocks.put(base.offset(p),state));
        CatalogueTerraces.supportStairs(blocks);
        CatalogueTerraces.resolveStairCorners(blocks);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void stair(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> steps,int x,int z,int foot,Direction facing) {
        well(air,x,x,z,z,foot,foot+2);
        steps.put(new BlockPos(x,foot-1,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
    }
    private static void room(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> floors,int x1,int x2,int foot,int z1,int z2) {
        well(air,x1,x2,z1,z2,foot,foot+2);
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) floors.put(new BlockPos(x,foot-1,z),Blocks.STONE.defaultBlockState());
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x,int back,int z1,int z2,int y1,int y2) {
        for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) {
            blocks.put(base.offset(back,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
    }
    private static void rearLadders(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int y=y1;y<=y2;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
    }
    private static void solid(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
