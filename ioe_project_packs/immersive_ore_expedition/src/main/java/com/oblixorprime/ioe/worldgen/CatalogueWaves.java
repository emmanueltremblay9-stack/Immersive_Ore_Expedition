package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashMap;
import java.util.Map;

/** BP-33: floor and roof follow two crossed undulations, with no internal room partition. */
final class CatalogueWaves {
    private CatalogueWaves() { }
    static Map<BlockPos,BlockState> geometry(BlockPos center,CatalogueTerraces.Variant variant) {
        return CatalogueSaddle.compose(center,heights(variant),false);
    }
    static Map<BlockPos,Integer> heights(CatalogueTerraces.Variant variant) {
        // Two-cell crests give opposed slopes their own oriented stair edges.
        int[] wave={0,1,1,0,-1,-1,-1,0,1,1,0};
        var out=new LinkedHashMap<BlockPos,Integer>();
        for(int x=2;x<=12;x++) for(int z=3;z<=13;z++) {
            if(!CatalogueSaddle.surfaceCell(x,z) || x==2 && z>=6 && z<=7
                    || z==3 && x>=6 && x<=8) continue;
            int foot=9+wave[x-2]+wave[z-3];
            if(x>=7 && x<=8 && z<=5 || x<=6 && z>=10 && z<=11) foot=9;
            if(variant==CatalogueTerraces.Variant.WAVES_NOTCH && x>=4 && x<=5 && z>=6 && z<=7) foot=8;
            if(variant==CatalogueTerraces.Variant.WAVES_RIDGE && x==7 && z>=7 && z<=9) foot=8;
            out.put(new BlockPos(x,0,z),foot);
        }
        return out;
    }
}
