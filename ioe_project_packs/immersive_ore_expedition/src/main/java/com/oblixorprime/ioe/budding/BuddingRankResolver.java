package com.oblixorprime.ioe.budding;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BuddingRankResolver {
    private static final String EXTENDED_AE_INCOMPLETE_DETAIL =
            "RANK_UNSUPPORTED: ExtendedAE 2.2.33 degrades fully -> mostly -> half -> hardly "
                    + "-> ae2:quartz_block, so the family has no non-degrading canonical FLAWLESS rank";

    private BuddingRankResolver() {
    }

    public static BuddingRankResolution resolve(BuddingResourceFamily family, BuddingRank rank) {
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(rank, "rank");

        if (family.kind() == BuddingResourceFamily.Kind.EXTENDED_AE) {
            return BuddingRankResolution.unsupported(
                    family,
                    rank,
                    family.observedNativeBuddingId(rank),
                    EXTENDED_AE_INCOMPLETE_DETAIL
            );
        }

        if (!ModList.get().isLoaded(family.dependencyModId())) {
            return BuddingRankResolution.unsupported(
                    family,
                    rank,
                    Optional.empty(),
                    "Required mod is not loaded: " + family.dependencyModId()
            );
        }

        ResourceLocation buddingBlockId = switch (family.kind()) {
            case GEORE -> IoeBuddingBlocks.blockId(family.key(), rank);
            case CERTUS -> family.observedNativeBuddingId(rank).orElseThrow();
            case EXTENDED_AE -> throw new IllegalStateException("ExtendedAE is handled as an incomplete family");
        };

        List<ResourceLocation> requiredIds = new ArrayList<>(family.growthProductIds());
        requiredIds.add(family.storageBlockId());
        requiredIds.add(buddingBlockId);
        List<ResourceLocation> missingIds = requiredIds.stream()
                .filter(id -> BuiltInRegistries.BLOCK.getOptional(id).isEmpty())
                .toList();
        if (!missingIds.isEmpty()) {
            return BuddingRankResolution.unsupported(
                    family,
                    rank,
                    Optional.of(buddingBlockId),
                    "Required registered blocks are missing: " + missingIds
            );
        }

        Block buddingBlock = BuiltInRegistries.BLOCK.getOptional(buddingBlockId).orElseThrow();
        return BuddingRankResolution.supported(
                family,
                rank,
                buddingBlock,
                buddingBlockId,
                family.storageBlockId(),
                family.growthProductIds(),
                degradationTargetId(family, rank)
        );
    }

    private static Optional<ResourceLocation> degradationTargetId(
            BuddingResourceFamily family,
            BuddingRank rank
    ) {
        if (!rank.canGrowthDegrade()) {
            return Optional.empty();
        }
        return rank.degradedRank()
                .map(nextRank -> switch (family.kind()) {
                    case GEORE -> IoeBuddingBlocks.blockId(family.key(), nextRank);
                    case CERTUS -> family.observedNativeBuddingId(nextRank).orElseThrow();
                    case EXTENDED_AE -> throw new IllegalStateException(
                            "ExtendedAE cannot produce a supported canonical degradation target"
                    );
                })
                .or(() -> Optional.of(family.storageBlockId()));
    }
}
