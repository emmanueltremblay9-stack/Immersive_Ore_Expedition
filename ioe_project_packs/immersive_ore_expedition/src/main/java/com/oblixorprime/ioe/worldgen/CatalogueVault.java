package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashSet;
import java.util.Map;

/** BP-08: a tall buttressed cavity with interrupted supported wall ledges. */
final class CatalogueVault {
    private CatalogueVault() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean dominant=variant==CatalogueTerraces.Variant.VAULT_BUTTRESS;
        boolean twin=variant==CatalogueTerraces.Variant.VAULT_TWIN;
        var air=new LinkedHashSet<BlockPos>();
        for(int x=4;x<=10;x++) for(int z=8;z<=12;z++) {
            if(x==4 && z>=11) continue;
            int roof=twin?(x<7?17:14):(x==7?17:x==4||x==10?14:16);
            well(air,x,x,z,z,3,roof);
        }
        if(!twin) room(air,4,10,3,7,8);
        room(air,3,4,3,4,7);
        room(air,3,6,3,6,7);
        room(air,8,12,3,6,7);
        room(air,8,9,3,7,8);
        room(air,9,11,3,11,12);
        room(air,6,7,15,1,4);
        room(air,6,11,15,4,5);
        room(air,6,8,15,6,7);
        room(air,5,6,15,6,12);
        room(air,3,6,15,11,12);
        room(air,11,12,15,4,7);
        room(air,5,11,9,4,5);
        room(air,11,12,9,4,9);
        room(air,8,12,9,8,9);
        room(air,8,9,9,8,12);
        room(air,9,11,9,11,12);
        room(air,5,6,9,8,11);
        room(air,3,6,9,10,11);
        well(air,11,12,6,7,3,17);
        well(air,5,6,11,12,3,17);
        var blocks=CatalogueTerraces.finish(center,air,false);
        var base=CatalogueTerraces.base(center);
        floor(blocks,base,4,10,2,8,12);
        floor(blocks,base,6,7,14,1,4);
        floor(blocks,base,6,11,14,4,5);
        floor(blocks,base,6,8,14,6,7);
        floor(blocks,base,5,6,14,6,12);
        floor(blocks,base,3,6,14,11,12);
        floor(blocks,base,11,12,14,4,7);
        floor(blocks,base,5,11,8,4,5);
        floor(blocks,base,11,12,8,4,9);
        floor(blocks,base,8,12,8,8,9);
        floor(blocks,base,8,9,8,8,12);
        floor(blocks,base,9,11,8,11,12);
        floor(blocks,base,5,6,8,8,11);
        floor(blocks,base,3,6,8,10,11);
        for(int y=3;y<=14;y++) floor(blocks,base,4,4,y,8,8);
        for(int y=3;y<=8;y++) floor(blocks,base,10,10,y,8,8);
        if(dominant) {
            for(int y=3;y<=17;y++) floor(blocks,base,7,8,y,8,10);
            // Recess the east face at the two walking levels, retaining a continuous spine.
            for(int foot:new int[]{3,9}) for(int y=foot;y<=foot+2;y++) for(int z=8;z<=10;z++)
                blocks.put(base.offset(8,y,z),Blocks.AIR.defaultBlockState());
        }
        if(twin) {
            for(int y=2;y<=17;y++) floor(blocks,base,7,7,y,8,12);
            for(int y=2;y<=14;y++) floor(blocks,base,7,7,y,7,7);
            for(int y=3;y<=5;y++) for(int z=11;z<=12;z++) blocks.put(base.offset(7,y,z),Blocks.AIR.defaultBlockState());
            for(int y=9;y<=11;y++) for(int z=8;z<=9;z++) blocks.put(base.offset(7,y,z),Blocks.AIR.defaultBlockState());
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
    private static void floor(Map<BlockPos,BlockState> blocks,BlockPos base,int x1,int x2,int y,int z1,int z2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) blocks.put(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
    }
    private static void room(LinkedHashSet<BlockPos> air,int x1,int x2,int y,int z1,int z2) { well(air,x1,x2,z1,z2,y,y+2); }
    private static void well(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) air.add(new BlockPos(x,y,z));
    }
}
