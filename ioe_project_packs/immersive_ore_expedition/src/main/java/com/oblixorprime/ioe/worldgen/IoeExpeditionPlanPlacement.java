package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.domum.DomumOrnamentumCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Applies an expedition plan with an in-memory compensation journal. Pending sites retain the journal until the
 * locator and any required IE deposit have both been confirmed.
 */
final class IoeExpeditionPlanPlacement {
    private static final int BLOCK_UPDATE_FLAGS = 2;

    private IoeExpeditionPlanPlacement() {
    }

    static Optional<AppliedPlan> apply(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(plan, "plan");
        try {
            // Staging does not freeze native structure metadata. Recheck the existing camp policy for
            // every application, including lower-tier fallbacks, before reading/writing terrain.
            if (plan.requestedFeatureId().equals(IoeWorldgenFeatureKeys.MINER_CAMP)
                    && ExpeditionSiteFeature.collidesWithStructure(level, plan)) {
                return Optional.empty();
            }
            for (BlockPos pos : plan.blocks().keySet()) {
                if (!canWrite(level, pos)) {
                    return Optional.empty();
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            return Optional.empty();
        }

        LinkedHashMap<BlockPos, BlockState> previousStates = new LinkedHashMap<>();
        try {
            for (Map.Entry<BlockPos, BlockState> placement : plan.blocks().entrySet()) {
                BlockPos pos = placement.getKey();
                BlockState target = placement.getValue();
                if (!canWrite(level, pos)) {
                    return abortPartialPlacement(level, plan, previousStates, null);
                }
                previousStates.put(pos.immutable(), level.getBlockState(pos));
                boolean changed = level.setBlock(pos, target, BLOCK_UPDATE_FLAGS);
                if (!changed && !level.getBlockState(pos).equals(target)) {
                    return abortPartialPlacement(level, plan, previousStates, null);
                }
                ExpeditionBlockEntityPayload payload = plan.blockEntityPayloads().get(pos);
                if (payload != null && !applyPayload(level, pos, payload)) {
                    return abortPartialPlacement(level, plan, previousStates, null);
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            return abortPartialPlacement(level, plan, previousStates, failure);
        }
        return Optional.of(new AppliedPlan(plan, previousStates, true));
    }

    private static boolean applyPayload(
            WorldGenLevel level,
            BlockPos pos,
            ExpeditionBlockEntityPayload payload
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        if (payload.hasLootTable()) {
            if (!(blockEntity instanceof RandomizableContainerBlockEntity container)) {
                return false;
            }
            ResourceKey<LootTable> lootTable = ResourceKey.create(Registries.LOOT_TABLE, payload.lootTable());
            container.setLootTable(lootTable);
            container.setLootTableSeed(payload.lootSeed());
            container.setChanged();
            return true;
        }
        return payload.hasMaterialBlocks()
                && DomumOrnamentumCompat.applyMaterialPayload(blockEntity, payload.materialBlocks());
    }

    private static Optional<AppliedPlan> abortPartialPlacement(
            WorldGenLevel level,
            ExpeditionSiteBlockPlan plan,
            Map<BlockPos, BlockState> previousStates,
            Throwable cause
    ) {
        if (previousStates.isEmpty()) {
            return Optional.empty();
        }
        AppliedPlan partial = new AppliedPlan(plan, previousStates, true);
        if (!partial.rollback(level)) {
            throw new PlacementCompensationException(partial, cause);
        }
        return Optional.empty();
    }

    private static boolean canWrite(WorldGenLevel level, BlockPos pos) {
        if (!level.ensureCanWrite(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (!existing.getFluidState().isEmpty() || existing.hasBlockEntity()) {
            return false;
        }
        return existing.isAir() || OreLoadChamberReplacementRules.canReplace(existing);
    }

    static final class AppliedPlan {
        private final ExpeditionSiteBlockPlan plan;
        private final Map<BlockPos, BlockState> previousStates;
        private final AtomicBoolean active;
        private final Map<BlockPos, BlockState> expectedRollbackStates = new LinkedHashMap<>();
        private final java.util.Set<BlockPos> observedRollbackOwnership = new java.util.HashSet<>();

        private AppliedPlan(
                ExpeditionSiteBlockPlan plan,
                Map<BlockPos, BlockState> previousStates,
                boolean active
        ) {
            this.plan = Objects.requireNonNull(plan, "plan");
            this.previousStates = java.util.Collections.unmodifiableMap(
                    new LinkedHashMap<>(Objects.requireNonNull(previousStates, "previousStates")));
            this.active = new AtomicBoolean(active);
        }

        ExpeditionSiteBlockPlan plan() {
            return plan;
        }

        void accept() {
            active.set(false);
        }

        boolean rollback(WorldGenLevel level) {
            Objects.requireNonNull(level, "level");
            if (!active.get()) {
                return true;
            }
            boolean restored = true;
            // Take exact ownership before any restoration can update a neighbour's derived state.
            // A pre-existing foreign edit is never admitted into this journal afterwards.
            var expectedOwnedStates = expectedRollbackStates;
            for (var previous : previousStates.entrySet()) {
                if (observedRollbackOwnership.contains(previous.getKey())) continue;
                try {
                    var current = level.getBlockState(previous.getKey());
                    var target = plan.blocks().get(previous.getKey());
                    if (!current.equals(previous.getValue()) && current.equals(target))
                        expectedOwnedStates.put(previous.getKey(), target);
                    observedRollbackOwnership.add(previous.getKey());
                } catch (RuntimeException | LinkageError failure) {
                    restored = false;
                }
            }
            // Undo in reverse write order: restore neighbor-dependent blocks before their supports.
            // Only our own synchronous native stair-shape updates can advance an owned expectation.
            var restorationOrder = new java.util.ArrayList<>(previousStates.entrySet());
            java.util.Collections.reverse(restorationOrder);
            for (Map.Entry<BlockPos, BlockState> previous : restorationOrder) {
                try {
                    BlockPos pos = previous.getKey();
                    BlockState target = expectedOwnedStates.get(pos);
                    if (target == null) continue;
                    BlockState current = level.getBlockState(pos);
                    if (current.equals(previous.getValue()) || !current.equals(target)) {
                        continue;
                    }
                    ExpeditionBlockEntityPayload payload = plan.blockEntityPayloads().get(pos);
                    if (payload != null && payload.hasLootTable()
                            && level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
                        // Removing a chest normally unpacks and drops its loot. This container belongs to
                        // the uncommitted plan (pre-existing block entities are rejected by canWrite).
                        container.setLootTable(null);
                        container.clearContent();
                        container.setChanged();
                    }
                    var neighbourStairs = new LinkedHashMap<BlockPos, BlockState>();
                    for (Direction direction : java.util.List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                        var neighbour = pos.relative(direction);
                        var expected = expectedOwnedStates.get(neighbour);
                        if (expected != null && expected.getBlock() instanceof StairBlock
                                && level.getBlockState(neighbour).equals(expected))
                            neighbourStairs.put(neighbour, expected);
                    }
                    boolean changed = level.setBlock(pos, previous.getValue(), BLOCK_UPDATE_FLAGS);
                    if (!changed && !level.getBlockState(pos).equals(previous.getValue())) {
                        restored = false;
                    }
                    for (var neighbour : neighbourStairs.entrySet()) {
                        var after = level.getBlockState(neighbour.getKey());
                        // Never absorb a replacement, facing/half/water change, or an earlier foreign edit.
                        if (after.getBlock() instanceof StairBlock
                                && neighbour.getValue().setValue(StairBlock.SHAPE, after.getValue(StairBlock.SHAPE)).equals(after))
                            expectedOwnedStates.put(neighbour.getKey(), after);
                    }
                } catch (RuntimeException | LinkageError failure) {
                    restored = false;
                }
            }
            if (restored) {
                active.set(false);
            }
            return restored;
        }
    }

    static final class PlacementCompensationException extends IllegalStateException {
        private final AppliedPlan appliedPlan;

        private PlacementCompensationException(AppliedPlan appliedPlan, Throwable cause) {
            super("Could not fully compensate a partially applied expedition plan", cause);
            this.appliedPlan = Objects.requireNonNull(appliedPlan, "appliedPlan");
        }

        AppliedPlan appliedPlan() {
            return appliedPlan;
        }
    }
}
