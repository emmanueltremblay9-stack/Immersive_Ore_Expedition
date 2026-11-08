package com.oblixorprime.ioe.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** Permanent Entro heart; native Fully retains its original wearing behavior. */
public final class EntroFlawlessBuddingBlock extends GeOreBuddingBlock {
    public EntroFlawlessBuddingBlock(Properties properties) {
        super(BuddingResourceFamily.ENTROIZED_FLUIX, BuddingRank.FLAWLESS, properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        Direction direction = Direction.values()[random.nextInt(6)];
        BlockPos target = pos.relative(direction);
        if (!level.hasChunkAt(target)) return;
        nextGrowth(family(), level.getBlockState(target), direction)
                .ifPresent(next -> level.setBlockAndUpdate(target, next));
    }
}
