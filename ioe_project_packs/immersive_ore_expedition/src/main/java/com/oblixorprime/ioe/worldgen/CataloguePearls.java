package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;

/** BP-03: three rounded, offset lobes in a chain; no resource or quality random source. */
final class CataloguePearls {
    private CataloguePearls() { }

    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        boolean split = variant == CatalogueTerraces.Variant.PEARLS_SPLIT;
        boolean satellite = variant == CatalogueTerraces.Variant.PEARLS_SATELLITE;
        var air = new LinkedHashSet<BlockPos>();
        for (int x=2;x<=12;x++) for (int z=2;z<=12;z++) {
            int dx=x-7, dz=z-7;
            int low=(dx+dz)*(dx+dz)*22+(dx-dz)*(dx-dz)*90;
            int high=(dx-dz)*(dx-dz)*22+(dx+dz)*(dx+dz)*90;
            int mid=(2*x-15)*(2*x-15)*85+(2*z-17)*(2*z-17)*121;
            if (low<=1980) {
                column(air,x,z,3,5);
                if (low<=500) air.add(new BlockPos(x,6,z));
            }
            if (high<=1980 && !(satellite && x==10 && z>=8)) column(air,x,z,15,17);
            if (mid<=10285) {
                if (!split) {
                    column(air,x,z,9,11);
                    if (mid<=2600) air.add(new BlockPos(x,12,z));
                } else if (x<=6 || x==7 && z<=5) {
                    column(air,x,z,9,11);
                    if (mid<=2600) air.add(new BlockPos(x,12,z));
                } else if (x>=8 && z<=9 && !(x==8 && z<=5)) {
                    column(air,x,z,10,12);
                    if (mid<=2600) air.add(new BlockPos(x,13,z));
                }
            }
        }
        box(air,6,7,1,7,15,17);
        // These are the only links between the three lobes, on opposite sides.
        box(air,11,12,6,7,9,17);
        box(air,2,3,6,7,3,11);
        if (split) {
            box(air,6,7,4,5,9,11); // unchanged north-middle node front
            box(air,7,8,6,7,10,12);
            box(air,8,12,8,9,10,12);
            box(air,8,9,10,10,10,12);
            box(air,8,11,11,12,9,11); // unchanged east-middle node front
        }
        if (satellite) {
            box(air,11,12,8,11,15,17);
            column(air,11,12,15,17);
        }
        var blocks = CatalogueTerraces.finish(center,air);
        var base = CatalogueTerraces.base(center);
        if (split) {
            for (int z=6;z<=7;z++) {
                blocks.put(base.offset(11,9,z),Blocks.STONE.defaultBlockState());
                stair(blocks,base.offset(7,9,z),Direction.EAST);
            }
            for (int x=8;x<=9;x++) stair(blocks,base.offset(x,9,10),Direction.NORTH);
        }
        return blocks;
    }

    private static void stair(Map<BlockPos,BlockState> blocks,BlockPos pos,Direction facing) {
        blocks.put(pos.below(),Blocks.STONE.defaultBlockState());
        blocks.put(pos,Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,facing));
    }
    private static void column(LinkedHashSet<BlockPos> air,int x,int z,int minY,int maxY) {
        for(int y=minY;y<=maxY;y++) air.add(new BlockPos(x,y,z));
    }
    private static void box(LinkedHashSet<BlockPos> air,int x1,int x2,int z1,int z2,int y1,int y2) {
        for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) column(air,x,z,y1,y2);
    }
}
