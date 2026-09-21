package com.oblixorprime.ioe.budding;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BuddingRankResolution(
        Status status,
        BuddingResourceFamily family,
        BuddingRank rank,
        Optional<Block> buddingBlock,
        Optional<ResourceLocation> buddingBlockId,
        ResourceLocation storageBlockId,
        List<ResourceLocation> growthProductIds,
        Optional<ResourceLocation> degradationTargetId,
        BuddingRank maximumRestorableRank,
        boolean canGrowthDegrade,
        String detail
) {
    public BuddingRankResolution {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(buddingBlock, "buddingBlock");
        Objects.requireNonNull(buddingBlockId, "buddingBlockId");
        Objects.requireNonNull(storageBlockId, "storageBlockId");
        growthProductIds = List.copyOf(growthProductIds);
        Objects.requireNonNull(degradationTargetId, "degradationTargetId");
        Objects.requireNonNull(maximumRestorableRank, "maximumRestorableRank");
        Objects.requireNonNull(detail, "detail");

        if (status == Status.SUPPORTED && (buddingBlock.isEmpty() || buddingBlockId.isEmpty())) {
            throw new IllegalArgumentException("A supported resolution requires a registered budding block");
        }
        if (rank == BuddingRank.FLAWLESS && canGrowthDegrade) {
            throw new IllegalArgumentException("Canonical FLAWLESS cannot growth-degrade");
        }
    }

    public static BuddingRankResolution supported(
            BuddingResourceFamily family,
            BuddingRank rank,
            Block buddingBlock,
            ResourceLocation buddingBlockId,
            ResourceLocation storageBlockId,
            List<ResourceLocation> growthProductIds,
            Optional<ResourceLocation> degradationTargetId
    ) {
        return new BuddingRankResolution(
                Status.SUPPORTED,
                family,
                rank,
                Optional.of(buddingBlock),
                Optional.of(buddingBlockId),
                storageBlockId,
                growthProductIds,
                degradationTargetId,
                BuddingRank.MAXIMUM_RESTORABLE,
                rank.canGrowthDegrade(),
                "Exact functional rank is registered"
        );
    }

    public static BuddingRankResolution unsupported(
            BuddingResourceFamily family,
            BuddingRank rank,
            Optional<ResourceLocation> observedNativeBuddingId,
            String detail
    ) {
        return new BuddingRankResolution(
                Status.RANK_UNSUPPORTED,
                family,
                rank,
                Optional.empty(),
                observedNativeBuddingId,
                family.storageBlockId(),
                family.growthProductIds(),
                Optional.empty(),
                BuddingRank.MAXIMUM_RESTORABLE,
                false,
                detail
        );
    }

    public boolean supported() {
        return status == Status.SUPPORTED;
    }

    public enum Status {
        SUPPORTED,
        RANK_UNSUPPORTED
    }
}
