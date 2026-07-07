package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProvinceRuleValidator {
    private final ResourcePolicyService policyService;
    private final LoadedResourceScanner scanner;

    public ProvinceRuleValidator(ResourcePolicyService policyService, LoadedResourceScanner scanner) {
        this.policyService = Objects.requireNonNull(policyService, "policyService");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public ProvinceValidationResult validate(ProvinceRule rule) {
        Objects.requireNonNull(rule, "rule");
        List<ResourceRef> usable = new ArrayList<>();
        List<ResourcePolicyDecision> skipped = new ArrayList<>();
        List<ResourcePolicyDecision> rejected = new ArrayList<>();
        boolean hasEnabledAnchorStructure = hasEnabledAnchorStructure(rule);

        for (ResourceRef resource : rule.resources()) {
            ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
            if (decision.shouldUse() && hasEnabledAnchorStructure) {
                usable.add(resource);
            } else if (decision.shouldUse()) {
                rejected.add(ResourcePolicyDecision.reject("Province has no enabled expedition anchor structure for resource: " + resource.id()));
            } else if (decision.shouldSkip()) {
                skipped.add(decision);
            } else {
                rejected.add(decision);
            }
        }

        return new ProvinceValidationResult(rule, usable, skipped, rejected);
    }

    private static boolean hasEnabledAnchorStructure(ProvinceRule rule) {
        return rule.anchorStructures().stream()
                .anyMatch(anchorStructure -> ExpeditionStructureRegistry.isEnabledStructureId(anchorStructure.toString()));
    }
}
