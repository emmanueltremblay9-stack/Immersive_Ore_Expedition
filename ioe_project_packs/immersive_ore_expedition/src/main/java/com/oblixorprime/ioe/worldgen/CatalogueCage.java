package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-25: six separated edges around retained rock, with four noncoplanar vertices. */
final class CatalogueCage {
    private CatalogueCage() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean omitted=variant==CatalogueTerraces.Variant.CAGE_OPEN;
        boolean split=variant==CatalogueTerraces.Variant.CAGE_SPLIT;
        var air=new LinkedHashSet<BlockPos>();
        room(air,3,6,3,4,7); // A
        room(air,8,11,3,4,7); // B
        room(air,3,11,3,4,5); // AB
        room(air,3,4,3,7,12); // AC
        room(air,3,11,3,11,12); // C and its fixed mineral retreat
        if(!omitted) room(air,8,9,3,7,12); // BC is absent at construction in B.
        room(air,6,7,15,1,7);
        room(air,split?1:3,12,15,6,7); // D and upper edge ends
        room(air,10,11,15,4,5);
        room(air,5,6,15,8,12);
        room(air,3,6,15,11,12);
        well(air,3,4,6,7,3,split?13:17); // AD
        if(split) {
            well(air,1,2,6,7,11,17);
            well(air,4,5,6,7,11,17);
            room(air,1,5,11,6,7);
        }
        well(air,11,12,6,7,3,17); // BD
        well(air,5,6,11,12,3,17); // CD
        room(air,6,9,9,4,5); // N6 belongs to the normalized BD retreat.
        room(air,8,9,9,4,7);
        room(air,8,12,9,6,7);
        room(air,8,9,9,8,12);
        room(air,9,11,9,11,12);
        room(air,3,6,9,10,11); // N7 belongs to CD, never the omitted BC edge.
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,6,7,14,1,7);
        floor(blocks,base,split?1:3,12,14,6,7);
        floor(blocks,base,10,11,14,4,5);
        floor(blocks,base,5,6,14,8,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,3,4,8,7,7);
        if(split) floor(blocks,base,1,5,10,6,7);
        floor(blocks,base,6,9,8,4,5);
        floor(blocks,base,8,9,8,4,7);
        floor(blocks,base,8,12,8,6,7);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,11,8,11,12);
        floor(blocks,base,3,6,8,10,11);
        for(int y=2;y<=11;y++) floor(blocks,base,5,7,y,8,9);
        for(int y=6;y<=11;y++) floor(blocks,base,6,7,y,6,7);
        if(split) for(int y=14;y<=14;y++) floor(blocks,base,3,3,y,6,7);
        southLadders(blocks,base,3,4,3,split?11:15);
        if(split) {
            southLadders(blocks,base,1,2,11,15);
            southLadders(blocks,base,4,5,11,15);
        }
        for(int y=3;y<=15;y++) {
            for(int z=6;z<=7;z++) {
                blocks.put(base.offset(13,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(12,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
            }
            for(int x=5;x<=6;x++) {
                blocks.put(base.offset(x,y,13),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,12),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            }
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,11,11,y,6,7);
            floor(blocks,base,5,6,y,11,11);
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void southLadders(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int y=y1;y<=y2;y++) {
            blocks.put(base.offset(x,y,5),Blocks.STONE.defaultBlockState());
            blocks.put(base.offset(x,y,6),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
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
