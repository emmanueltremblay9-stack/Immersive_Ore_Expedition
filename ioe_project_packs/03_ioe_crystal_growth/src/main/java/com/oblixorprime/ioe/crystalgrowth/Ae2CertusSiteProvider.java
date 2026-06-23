package com.oblixorprime.ioe.crystalgrowth;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public final class Ae2CertusSiteProvider implements CrystalGrowthSiteProvider {
    @Override public boolean isAvailable() { return false; /* TODO Codex: check ModList for ae2 */ }
    @Override public boolean canGenerateAt(WorldGenLevel level, BlockPos anchorPos) { return false; }
    @Override public boolean generate(WorldGenLevel level, BlockPos anchorPos) { return false; }
}
