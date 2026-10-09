package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Canonical resource budget before registry resolution or block placement.
 * Ore counts exclude the budding hearts. This plan does not reserve IE/IP deposits.
 */
public final class BuddingSitePlan {
    private final SiteQuality quality;
    private final List<BuddingRank> nodeRanks;
    private final int oreBlocksPerNode;
    private final int residualOreBlocks;

    private BuddingSitePlan(SiteQuality quality, List<BuddingRank> nodeRanks,
                            int oreBlocksPerNode, int residualOreBlocks) {
        this.quality = quality;
        this.nodeRanks = List.copyOf(nodeRanks);
        this.oreBlocksPerNode = oreBlocksPerNode;
        this.residualOreBlocks = residualOreBlocks;
    }

    /** Selects quality with the canonical weights, then plans that site's nodes. */
    public static BuddingSitePlan roll(RandomSource random, int dryOreBlocks) {
        Objects.requireNonNull(random, "random");
        validateDryOreBlocks(dryOreBlocks);
        return forQuality(SiteQualityRoll.DEFAULT.roll(random), random, dryOreBlocks);
    }

    /**
     * Plans an already selected quality. The caller supplies the single approved uniform
     * DRY pocket draw (0..5), independently derived with DryPocketRoll.
     * Only Motherlode draws a Flawless chance, once per site; a success then selects a node.
     */
    public static BuddingSitePlan forQuality(SiteQuality quality, RandomSource random, int dryOreBlocks) {
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(random, "random");
        validateDryOreBlocks(dryOreBlocks);
        BuddingSitePlan ordinary = ordinaryPlan(quality, dryOreBlocks);
        if (quality != SiteQuality.MOTHERLODE || random.nextInt(10_000) >= 777) {
            return ordinary;
        }
        List<BuddingRank> ranks = new ArrayList<>(ordinary.nodeRanks);
        ranks.set(random.nextInt(ranks.size()), BuddingRank.FLAWLESS);
        return new BuddingSitePlan(quality, ranks, ordinary.oreBlocksPerNode, 0);
    }

    /**
     * Rebuilds a strictly lower quality without a new lottery. Any Flawless selection is
     * discarded. The caller's separate deposit reservation/compensation remains authoritative.
     */
    public BuddingSitePlan downgradeTo(SiteQuality lowerQuality, int dryOreBlocks) {
        Objects.requireNonNull(lowerQuality, "lowerQuality");
        validateDryOreBlocks(dryOreBlocks);
        if (!isLowerQuality(lowerQuality)) {
            throw new IllegalArgumentException("Fallback must select a strictly lower site quality");
        }
        return ordinaryPlan(lowerQuality, dryOreBlocks);
    }

    private boolean isLowerQuality(SiteQuality candidate) {
        var lower = quality.directLower();
        while (lower.isPresent()) {
            if (lower.orElseThrow() == candidate) {
                return true;
            }
            lower = lower.orElseThrow().directLower();
        }
        return false;
    }

    private static BuddingSitePlan ordinaryPlan(SiteQuality quality, int dryOreBlocks) {
        return switch (quality) {
            case DRY -> new BuddingSitePlan(quality, List.of(), 0, dryOreBlocks);
            case POOR -> productive(quality, 3, 4, BuddingRank.DAMAGED);
            case NORMAL -> productive(quality, 4, 5, BuddingRank.CHIPPED);
            case RICH -> productive(quality, 5, 6, BuddingRank.FLAWED);
            case MOTHERLODE -> productive(quality, 7, 7, BuddingRank.FLAWED);
        };
    }

    private static BuddingSitePlan productive(SiteQuality quality, int nodes, int orePerNode, BuddingRank rank) {
        return new BuddingSitePlan(quality, Collections.nCopies(nodes, rank), orePerNode, 0);
    }

    private static void validateDryOreBlocks(int count) {
        if (count < 0 || count > 5) {
            throw new IllegalArgumentException("DRY residual ore count must be in [0, 5]");
        }
    }

    public SiteQuality quality() {
        return quality;
    }

    public List<BuddingRank> nodeRanks() {
        return nodeRanks;
    }

    public int oreBlocksPerNode() {
        return oreBlocksPerNode;
    }

    public int residualOreBlocks() {
        return residualOreBlocks;
    }

    public int totalOreBlocks() {
        return nodeRanks.size() * oreBlocksPerNode + residualOreBlocks;
    }
}
