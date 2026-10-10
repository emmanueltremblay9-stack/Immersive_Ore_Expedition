package com.oblixorprime.ioe.budding;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

/** Keeps the established Iron class and registry identities while sharing the family implementation. */
public final class IronBuddingBlock extends GeOreBuddingBlock {
    public IronBuddingBlock(BuddingRank rank, Properties properties) {
        super(BuddingResourceFamily.GEORE_IRON, rank, properties);
    }

    static Optional<BlockState> nextGrowth(BlockState neighbor, Direction direction) {
        return nextGrowth(BuddingResourceFamily.GEORE_IRON, neighbor, direction);
    }
}
