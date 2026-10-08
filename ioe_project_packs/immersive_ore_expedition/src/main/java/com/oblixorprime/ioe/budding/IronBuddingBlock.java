package com.oblixorprime.ioe.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

/** Original registry-based implementation of the growth rules verified in AE2 19.2.17. */
public final class IronBuddingBlock extends Block {
    private final BuddingRank rank;

    public IronBuddingBlock(BuddingRank rank, Properties properties) {
        super(properties);
        this.rank = java.util.Objects.requireNonNull(rank, "rank");
    }

    public BuddingRank rank() {
        return rank;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        Direction direction = Direction.values()[random.nextInt(6)];
        BlockPos target = pos.relative(direction);
        Optional<BlockState> next = nextGrowth(level.getBlockState(target), direction);
        if (next.isEmpty()) {
            return;
        }
        level.setBlockAndUpdate(target, next.orElseThrow());
        if (rank.canGrowthDegrade() && random.nextInt(12) == 0) {
            Block replacement = rank.degradedRank().map(IoeIronBuddingBlocks::block).orElse(Blocks.IRON_BLOCK);
            level.setBlockAndUpdate(pos, replacement.defaultBlockState());
        }
    }

    /** Missing or incompatible GeOre products stop growth; never substitute another mineral. */
    static Optional<BlockState> nextGrowth(BlockState neighbor, Direction direction) {
        List<Block> products = BuddingResourceFamily.GEORE_IRON.growthProductIds().stream()
                .map(id -> BuiltInRegistries.BLOCK.getOptional(id).orElse(null)).toList();
        if (products.stream().anyMatch(block -> block == null
                || !block.defaultBlockState().hasProperty(AmethystClusterBlock.FACING)
                || !block.defaultBlockState().hasProperty(AmethystClusterBlock.WATERLOGGED))) {
            return Optional.empty();
        }
        int next = -1;
        if (neighbor.isAir() || neighbor.is(Blocks.WATER) && neighbor.getFluidState().getAmount() == 8) {
            next = 0;
        } else {
            for (int stage = 0; stage < products.size() - 1; stage++) {
                if (neighbor.is(products.get(stage)) && neighbor.getValue(AmethystClusterBlock.FACING) == direction) {
                    next = stage + 1;
                    break;
                }
            }
        }
        if (next < 0) {
            return Optional.empty();
        }
        return Optional.of(products.get(next).defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, neighbor.getFluidState().getType() == Fluids.WATER));
    }
}
