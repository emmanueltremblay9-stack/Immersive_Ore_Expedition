package com.oblixorprime.ioe.budding;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

import java.util.Objects;

public final class GeOreRankedBuddingBlock extends Block {
    public static final int GROWTH_CHANCE = 5;
    public static final int DECAY_CHANCE = 12;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final String material;
    private final BuddingRank rank;

    public GeOreRankedBuddingBlock(Properties properties, String material, BuddingRank rank) {
        super(properties);
        this.material = Objects.requireNonNull(material, "material");
        this.rank = Objects.requireNonNull(rank, "rank");
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(GROWTH_CHANCE) != 0) {
            return;
        }

        Direction direction = Util.getRandom(DIRECTIONS, randomSource);
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block newCluster = null;
        Block smallBud = requireGeOreBlock("small_" + material + "_bud");
        Block mediumBud = requireGeOreBlock("medium_" + material + "_bud");
        Block largeBud = requireGeOreBlock("large_" + material + "_bud");

        if (canClusterGrowAtState(targetState)) {
            newCluster = smallBud;
        } else if (targetState.is(smallBud)
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = mediumBud;
        } else if (targetState.is(mediumBud)
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = largeBud;
        } else if (targetState.is(largeBud)
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = requireGeOreBlock(material + "_cluster");
        }

        if (newCluster == null) {
            return;
        }

        BlockState newClusterState = newCluster.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(
                        AmethystClusterBlock.WATERLOGGED,
                        targetState.getFluidState().getType() == Fluids.WATER
                );
        level.setBlockAndUpdate(targetPos, newClusterState);

        if (!rank.canGrowthDegrade() || randomSource.nextInt(DECAY_CHANCE) != 0) {
            return;
        }

        ResourceLocation degradationTarget = rank.degradedRank()
                .map(nextRank -> IoeBuddingBlocks.blockId(material, nextRank))
                .orElseGet(() -> ResourceLocation.fromNamespaceAndPath("geore", material + "_block"));
        Block degradedBlock = BuiltInRegistries.BLOCK.getOptional(degradationTarget)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing GeOre rank degradation target: " + degradationTarget
                ));
        level.setBlockAndUpdate(pos, degradedBlock.defaultBlockState());
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

    private static Block requireGeOreBlock(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("geore", path);
        return BuiltInRegistries.BLOCK.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("Missing required GeOre block: " + id));
    }
}
