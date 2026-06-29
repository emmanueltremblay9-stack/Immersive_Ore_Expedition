package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.List;
import java.util.Objects;

public record ProvinceValidationResult(
        ProvinceRule rule,
        List<ResourceRef> usableResources,
        List<ResourcePolicyDecision> skippedResources,
        List<ResourcePolicyDecision> rejectedResources
) {
    public ProvinceValidationResult {
        Objects.requireNonNull(rule, "rule");
        usableResources = List.copyOf(Objects.requireNonNull(usableResources, "usableResources"));
        skippedResources = List.copyOf(Objects.requireNonNull(skippedResources, "skippedResources"));
        rejectedResources = List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
    }

    public boolean hasUsableResources() {
        return !usableResources.isEmpty();
    }
}
