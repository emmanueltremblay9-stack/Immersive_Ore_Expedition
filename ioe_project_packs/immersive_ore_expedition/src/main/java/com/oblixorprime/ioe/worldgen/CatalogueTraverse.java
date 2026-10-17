package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-40: one oblique rooted bar, a framed middle window or two upper attachments. */
final class CatalogueTraverse {
    private CatalogueTraverse() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean fork=variant==CatalogueTerraces.Variant.TRAVERSE_FORK;
        boolean window=variant==CatalogueTerraces.Variant.TRAVERSE_WINDOW;
        var air=new LinkedHashSet<BlockPos>();
        // Rounded viewing contours follow the oblique axis, not a vertical pillar.
        for(int y=6;y<=14;y++) {
            int x=y<=5?4:y<=7?5:y<=12?6:y<=14?7:8;
            int z=y<=7?5:y<=12?6:y<=14?7:8;
            int right=y<=5?6:y<=7?8:y<=12?9:y<=14?10:fork?12:11;
            for(int xx=x-1;xx<=Math.min(13,right+1);xx++) for(int zz=z-1;zz<=z+2;zz++) {
                if((xx==x-1 || xx==right+1) && (zz==z-1 || zz==z+2)) continue;
                air.add(new BlockPos(xx,y,zz));
            }
        }
        room(air,3,11,3,4,5);
        room(air,3,4,3,5,8);
        room(air,3,9,3,7,8);
        room(air,8,9,3,4,9);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,3,11,9,4,5);
        room(air,5,11,9,8,9);
        room(air,10,11,9,4,9);
        room(air,5,6,9,8,11);
        room(air,3,6,9,10,11);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        if(window) room(air,7,8,9,4,9);
        room(air,6,7,15,1,7);
        room(air,6,11,15,4,5);
        room(air,3,6,15,6,9);
        room(air,3,7,15,8,9);
        room(air,5,6,15,8,12);
        room(air,3,5,15,11,12);
        room(air,7,fork?12:11,15,6,7);
        well(air,3,4,6,7,3,17);
        well(air,11,12,4,5,3,17);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,3,11,8,4,5);
        floor(blocks,base,5,11,8,8,9);
        floor(blocks,base,10,11,8,4,9);
        floor(blocks,base,5,6,8,8,11);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,6,7,14,1,7);
        floor(blocks,base,6,11,14,4,5);
        floor(blocks,base,7,fork?12:11,14,6,7);
        floor(blocks,base,3,6,14,6,9);
        floor(blocks,base,3,7,14,8,9);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,5,14,11,12);
        rock(blocks,base,5,7,2,5,5,6);
        rock(blocks,base,5,8,6,7,5,6);
        rock(blocks,base,6,9,8,12,6,7);
        rock(blocks,base,7,10,13,14,7,8);
        if(fork) {
            rock(blocks,base,7,12,14,14,7,8);
            rock(blocks,base,7,8,15,18,8,9);
            rock(blocks,base,11,12,15,18,8,9);
            floor(blocks,base,7,12,14,6,7);
            for(int x=9;x<=10;x++) for(int y=15;y<=17;y++) for(int z=8;z<=9;z++)
                blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
        } else rock(blocks,base,8,11,15,18,8,9);
        if(window) for(int x=7;x<=8;x++) for(int y=9;y<=11;y++) for(int z=6;z<=7;z++)
            blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
        for(int y=3;y<=15;y++) {
            for(int z=6;z<=7;z++) {
                blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            for(int z=4;z<=5;z++) {
                blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,4,4,y,6,7);
            floor(blocks,base,11,11,y,4,5);
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void rock(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y1,int y2,int z1,int z2) {
        for(int y=y1;y<=y2;y++) floor(blocks,base,x1,x2,y,z1,z2);
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
