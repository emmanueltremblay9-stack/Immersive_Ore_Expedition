package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-30: a broad prism whose ceiling rises along one diagonal axis. */
final class CatalogueWedge {
    private CatalogueWedge() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean gorges=variant==CatalogueTerraces.Variant.WEDGE_GORGES;
        boolean gallery=variant==CatalogueTerraces.Variant.WEDGE_GALLERY;
        var air=new LinkedHashSet<BlockPos>();
        // u=x+z controls height; v=x-z bounds width independently of height.
        for(int x=1;x<=13;x++) for(int z=1;z<=13;z++) {
            int u=x+z;
            if(u<8 || u>14 || Math.abs(x-z)>5) continue;
            int ceiling=Math.max(5,roof(u)-(Math.abs(x-z)==4?2:0));
            for(int y=3;y<=ceiling;y++) {
                // Bevel only the outer viewing edge; keep both low gorge widths intact.
                if(Math.abs(x-z)==5 && y>5) continue;
                air.add(new BlockPos(x,y,z));
            }
        }
        room(air,3,4,3,4,5);
        room(air,7,8,3,8,12);
        room(air,9,11,3,11,12);
        room(air,8,12,3,6,7);
        room(air,6,11,9,4,5);
        room(air,8,12,9,6,7);
        room(air,8,9,9,6,12);
        room(air,5,11,9,11,12);
        room(air,3,6,9,10,11);
        room(air,6,7,15,1,4);
        room(air,6,8,15,4,5);
        room(air,6,8,15,6,7);
        room(air,8,12,15,6,7);
        room(air,10,11,15,4,5);
        room(air,6,7,15,8,12);
        room(air,3,7,15,11,12);
        well(air,11,12,6,7,3,17);
        well(air,5,6,11,12,3,gallery?17:11);
        if(gallery) {
            room(air,8,9,15,8,11);
            room(air,5,9,15,10,11);
        }
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,11,8,4,5);
        floor(blocks,base,8,12,8,6,7);
        floor(blocks,base,8,9,8,6,12);
        floor(blocks,base,5,11,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,6,8,14,4,5);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,8,12,14,6,7);
        floor(blocks,base,10,11,14,4,5);
        floor(blocks,base,6,7,14,8,12);
        floor(blocks,base,3,7,14,11,12);
        if(gorges) for(int z=5;z<=6;z++) {
            int x=6;
            // Leave a two-wide near-tip confluence before the rib starts.
            for(int y=2;y<=roof(x+z);y++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        }
        for(int y=3;y<=15;y++) for(int z=6;z<=7;z++) {
            blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        }
        for(int y=3;y<=(gallery?15:9);y++) for(int x=5;x<=6;x++) {
            blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        for(int y:new int[]{8,14}) floor(blocks,base,11,11,y,6,7);
        floor(blocks,base,5,6,8,11,11);
        if(gallery) {
            floor(blocks,base,8,9,14,8,11);
            floor(blocks,base,5,9,14,10,11);
            // Keep the second supported descent open through its upper gallery floor.
            for(int x=5;x<=6;x++) blocks.put(base.offset(x,14,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static int roof(int u) { return Math.min(17,Math.max(5,2*u-9)); }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
