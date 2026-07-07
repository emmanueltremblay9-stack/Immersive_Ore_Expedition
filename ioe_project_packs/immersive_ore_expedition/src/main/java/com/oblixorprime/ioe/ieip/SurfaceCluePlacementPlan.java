package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record SurfaceCluePlacementPlan(
        ClueType clueType,
        SourceSystem sourceSystem,
        ResourceRef clueResource,
        BlockPos origin,
        boolean placementAllowed,
        Decision decision,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        Optional<ResourceLocation> anchorType,
        Optional<ClueMetadata> clueMetadata
) {
    public SurfaceCluePlacementPlan {
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        anchorType = anchorType == null ? Optional.empty() : anchorType;
        clueMetadata = clueMetadata == null ? Optional.empty() : clueMetadata;
        if (placementAllowed && decision != Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Allowed surface clue plans must use ALLOW_PLAN");
        }
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed surface clue plans must not carry a skip reason");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped surface clue plans require a skip reason");
        }
        if (!placementAllowed && decision == Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Skipped surface clue plans must not use ALLOW_PLAN");
        }
        if (placementAllowed) {
            Objects.requireNonNull(clueType, "clueType");
            Objects.requireNonNull(sourceSystem, "sourceSystem");
            Objects.requireNonNull(clueResource, "clueResource");
            Objects.requireNonNull(origin, "origin");
            if (clueMetadata.isEmpty()) {
                throw new IllegalArgumentException("Allowed surface clue plans require clue metadata");
            }
        }
    }

    public static SurfaceCluePlacementPlan allowed(
            ClueType clueType,
            SourceSystem sourceSystem,
            ResourceRef clueResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            ClueMetadata clueMetadata
    ) {
        return new SurfaceCluePlacementPlan(
                clueType,
                sourceSystem,
                clueResource,
                origin,
                true,
                Decision.ALLOW_PLAN,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.of(Objects.requireNonNull(clueMetadata, "clueMetadata"))
        );
    }

    public static SurfaceCluePlacementPlan skipped(
            ClueType clueType,
            SourceSystem sourceSystem,
            ResourceRef clueResource,
            BlockPos origin,
            Decision decision,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            ClueMetadata clueMetadata
    ) {
        return new SurfaceCluePlacementPlan(
                clueType,
                sourceSystem,
                clueResource,
                origin,
                false,
                decision,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.ofNullable(clueMetadata)
        );
    }

    public record ClueMetadata(
            double rarityWeight,
            int clueSize,
            boolean pocketLake,
            boolean gasVent,
            boolean rendersFullUndergroundDeposit
    ) {
        public ClueMetadata {
            if (!Double.isFinite(rarityWeight) || rarityWeight < 0.0D) {
                throw new IllegalArgumentException("rarityWeight must be finite and non-negative");
            }
            if (clueSize < 0) {
                throw new IllegalArgumentException("clueSize must not be negative");
            }
            if (rendersFullUndergroundDeposit) {
                throw new IllegalArgumentException("Surface clue plans must not render full underground deposits");
            }
        }
    }

    public enum ClueType {
        IE_MINERAL_BOULDER,
        IE_MINERAL_OUTCROP,
        IP_SEEP,
        IP_POCKET_LAKE,
        IP_GAS_VENT
    }

    public enum SourceSystem {
        IE_MINERAL_OUTCROP,
        IP_RESERVOIR_SEEP
    }

    public enum Decision {
        ALLOW_PLAN,
        SKIP_RUNTIME_DISABLED,
        SKIP_RESOURCE_NOT_LOADED,
        SKIP_RESOURCE_DENIED,
        SKIP_STRICT_EXCLUSION,
        SKIP_OPTIONAL_MOD_ABSENT,
        SKIP_INVALID_CLUE_TYPE,
        SKIP_INVALID_INPUT
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NULL_RESOURCE,
        NULL_ORIGIN,
        INVALID_CLUE_TYPE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        OPTIONAL_MOD_ABSENT,
        INVALID_INPUT
    }
}
