package com.oblixorprime.ioe.worldgen;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record OreLoadChamberBlockPlacementResult(
        int candidateCount,
        int placedCount,
        int skippedCount,
        SkipReason skipReason,
        Map<SkipReason, Integer> skipCounts
) {
    public OreLoadChamberBlockPlacementResult {
        if (candidateCount < 0) {
            throw new IllegalArgumentException("candidateCount must not be negative");
        }
        if (placedCount < 0) {
            throw new IllegalArgumentException("placedCount must not be negative");
        }
        if (skippedCount < 0) {
            throw new IllegalArgumentException("skippedCount must not be negative");
        }
        if (placedCount + skippedCount != candidateCount) {
            throw new IllegalArgumentException("placed and skipped counts must match candidate count");
        }
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        skipCounts = copySkipCounts(skipCounts);
        int countedSkips = skipCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (countedSkips != skippedCount) {
            throw new IllegalArgumentException("skipCounts must match skippedCount");
        }
        if (skipReason == SkipReason.NONE && candidateCount == 0 && skippedCount == 0) {
            throw new IllegalArgumentException("empty results require a skip reason");
        }
        if (skipReason != SkipReason.NONE && placedCount > 0) {
            throw new IllegalArgumentException("plan-level skipped results must not place blocks");
        }
    }

    public static OreLoadChamberBlockPlacementResult skipped(SkipReason skipReason, int candidateCount) {
        Objects.requireNonNull(skipReason, "skipReason");
        if (skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped results require a skip reason");
        }
        EnumMap<SkipReason, Integer> skipCounts = new EnumMap<>(SkipReason.class);
        if (candidateCount > 0) {
            skipCounts.put(skipReason, candidateCount);
        }
        return new OreLoadChamberBlockPlacementResult(
                candidateCount,
                0,
                candidateCount,
                skipReason,
                skipCounts
        );
    }

    public static OreLoadChamberBlockPlacementResult completed(
            int candidateCount,
            int placedCount,
            Map<SkipReason, Integer> skipCounts
    ) {
        int skippedCount = skipCounts == null
                ? 0
                : skipCounts.values().stream().mapToInt(Integer::intValue).sum();
        return new OreLoadChamberBlockPlacementResult(
                candidateCount,
                placedCount,
                skippedCount,
                SkipReason.NONE,
                skipCounts
        );
    }

    public int skippedFor(SkipReason reason) {
        return skipCounts.getOrDefault(Objects.requireNonNull(reason, "reason"), 0);
    }

    public boolean anyPlaced() {
        return placedCount > 0;
    }

    private static Map<SkipReason, Integer> copySkipCounts(Map<SkipReason, Integer> skipCounts) {
        EnumMap<SkipReason, Integer> copied = new EnumMap<>(SkipReason.class);
        if (skipCounts != null) {
            for (Map.Entry<SkipReason, Integer> entry : skipCounts.entrySet()) {
                SkipReason reason = Objects.requireNonNull(entry.getKey(), "skip reason");
                int count = Objects.requireNonNull(entry.getValue(), "skip count");
                if (reason == SkipReason.NONE) {
                    throw new IllegalArgumentException("skipCounts must not include NONE");
                }
                if (count < 0) {
                    throw new IllegalArgumentException("skip count must not be negative");
                }
                if (count > 0) {
                    copied.merge(reason, count, Integer::sum);
                }
            }
        }
        return Collections.unmodifiableMap(copied);
    }

    public enum SkipReason {
        NONE,
        NULL_PLAN,
        PLAN_NOT_READY,
        TARGET_UNAVAILABLE,
        TARGET_OUTSIDE_WRITE_REGION,
        TARGET_NOT_REPLACEABLE,
        TARGET_RESOURCE_MISSING,
        WORLD_WRITE_FAILED
    }
}
