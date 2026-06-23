package com.oblixorprime.ioe.ieip;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public final class IeMineralOutcropFeature {
    public boolean placeOutcropClue(WorldGenLevel level, BlockPos nearSurfacePos) {
        // TODO Codex: query/derive IE mineral data safely if API exists.
        // Place only small clue boulders. Do not render full deposits.
        return false;
    }
}
