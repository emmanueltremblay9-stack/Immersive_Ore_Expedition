package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.worldgen.LoadedStructureCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Native metadata only: neither player-build detection nor an external claims API. */
final class NetherNativeProtection {
    private NetherNativeProtection() { }
    static boolean protectedAt(ServerLevel level, BlockPos pos, BlockState state) {
        return state.hasBlockEntity() || LoadedStructureCollision.blocksPlacement(level,
                new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()));
    }
}
