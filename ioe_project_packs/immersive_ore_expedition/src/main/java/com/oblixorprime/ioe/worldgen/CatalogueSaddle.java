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

/** BP-12: a continuous rotated saddle with fixed mineral retreats at its boundary. */
final class CatalogueSaddle {
    private CatalogueSaddle() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean notch=variant==CatalogueTerraces.Variant.SADDLE_NOTCH;
        boolean bridge=variant==CatalogueTerraces.Variant.SADDLE_BRIDGE;
        return compose(center,heights(notch),bridge);
    }
    static Map<BlockPos,Integer> heights(boolean notch) {
        var out=new LinkedHashMap<BlockPos,Integer>();
        for(int x=3;x<=12;x++) for(int z=4;z<=13;z++) {
            if(!surfaceCell(x,z)) continue;
            int a=x<=4?-2:x==5?-1:x==6?0:x==7?1:2;
            int b=z<=5?-2:z==6?-1:z<=9?0:z==10?1:2;
            int foot=9-Math.round(a*b/2.0f);
            if(x>=7 && x<=8 && z<=5 || x<=6 && z>=10 && z<=11) foot=9;
            if(notch && x>=9 && x<=10 && z>=5 && z<=6) foot=9;
            out.put(new BlockPos(x,0,z),foot);
        }
        return out;
    }
    static boolean surfaceCell(int x,int z) {
        return !(x<=4 && z>=8 && z<=9 || x>=10 && z>=10 || (x==5 || x==6) && z==13);
    }
    /** Shared supported surface and canonical boundary retreats; no resource draws. */
    static Map<BlockPos,BlockState> compose(BlockPos center,Map<BlockPos,Integer> heights,boolean bridge) {
        var air=new LinkedHashSet<BlockPos>();
        var floors=new LinkedHashMap<BlockPos,BlockState>();
        for(var e:heights.entrySet()) {
            int x=e.getKey().getX(),z=e.getKey().getZ(),foot=e.getValue();
            room(air,floors,x,x,foot,z,z);
            for(var lower:java.util.List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST)) {
                var h=heights.get(e.getKey().relative(lower));
                if(h!=null && h==foot-1) {
                    floors.put(new BlockPos(x,foot-1,z),Blocks.STONE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING,lower.getOpposite()));
                    break;
                }
            }
        }
        int westTop=bridge?13:Math.max(9,Math.max(heights.getOrDefault(new BlockPos(3,0,6),9),
                heights.getOrDefault(new BlockPos(3,0,7),9)));
        room(air,floors,3,4,3,4,7);
        well(air,3,4,6,7,3,westTop+2);
        room(air,floors,9,11,3,11,12);
        well(air,9,10,12,13,3,11);
        room(air,floors,10,12,9,12,13);
        room(air,floors,10,10,9,11,11);
        room(air,floors,6,7,15,1,4);
        room(air,floors,6,11,15,4,5);
        room(air,floors,6,8,15,6,7);
        room(air,floors,10,11,15,4,7);
        well(air,11,12,6,7,9,17);
        well(air,5,6,11,12,9,17);
        room(air,floors,3,6,15,11,12);
        if(bridge) {
            room(air,floors,3,4,13,4,7);
            well(air,3,4,4,5,7,15);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floors.forEach((p,s)->blocks.put(base.offset(p),s));
        for(int z=6;z<=7;z++) for(int y=3;y<=westTop;y++) {
            blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        }
        for(int z=6;z<=7;z++) for(int y=9;y<=15;y++) {
            blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        solid(blocks,base,11,11,14,6,7);
        for(int x=5;x<=6;x++) for(int y=9;y<=15;y++) {
            blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        solid(blocks,base,5,6,14,11,11);
        for(int x=9;x<=10;x++) for(int y=3;y<=9;y++) {
            blocks.put(base.offset(x,y,14),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,13),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        solid(blocks,base,10,10,8,12,12);
        if(bridge) for(int x=3;x<=4;x++) for(int y=7;y<=13;y++) {
            blocks.put(base.offset(x,y,3),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,4),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        }
        CatalogueTerraces.supportStairs(blocks);
        CatalogueTerraces.resolveStairCorners(blocks);
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
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
