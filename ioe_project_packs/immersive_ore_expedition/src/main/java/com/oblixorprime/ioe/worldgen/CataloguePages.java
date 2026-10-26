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

/** BP-22: three widening inclined sheets, joined at a supported front hinge. */
final class CataloguePages {
    private CataloguePages() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean window=variant==CatalogueTerraces.Variant.PAGES_WINDOW;
        boolean torn=variant==CatalogueTerraces.Variant.PAGES_TORN;
        var air=new LinkedHashSet<BlockPos>();
        var floors=new LinkedHashMap<BlockPos,BlockState>();
        var steps=new LinkedHashMap<BlockPos,BlockState>();
        // The three sheets broaden away from their hinge and have distinct inclinations.
        for(int z=6;z<=12;z++) {
            int low=z<=9?3:z==10?2:1;
            int high=z<=9?15:z==10?14:13;
            for(int x=z<=7?4:3;x<=7;x++) cell(air,floors,x,low,z);
            for(int x=7;x<=(z<=7?10:12);x++) cell(air,floors,x,high,z);
            for(int x=5;x<=9;x++) {
                if(torn && (x==7 && z<=10 || x>=8 && z<=7)) continue;
                int middle=x<=6?9:x==7?8:7;
                cell(air,floors,x,middle,z);
                if(x==6 || x==7) steps.put(new BlockPos(x,middle-1,z),stairs(Direction.WEST));
            }
            if(z==9 || z==10) {
                for(int x=3;x<=7;x++) steps.put(new BlockPos(x,low-1,z),stairs(Direction.NORTH));
                for(int x=7;x<=12;x++) steps.put(new BlockPos(x,high-1,z),stairs(Direction.NORTH));
            }
        }
        room(air,floors,5,6,3,4,5);
        room(air,floors,2,3,3,4,7);
        room(air,floors,2,7,3,6,7);
        room(air,floors,5,12,3,6,7);
        room(air,floors,8,9,3,8,12);
        room(air,floors,9,11,3,11,12);
        room(air,floors,5,8,9,4,5);
        room(air,floors,3,6,9,10,11);
        room(air,floors,8,9,7,13,13);
        room(air,floors,11,12,9,12,13);
        // Keep the two-wide N2 approach behind its reserved ore mask at X10/Y8/Z11.
        for(int z=12;z<=13;z++) {
            cell(air,floors,10,8,z);
            steps.put(new BlockPos(10,7,z),stairs(Direction.EAST));
        }
        for(int z=12;z<=13;z++) steps.put(new BlockPos(11,8,z),stairs(Direction.EAST));
        room(air,floors,6,7,15,1,5);
        room(air,floors,5,11,15,4,5);
        room(air,floors,5,8,15,6,7);
        room(air,floors,10,11,15,4,7);
        room(air,floors,5,6,15,8,12);
        room(air,floors,3,6,15,11,12);
        well(air,5,6,4,5,3,17);
        if(window) well(air,5,6,11,12,1,9);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floors.forEach((p,s)->blocks.put(base.offset(p),s));
        steps.forEach((p,s)->blocks.put(base.offset(p),s));
        if(torn) for(int z=6;z<=10;z++) for(int y=7;y<=11;y++)
            blocks.put(base.offset(7,y,z),Blocks.STONE.defaultBlockState());
        for(int z=4;z<=5;z++) for(int y=3;y<=15;y++) {
            blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        }
        for(int y:new int[]{2,8,14}) for(int z=4;z<=5;z++) blocks.put(base.offset(6,y,z),Blocks.STONE.defaultBlockState());
        if(window) {
            for(int x=5;x<=6;x++) for(int y=1;y<=9;y++) {
                blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            }
            for(int x=5;x<=6;x++) blocks.put(base.offset(x,8,11),Blocks.STONE.defaultBlockState());
        }
        CatalogueTerraces.supportStairs(blocks);
        CatalogueTerraces.resolveStairCorners(blocks);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static BlockState stairs(Direction facing) { return Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing); }
    private static void cell(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> floors,int x,int y,int z) {
        well(air,x,x,z,z,y,y+2);floors.put(new BlockPos(x,y-1,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,Map<BlockPos,BlockState> floors,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) cell(air,floors,x,y,z);
    }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
