package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-17: a ceiling-rooted clapper, two free tips or a pierced hanging body. */
final class CatalogueBell {
    private CatalogueBell() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean fork=variant==CatalogueTerraces.Variant.BELL_FORK;
        boolean window=variant==CatalogueTerraces.Variant.BELL_WINDOW;
        var air=new LinkedHashSet<BlockPos>();
        room(air,5,11,3,4,7);
        room(air,7,9,3,8,9);
        // Expose front/back and both flanks of the ceiling-rooted core.
        for(int y=6;y<=17;y++) {
            int left=y<=7 ? (fork?5:8) : (fork?4:7);
            int right=y<=7 ? (window?11:10) : (!fork && !window && y<=10 ? 10 : window?12:11);
            for(int x=left;x<=right;x++) for(int z=5;z<=8;z++) {
                if((x==left || x==right) && (z==5 || z==8)) continue;
                air.add(new BlockPos(x,y,z));
            }
        }
        room(air,3,6,3,4,5);
        room(air,11,12,3,4,5);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,3,12,9,4,5);
        room(air,fork?7:6,fork?8:7,9,6,7);
        room(air,5,12,9,8,9);
        room(air,5,6,9,8,11);
        room(air,3,6,9,10,11);
        room(air,8,9,9,8,12);
        room(air,9,12,9,11,12);
        room(air,6,7,15,1,4);
        room(air,6,13,15,4,5);
        room(air,fork?7:6,fork?8:7,15,4,7);
        room(air,3,4,15,6,9);
        room(air,3,fork?8:7,15,8,9);
        room(air,5,6,15,8,12);
        room(air,3,5,15,11,12);
        well(air,3,4,6,7,3,17);
        well(air,12,13,4,5,3,17);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,3,12,8,4,5);
        floor(blocks,base,fork?7:6,fork?8:7,8,6,7);
        floor(blocks,base,5,12,8,8,9);
        floor(blocks,base,5,6,8,8,11);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,12,8,11,12);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,6,13,14,4,5);
        floor(blocks,base,fork?7:6,fork?8:7,14,4,7);
        floor(blocks,base,3,4,14,6,9);
        floor(blocks,base,3,fork?8:7,14,8,9);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,5,14,11,12);
        if(fork) {
            rock(blocks,base,6,6,6,7,6,7);
            rock(blocks,base,9,9,6,7,6,7);
            rock(blocks,base,5,6,8,17,6,7);
            rock(blocks,base,9,10,8,17,6,7);
            rock(blocks,base,5,10,18,18,6,7);
        } else if(window) {
            rock(blocks,base,9,10,6,7,6,7);
            rock(blocks,base,8,11,8,18,6,7);
            for(int x=9;x<=10;x++) for(int y=9;y<=11;y++) for(int z=6;z<=7;z++)
                blocks.put(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
        } else {
            rock(blocks,base,9,9,6,7,6,7);
            rock(blocks,base,8,9,8,10,6,7);
            rock(blocks,base,8,10,11,18,6,7);
        }
        for(int y=3;y<=15;y++) {
            for(int z=6;z<=7;z++) {
                blocks.put(base.offset(2,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(3,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            for(int z=4;z<=5;z++) {
                blocks.put(base.offset(14,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(13,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,4,4,y,6,7);
            floor(blocks,base,12,12,y,4,5);
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
