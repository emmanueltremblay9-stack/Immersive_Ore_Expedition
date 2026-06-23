package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.List;
import java.util.Objects;

public record MineralOutcropPlan(
        String depositId,
        ResourceRef clueResource,
        int boulderCount,
        int freeOreRewardLimitBlocks,
        boolean usesDepositPresentResourcesOnly,
        boolean rendersFullDeposit,
        List<ResourcePolicyDecision> skippedResources,
        List<ResourcePolicyDecision> rejectedResources
) {
    public MineralOutcropPlan {
        Objects.requireNonNull(depositId, "depositId");
        Objects.requireNonNull(clueResource, "clueResource");
        if (depositId.isBlank()) {
            throw new IllegalArgumentException("depositId must not be blank");
        }
        if (boulderCount < 0) {
            throw new IllegalArgumentException("boulderCount must not be negative");
        }
        if (freeOreRewardLimitBlocks < 0) {
            throw new IllegalArgumentException("freeOreRewardLimitBlocks must not be negative");
        }
        skippedResources = List.copyOf(Objects.requireNonNull(skippedResources, "skippedResources"));
        rejectedResources = List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
    }
}
