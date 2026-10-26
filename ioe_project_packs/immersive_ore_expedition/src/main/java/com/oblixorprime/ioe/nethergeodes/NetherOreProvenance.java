package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** An exact sanitation exemption, never a chunk-write capability or retrogen instruction. */
public final class NetherOreProvenance {
    private NetherOreProvenance() { }
    static boolean isResource(BlockState state) {
        return state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.ANCIENT_DEBRIS);
    }
    public static boolean preserves(ServerLevel level, BlockPos pos, BlockState state) {
        return level.dimension().equals(Level.NETHER) && isResource(state)
                && level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME).preserves(pos, state);
    }
}
