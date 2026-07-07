package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class RandomOreSuppressionPlanner {
    private final RandomOreSuppressionRules rules;
    private final OreSuppressionPolicy suppressionPolicy;

    public RandomOreSuppressionPlanner(
            RandomOreSuppressionRules rules,
            OreSuppressionPolicy suppressionPolicy
    ) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.suppressionPolicy = Objects.requireNonNull(suppressionPolicy, "suppressionPolicy");
    }

    public static RandomOreSuppressionPlanner defaults() {
        return new RandomOreSuppressionPlanner(
                new RandomOreSuppressionRules(),
                new OreSuppressionPolicy()
        );
    }

    public RandomOreSuppressionPlan planRandomOreSuppression(
            ResourceRef resource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        return planRandomOreSuppression(
                resource,
                scanner,
                policyService,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public RandomOreSuppressionPlan planRandomOreSuppression(
            ResourceRef resource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        return planRandomOreSuppression(resource, scanner, policyService, null, null, placementGates);
    }

    public RandomOreSuppressionPlan planRandomOreSuppression(
            ResourceRef resource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (resource == null) {
            return rules.invalidInput(null, RandomOreSuppressionPlan.SkipReason.NULL_RESOURCE, biomeId, provinceId);
        }
        if (placementGates == null) {
            return rules.invalidInput(resource, RandomOreSuppressionPlan.SkipReason.INVALID_INPUT, biomeId, provinceId);
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return rules.noOpDisabled(resource, biomeId, provinceId);
        }
        return rules.evaluateEnabled(
                resource,
                scanner,
                policyService,
                suppressionPolicy,
                biomeId,
                provinceId
        );
    }
}
