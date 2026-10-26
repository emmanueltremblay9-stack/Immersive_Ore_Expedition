package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-13: serial transverse diaphragms with descending, staggered windows. */
final class CatalogueNave {
    private CatalogueNave() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean doubleWindow=variant==CatalogueTerraces.Variant.NAVE_DOUBLE;
        boolean arch=variant==CatalogueTerraces.Variant.NAVE_ARCH;
        var air=new LinkedHashSet<BlockPos>();
        // Tall, narrow bays expose the diaphragms without excavating a box.
        well(air,6,9,5,7,3,17);
        well(air,6,10,9,10,3,17);
        well(air,7,8,12,13,3,17);
        room(air,6,7,15,1,5);
        room(air,6,11,15,5,7);
        room(air,10,11,15,4,5);
        room(air,6,11,9,5,7);
        room(air,6,8,9,4,5);
        room(air,3,11,3,5,7);
        room(air,3,4,3,4,5);
        room(air,10,11,9,8,10);
        room(air,3,11,9,9,10);
        room(air,6,10,3,9,10);
        room(air,6,10,15,9,10);
        room(air,8,9,3,11,12);
        room(air,7,11,3,12,13);
        room(air,7,11,9,12,13);
        room(air,3,8,15,12,13);
        well(air,12,13,6,7,3,17);
        well(air,6,7,9,10,3,17);
        if(doubleWindow) room(air,8,9,15,7,9);
        if(arch) room(air,6,9,15,10,12);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,5);
        floor(blocks,base,6,11,14,5,7);
        floor(blocks,base,10,11,14,4,5);
        floor(blocks,base,6,11,8,5,7);
        floor(blocks,base,6,8,8,4,5);
        floor(blocks,base,10,11,8,8,10);
        floor(blocks,base,3,11,8,9,10);
        floor(blocks,base,6,10,14,9,10);
        floor(blocks,base,7,11,8,12,13);
        floor(blocks,base,3,8,14,12,13);
        ladder(blocks,base,12,13,6,5,3,15,Direction.SOUTH);
        ladder(blocks,base,6,7,9,8,3,15,Direction.SOUTH);
        ladder(blocks,base,7,8,13,14,3,15,Direction.NORTH);
        for(int y:new int[]{8,14}) {
            floor(blocks,base,12,13,y,7,7);
            floor(blocks,base,6,7,y,10,10);
            floor(blocks,base,7,8,y,12,12);
        }
        if(doubleWindow) floor(blocks,base,8,9,14,7,9);
        if(arch) {
            floor(blocks,base,6,9,14,10,12);
            for(int x:new int[]{6,9}) blocks.put(base.offset(x,17,11),Blocks.STONE.defaultBlockState());
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void ladder(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int z,int back,int y1,int y2,Direction facing) {
        for(int x=x1;x<=x2;x++) for(int y=y1;y<=y2;y++) {
            blocks.put(base.offset(x,y,back),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,facing));
        }
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
