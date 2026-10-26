package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-19: two perpendicular vertical sheets and a lower gallery around retained rock. */
final class CatalogueTrihedral {
    private CatalogueTrihedral() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean chamfer=variant==CatalogueTerraces.Variant.TRIHEDRAL_CHAMFER;
        boolean pierced=variant==CatalogueTerraces.Variant.TRIHEDRAL_PIERCED;
        var air=new LinkedHashSet<BlockPos>();
        well(air,5,6,4,12,3,17);
        well(air,5,12,4,5,3,17);
        room(air,3,6,3,4,5);
        room(air,5,9,3,7,8);
        room(air,8,9,3,8,12);
        room(air,9,12,3,11,12);
        room(air,3,6,9,10,11);
        room(air,5,12,9,11,12);
        room(air,6,7,15,1,4);
        room(air,5,7,15,6,7);
        room(air,3,6,15,11,12);
        well(air,10,11,5,6,3,17);
        var cuts=new LinkedHashSet<BlockPos>();
        if(chamfer) for(int z=5;z<=7;z++) room(cuts,12-z,14-z,9,z,z+1);
        if(pierced) for(int z=5;z<=8;z++) room(cuts,14-z,16-z,9,z,z+1);
        air.addAll(cuts);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        for(int y:new int[]{8,14}) {
            floor(blocks,base,5,6,y,4,12);
            floor(blocks,base,5,12,y,4,5);
        }
        floor(blocks,base,3,6,8,10,11);
        floor(blocks,base,5,12,8,11,12);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,5,7,14,6,7);
        floor(blocks,base,3,6,14,11,12);
        for(int y=6;y<=14;y++) floor(blocks,base,8,10,y,7,9);
        if(chamfer) for(int y=8;y<=12;y++) floor(blocks,base,5,6,y,4,5);
        for(var p:cuts) {
            blocks.put(base.offset(p),Blocks.AIR.defaultBlockState());
            if(p.getY()==9) blocks.put(base.offset(p).below(),Blocks.STONE.defaultBlockState());
        }
        for(int y=3;y<=15;y++) {
            for(int z=6;z<=7;z++) {
                blocks.put(base.offset(4,y,z),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(5,y,z),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
            }
            for(int x=10;x<=11;x++) {
                blocks.put(base.offset(x,y,7),Blocks.STONE.defaultBlockState());
                blocks.put(base.offset(x,y,6),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
            }
        }
        for(int y:new int[]{8,14}) {
            floor(blocks,base,6,6,y,6,7);
            floor(blocks,base,10,11,y,5,5);
        }
        blocks.put(center.below(),Blocks.CALCITE.defaultBlockState());
        return blocks;
    }
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
