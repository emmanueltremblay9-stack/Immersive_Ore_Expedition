package com.oblixorprime.ioe.crystalgrowth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public final class GeOreSiteProvider implements CrystalGrowthSiteProvider {
    @Override public boolean isAvailable() { return false; /* TODO Codex: optional GeOre detection */ }
    @Override public boolean canGenerateAt(WorldGenLevel level, BlockPos anchorPos) { return false; }
    @Override public boolean generate(WorldGenLevel level, BlockPos anchorPos) { return false; }
}
