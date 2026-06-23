package com.oblixorprime.ioe.crystalgrowth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public interface CrystalGrowthSiteProvider {
    boolean isAvailable();
    boolean canGenerateAt(WorldGenLevel level, BlockPos anchorPos);
    boolean generate(WorldGenLevel level, BlockPos anchorPos);
}
