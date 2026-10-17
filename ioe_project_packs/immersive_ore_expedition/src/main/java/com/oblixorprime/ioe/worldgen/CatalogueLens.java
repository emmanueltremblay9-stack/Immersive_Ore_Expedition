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

/** BP-01: flattened lens, inclined facing ledges and an oblique retained fold. */
final class CatalogueLens {
    private CatalogueLens() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean overlap=variant==CatalogueTerraces.Variant.LENS_OVERLAP;
        boolean broken=variant==CatalogueTerraces.Variant.LENS_BROKEN;
        var air=new LinkedHashSet<BlockPos>();
        var floors=new LinkedHashMap<BlockPos,BlockState>();
        for(int x=2;x<=12;x++) for(int z=3;z<=13;z++) for(int y=5;y<=13;y++)
            if(16*((x-7)*(x-7)+(z-8)*(z-8))+25*(y-9)*(y-9)<=400) air.add(new BlockPos(x,y,z));
        room(air,floors,6,7,15,1,7);
        room(air,floors,5,6,15,6,12);
        room(air,floors,3,6,15,11,12);
        well(air,5,6,12,13,3,17);
        room(air,floors,3,4,3,4,7);
        room(air,floors,3,6,3,6,7);
        room(air,floors,5,6,3,8,12);
        room(air,floors,3,7,9,4,5);
        room(air,floors,3,4,9,6,7);
        room(air,floors,5,5,10,6,7);
        room(air,floors,6,7,11,6,8);
        room(air,floors,6,7,10,9,9);
        room(air,floors,6,7,9,10,10);
        room(air,floors,3,7,9,10,11);
        room(air,floors,5,6,9,12,13);
        stairs(floors,5,5,9,6,7,Direction.EAST);
        stairs(floors,6,6,10,6,7,Direction.EAST);
        stairs(floors,6,7,10,8,8,Direction.NORTH);
        stairs(floors,6,7,9,9,9,Direction.NORTH);
        room(air,floors,10,11,15,4,5);
        room(air,floors,10,11,14,6,6);
        room(air,floors,10,11,13,7,12);
        well(air,10,11,12,13,3,15);
        room(air,floors,10,12,9,12,13);
        room(air,floors,10,10,9,11,11);
        room(air,floors,9,11,3,11,12);
        if(!overlap && !broken) {
            room(air,floors,7,11,15,4,5);
            room(air,floors,5,10,9,12,13);
        } else {
            room(air,floors,8,8,12,broken?6:7,8);
            room(air,floors,9,9,13,7,broken?9:8);
            stairs(floors,8,8,11,broken?6:7,8,Direction.EAST);
            stairs(floors,9,9,12,7,broken?9:8,Direction.EAST);
            if(broken) {
                well(air,8,8,6,8,12,17);
                well(air,9,9,7,9,13,17);
            }
        }
        // The A front passage must not flatten the right ledge's return steps.
        stairs(floors,10,11,14,5,5,Direction.NORTH);
        stairs(floors,10,11,13,6,6,Direction.NORTH);
        // Retain the host fold; its windows are the only transverse routes.
        air.removeIf(p->wall(p,overlap) && !opening(p,overlap,broken));
        floors.keySet().removeIf(p->wall(p,overlap) && !opening(p.above(),overlap,broken));
        var blocks=CatalogueTerraces.finish(center,air,false);var base=CatalogueTerraces.base(center);
        floors.forEach((p,s)->blocks.put(base.offset(p),s));
        for(int x=5;x<=6;x++) for(int y=3;y<=15;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        for(int x=10;x<=11;x++) for(int y=3;y<=13;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        for(int y:new int[]{2,8,14}) solid(blocks,base,5,6,y,12,12);
        for(int y:new int[]{2,8,12}) solid(blocks,base,10,11,y,12,12);
        CatalogueTerraces.supportStairs(blocks);
        CatalogueTerraces.resolveStairCorners(blocks);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static boolean wall(BlockPos p,boolean overlap) {
        return p.getX()==8 && p.getZ()<=9 || p.getX()==9 && p.getZ()>=(overlap?6:8);
    }
    private static boolean opening(BlockPos p,boolean overlap,boolean broken) {
        int x=p.getX(),y=p.getY(),z=p.getZ();
        if(!overlap && !broken) return x==8 && z>=4 && z<=5 && y>=15 && y<=17
                || x==9 && z>=12 && z<=13 && y>=9 && y<=11;
        return x==8 && z>=(broken?6:7) && z<=8 && y>=12 && y<=(broken?17:14)
                || x==9 && z>=7 && z<=(broken?9:8) && y>=13 && y<=(broken?17:15);
    }
    private static void stairs(Map<BlockPos,BlockState> floors,int x1,int x2,int y,int z1,int z2,Direction facing) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) floors.put(new BlockPos(x,y,z),Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
    }
    private static void room(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> floors,int x1,int x2,int foot,int z1,int z2) {
        well(air,x1,x2,z1,z2,foot,foot+2);
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) floors.put(new BlockPos(x,foot-1,z),Blocks.STONE.defaultBlockState());
    }
    private static void solid(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
