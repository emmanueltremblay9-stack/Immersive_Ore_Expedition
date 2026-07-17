package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

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
            }
        } catch (RuntimeException | LinkageError failure) {
            return abortPartialPlacement(level, plan, previousStates, failure);
        }
        return Optional.of(new AppliedPlan(plan, previousStates, true));
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

        private AppliedPlan(
                ExpeditionSiteBlockPlan plan,
                Map<BlockPos, BlockState> previousStates,
                boolean active
        ) {
            this.plan = Objects.requireNonNull(plan, "plan");
            this.previousStates = Map.copyOf(Objects.requireNonNull(previousStates, "previousStates"));
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
            for (Map.Entry<BlockPos, BlockState> previous : previousStates.entrySet()) {
                try {
                    BlockPos pos = previous.getKey();
                    BlockState target = plan.blocks().get(pos);
                    BlockState current = level.getBlockState(pos);
                    if (current.equals(previous.getValue()) || !current.equals(target)) {
                        continue;
                    }
                    boolean changed = level.setBlock(pos, previous.getValue(), BLOCK_UPDATE_FLAGS);
                    if (!changed && !level.getBlockState(pos).equals(previous.getValue())) {
                        restored = false;
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
