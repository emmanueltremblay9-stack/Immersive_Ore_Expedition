package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;

public final class OreLoadChamberBlockPlacementApplier {
    private static final int BLOCK_UPDATE_FLAGS = 2;

    public OreLoadChamberBlockPlacementResult apply(
            OreLoadChamberBlockPlacementPlan plan,
            PlacementTarget target
    ) {
        if (plan == null) {
            return OreLoadChamberBlockPlacementResult.skipped(
                    OreLoadChamberBlockPlacementResult.SkipReason.NULL_PLAN,
                    0
            );
        }

        int candidateCount = plan.oreTargets().size();
        if (!plan.placementReady()) {
            return OreLoadChamberBlockPlacementResult.skipped(
                    OreLoadChamberBlockPlacementResult.SkipReason.PLAN_NOT_READY,
                    candidateCount
            );
        }
        if (target == null) {
            return OreLoadChamberBlockPlacementResult.skipped(
                    OreLoadChamberBlockPlacementResult.SkipReason.TARGET_UNAVAILABLE,
                    candidateCount
            );
        }

        int placedCount = 0;
        EnumMap<OreLoadChamberBlockPlacementResult.SkipReason, Integer> skipCounts =
                new EnumMap<>(OreLoadChamberBlockPlacementResult.SkipReason.class);
        for (OreLoadChamberBlockPlacementPlan.OreBlockTarget oreTarget : plan.oreTargets()) {
            BlockPos pos = oreTarget.pos();
            ResourceRef resource = oreTarget.resource();
            if (!target.canWrite(pos)) {
                recordSkip(skipCounts, OreLoadChamberBlockPlacementResult.SkipReason.TARGET_OUTSIDE_WRITE_REGION);
                continue;
            }
            if (!target.hasPlacementResource(resource)) {
                recordSkip(skipCounts, OreLoadChamberBlockPlacementResult.SkipReason.TARGET_RESOURCE_MISSING);
                continue;
            }

            ReplacementCandidate candidate = target.replacementCandidate(pos);
            if (candidate == null || !OreLoadChamberReplacementRules.canReplace(
                    candidate.blockId(),
                    candidate.air(),
                    candidate.containsFluid(),
                    candidate.hasBlockEntity()
            )) {
                recordSkip(skipCounts, OreLoadChamberBlockPlacementResult.SkipReason.TARGET_NOT_REPLACEABLE);
                continue;
            }

            if (target.place(pos, resource)) {
                placedCount++;
            } else {
                recordSkip(skipCounts, OreLoadChamberBlockPlacementResult.SkipReason.WORLD_WRITE_FAILED);
            }
        }

        return OreLoadChamberBlockPlacementResult.completed(candidateCount, placedCount, skipCounts);
    }

    public OreLoadChamberBlockPlacementResult applyToWorld(
            OreLoadChamberBlockPlacementPlan plan,
            WorldGenLevel level
    ) {
        return apply(plan, level == null ? null : new WorldGenLevelPlacementTarget(level));
    }

    private static void recordSkip(
            EnumMap<OreLoadChamberBlockPlacementResult.SkipReason, Integer> skipCounts,
            OreLoadChamberBlockPlacementResult.SkipReason reason
    ) {
        skipCounts.merge(reason, 1, Integer::sum);
    }

    public interface PlacementTarget {
        boolean canWrite(BlockPos pos);

        boolean hasPlacementResource(ResourceRef resource);

        ReplacementCandidate replacementCandidate(BlockPos pos);

        boolean place(BlockPos pos, ResourceRef resource);
    }

    public record ReplacementCandidate(
            ResourceLocation blockId,
            boolean air,
            boolean containsFluid,
            boolean hasBlockEntity
    ) {
        public ReplacementCandidate {
            Objects.requireNonNull(blockId, "blockId");
        }
    }

    private record WorldGenLevelPlacementTarget(WorldGenLevel level) implements PlacementTarget {
        private WorldGenLevelPlacementTarget {
            Objects.requireNonNull(level, "level");
        }

        @Override
        public boolean canWrite(BlockPos pos) {
            return pos != null && level.ensureCanWrite(pos);
        }

        @Override
        public boolean hasPlacementResource(ResourceRef resource) {
            return resource != null
                    && resource.type() == ResourceType.BLOCK
                    && BuiltInRegistries.BLOCK.getOptional(resource.id()).isPresent();
        }

        @Override
        public ReplacementCandidate replacementCandidate(BlockPos pos) {
            if (pos == null) {
                return null;
            }
            BlockState state = level.getBlockState(pos);
            return new ReplacementCandidate(
                    BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                    state.isAir(),
                    !state.getFluidState().isEmpty(),
                    state.hasBlockEntity()
            );
        }

        @Override
        public boolean place(BlockPos pos, ResourceRef resource) {
            if (pos == null || resource == null || resource.type() != ResourceType.BLOCK) {
                return false;
            }
            Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(resource.id());
            return block.isPresent()
                    && level.setBlock(pos, block.orElseThrow().defaultBlockState(), BLOCK_UPDATE_FLAGS);
        }
    }
}
