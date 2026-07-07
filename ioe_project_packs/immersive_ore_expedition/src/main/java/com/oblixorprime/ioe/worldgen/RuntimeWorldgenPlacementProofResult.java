package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record RuntimeWorldgenPlacementProofResult(
        ResourceLocation anchorType,
        BlockPos origin,
        SiteQuality siteQuality,
        Optional<ResourceRef> proofResource,
        boolean placementPathAllowed,
        boolean blockPlaced,
        SkipReason skipReason,
        Optional<ExpeditionAnchorPlacementPlan> anchorPlan,
        Optional<ResourcePolicyDecision> resourceDecision,
        boolean diagnosticsEnabled
) {
    public RuntimeWorldgenPlacementProofResult {
        proofResource = proofResource == null ? Optional.empty() : proofResource;
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        anchorPlan = anchorPlan == null ? Optional.empty() : anchorPlan;
        resourceDecision = resourceDecision == null ? Optional.empty() : resourceDecision;
        if (placementPathAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed placement proof results must not carry a skip reason");
        }
        if (!placementPathAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped placement proof results require a skip reason");
        }
        if (blockPlaced && !placementPathAllowed) {
            throw new IllegalArgumentException("Placed proof results require an allowed placement path");
        }
    }

    static RuntimeWorldgenPlacementProofResult skipped(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            SkipReason skipReason,
            ExpeditionAnchorPlacementPlan anchorPlan,
            ResourcePolicyDecision resourceDecision,
            boolean diagnosticsEnabled
    ) {
        return new RuntimeWorldgenPlacementProofResult(
                anchorType,
                origin,
                siteQuality,
                Optional.ofNullable(proofResource),
                false,
                false,
                skipReason,
                Optional.ofNullable(anchorPlan),
                Optional.ofNullable(resourceDecision),
                diagnosticsEnabled
        );
    }

    static RuntimeWorldgenPlacementProofResult ready(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceRef proofResource,
            ExpeditionAnchorPlacementPlan anchorPlan,
            ResourcePolicyDecision resourceDecision,
            boolean diagnosticsEnabled
    ) {
        return new RuntimeWorldgenPlacementProofResult(
                anchorType,
                origin,
                siteQuality,
                Optional.of(proofResource),
                true,
                false,
                SkipReason.NONE,
                Optional.of(anchorPlan),
                Optional.of(resourceDecision),
                diagnosticsEnabled
        );
    }

    static RuntimeWorldgenPlacementProofResult placed(RuntimeWorldgenPlacementProofResult readyResult) {
        Objects.requireNonNull(readyResult, "readyResult");
        if (!readyResult.placementPathAllowed()) {
            throw new IllegalArgumentException("Only allowed proof results can be marked placed");
        }
        return new RuntimeWorldgenPlacementProofResult(
                readyResult.anchorType(),
                readyResult.origin(),
                readyResult.siteQuality(),
                readyResult.proofResource(),
                true,
                true,
                SkipReason.NONE,
                readyResult.anchorPlan(),
                readyResult.resourceDecision(),
                readyResult.diagnosticsEnabled()
        );
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        ANCHOR_PLANNING_REJECTED,
        UNSUPPORTED_PROOF_RESOURCE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        TARGET_OUTSIDE_WRITE_REGION,
        TARGET_NOT_REPLACEABLE,
        WORLD_WRITE_FAILED,
        INVALID_INPUT
    }
}
