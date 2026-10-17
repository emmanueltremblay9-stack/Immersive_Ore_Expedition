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

/** BP-18: dry channels divide and reunite at two distinct confluences. */
final class CatalogueDelta {
    private CatalogueDelta() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean capture=variant==CatalogueTerraces.Variant.DELTA_CAPTURE;
        boolean suspended=variant==CatalogueTerraces.Variant.DELTA_SUSPENDED;
        var air=new LinkedHashSet<BlockPos>();
        var floors=new LinkedHashMap<BlockPos,BlockState>();
        var steps=new LinkedHashMap<BlockPos,BlockState>();
        room(air,floors,6,7,15,1,4);
        room(air,floors,5,12,15,4,5);
        room(air,floors,5,8,15,6,7);
        room(air,floors,5,6,15,8,12);
        room(air,floors,3,6,15,11,12);
        well(air,5,6,6,7,9,17);
        well(air,8,9,6,7,9,17);
        room(air,floors,11,12,14,6,6);
        room(air,floors,11,12,13,7,13);
        for(int x=11;x<=12;x++) {
            steps.put(new BlockPos(x,14,5),stairs(Direction.NORTH));
            steps.put(new BlockPos(x,13,6),stairs(Direction.NORTH));
        }
        well(air,11,12,12,13,9,15);
        room(air,floors,5,6,9,8,12);
        room(air,floors,3,6,9,10,11);
        room(air,floors,8,9,9,8,capture?9:12);
        room(air,floors,7,8,9,4,5);
        room(air,floors,8,9,9,4,7);
        room(air,floors,3,12,9,12,13);
        if(capture) room(air,floors,5,9,9,8,9);
        well(air,3,4,12,13,suspended?5:3,11);
        well(air,6,7,12,13,5,11);
        well(air,9,10,12,13,3,11);
        room(air,floors,3,10,3,4,5);
        room(air,floors,3,4,3,6,suspended?10:13);
        room(air,floors,8,9,3,6,12);
        room(air,floors,8,11,3,12,13);
        room(air,floors,6,7,5,12,13);
        if(suspended) {
            room(air,floors,3,7,5,12,13);
            room(air,floors,3,4,4,11,11);
            for(int x=3;x<=4;x++) {
                steps.put(new BlockPos(x,4,12),stairs(Direction.SOUTH));
                steps.put(new BlockPos(x,3,11),stairs(Direction.SOUTH));
            }
        } else {
            room(air,floors,6,7,5,7,12);
            room(air,floors,6,7,4,6,6);
            for(int x=6;x<=7;x++) {
                steps.put(new BlockPos(x,4,7),stairs(Direction.SOUTH));
                steps.put(new BlockPos(x,3,6),stairs(Direction.SOUTH));
            }
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floors.forEach((p,s)->blocks.put(base.offset(p),s));
        for(int y=2;y<=7;y++) solid(blocks,base,5,5,y,6,11);
        if(capture) for(int y=8;y<=11;y++) solid(blocks,base,8,9,y,10,11);
        for(int y=9;y<=15;y++) for(int z=6;z<=7;z++) {
            blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            blocks.put(base.offset(10,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(9,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int y:new int[]{8,14}) {
            solid(blocks,base,6,6,y,6,7);solid(blocks,base,8,8,y,6,7);
        }
        rearLadders(blocks,base,11,12,9,13);
        for(int y:new int[]{8,12}) solid(blocks,base,11,12,y,12,12);
        rearLadders(blocks,base,3,4,suspended?5:3,9);
        solid(blocks,base,3,4,8,12,12);
        if(suspended) solid(blocks,base,3,4,4,12,12);
        rearLadders(blocks,base,6,7,5,9);
        for(int y:new int[]{4,8}) solid(blocks,base,6,7,y,12,12);
        rearLadders(blocks,base,9,10,3,9);
        solid(blocks,base,9,10,8,12,12);
        steps.forEach((p,s)->blocks.put(base.offset(p),s));
        CatalogueTerraces.supportStairs(blocks);
        CatalogueTerraces.resolveStairCorners(blocks);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static BlockState stairs(Direction facing) { return Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing); }
    private static void room(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> floors,int x1,int x2,int foot,int z1,int z2) {
        well(air,x1,x2,z1,z2,foot,foot+2);
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) floors.put(new BlockPos(x,foot-1,z),Blocks.STONE.defaultBlockState());
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
