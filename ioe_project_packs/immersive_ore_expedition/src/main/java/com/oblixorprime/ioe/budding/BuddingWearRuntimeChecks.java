package com.oblixorprime.ioe.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import java.util.ArrayList;
import java.util.List;

/** Registry-backed growth and wear; loaded ExtendedAE is the optional runtime oracle. */
final class BuddingWearRuntimeChecks {
    private BuddingWearRuntimeChecks() { }
    static void check(GameTestHelper helper, BuddingResourceFamily family) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos nativePos = helper.absolutePos(new BlockPos(10, 4, 4));
        var level = helper.getLevel();
        for (BuddingRank rank : BuddingRank.values()) {
            Block heart = IoeGeOreBuddingBlocks.block(family, rank);
            Block nativeHeart = BuddingResourceFamily.ENTROIZED_FLUIX.observedNativeBuddingId(rank)
                    .flatMap(BuiltInRegistries.BLOCK::getOptional).orElse(null);
            for (Direction direction : Direction.values()) for (int stage = 0; stage < 8; stage++) {
                for (int growth : new int[]{0, 1}) for (int decay : new int[]{0, 1}) {
                    level.setBlockAndUpdate(pos, heart.defaultBlockState());
                    for (Direction face : Direction.values()) level.setBlockAndUpdate(pos.relative(face), Blocks.STONE.defaultBlockState());
                    BlockState before = neighbor(family, stage, direction);
                    level.setBlockAndUpdate(pos.relative(direction), before);
                    var random = new Draws(growth, direction.ordinal(), decay);
                    heart.defaultBlockState().randomTick(level, pos, random);
                    boolean grows = growth == 0 && (stage < 4 || stage == 6);
                    List<Integer> bounds = growth != 0 ? List.of(3)
                            : grows && rank != BuddingRank.FLAWLESS ? List.of(3, 6, 10) : List.of(3, 6);
                    helper.assertTrue(random.bounds.equals(bounds), "Wrong random draws: " + family + "/" + rank + "/" + stage);
                    Block expected = heart;
                    if (grows && decay == 0) expected = switch (rank) {
                        case FULLY -> IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED);
                        case FLAWED -> IoeGeOreBuddingBlocks.block(family, BuddingRank.CHIPPED);
                        case CHIPPED -> IoeGeOreBuddingBlocks.block(family, BuddingRank.DAMAGED);
                        case DAMAGED -> BuiltInRegistries.BLOCK.getOptional(family.storageBlockId()).orElseThrow();
                        case FLAWLESS -> heart;
                    };
                    helper.assertTrue(level.getBlockState(pos).is(expected), "Wrong wear transition: " + family + "/" + rank);
                    BlockState after = level.getBlockState(pos.relative(direction));
                    if (grows) helper.assertTrue(after.is(BuiltInRegistries.BLOCK.getOptional(family.growthProductIds().get(stage == 6 ? 0 : stage)).orElseThrow())
                                    && after.getValue(AmethystClusterBlock.FACING) == direction
                                    && after.getValue(AmethystClusterBlock.WATERLOGGED) == (stage == 6), "Wrong growth product/orientation/waterlogging");
                    else helper.assertTrue(after.equals(before), "Blocked or mature crystal changed");
                    if (nativeHeart != null) {
                        level.setBlockAndUpdate(nativePos, nativeHeart.defaultBlockState());
                        for (Direction face : Direction.values()) level.setBlockAndUpdate(nativePos.relative(face), Blocks.STONE.defaultBlockState());
                        level.setBlockAndUpdate(nativePos.relative(direction), neighbor(BuddingResourceFamily.ENTROIZED_FLUIX, stage, direction));
                        var nativeRandom = new Draws(growth, direction.ordinal(), decay);
                        nativeHeart.defaultBlockState().randomTick(level, nativePos, nativeRandom);
                        helper.assertTrue(random.bounds.equals(nativeRandom.bounds), "Loaded ExtendedAE draw order differs");
                        Block nativeExpected = nativeHeart;
                        if (grows && decay == 0) nativeExpected = rank == BuddingRank.DAMAGED
                                ? BuiltInRegistries.BLOCK.getOptional(BuddingResourceFamily.ENTROIZED_FLUIX.storageBlockId()).orElseThrow()
                                : BuiltInRegistries.BLOCK.getOptional(BuddingResourceFamily.ENTROIZED_FLUIX
                                        .observedNativeBuddingId(((GeOreBuddingBlock) expected).rank()).orElseThrow()).orElseThrow();
                        helper.assertTrue(level.getBlockState(nativePos).is(nativeExpected), "Loaded ExtendedAE phase differs");
                        BlockState nativeAfter = level.getBlockState(nativePos.relative(direction));
                        helper.assertTrue(after.getValues().equals(nativeAfter.getValues()), "Loaded ExtendedAE properties differ");
                        if (grows) helper.assertTrue(nativeAfter.is(BuiltInRegistries.BLOCK.getOptional(
                                BuddingResourceFamily.ENTROIZED_FLUIX.growthProductIds().get(stage == 6 ? 0 : stage)).orElseThrow()), "Loaded ExtendedAE stage differs");
                    }
                }
            }
        }
    }
    private static BlockState neighbor(BuddingResourceFamily family, int stage, Direction direction) {
        if (stage == 0) return Blocks.AIR.defaultBlockState();
        if (stage == 5) return Blocks.STONE.defaultBlockState();
        if (stage == 6) return Blocks.WATER.defaultBlockState();
        return BuiltInRegistries.BLOCK.getOptional(family.growthProductIds().get(stage == 7 ? 0 : stage - 1)).orElseThrow().defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, stage == 7 ? direction.getOpposite() : direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, false);
    }
    private static final class Draws extends LegacyRandomSource {
        private final int[] draws;
        private int index;
        private final List<Integer> bounds = new ArrayList<>();
        Draws(int... draws) { super(0); this.draws = draws; }
        @Override public int nextInt(int bound) {
            bounds.add(bound);
            if (index >= draws.length) throw new AssertionError("Unexpected random draw");
            return Math.floorMod(draws[index++], bound);
        }
    }
}
