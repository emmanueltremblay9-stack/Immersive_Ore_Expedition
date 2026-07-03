package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class RandomOreSuppressionRules {
    private static final double ORIGINAL_DENSITY_MULTIPLIER = 1.0D;

    public RandomOreSuppressionPlan noOpDisabled(
            ResourceRef resource,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        return RandomOreSuppressionPlan.skipped(
                resource,
                RandomOreSuppressionPlan.Decision.NO_OP_DISABLED,
                RandomOreSuppressionPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                ORIGINAL_DENSITY_MULTIPLIER,
                ORIGINAL_DENSITY_MULTIPLIER,
                false,
                false,
                false,
                biomeId,
                provinceId,
                RandomOreSuppressionPlan.SourceReason.CONFIG_DISABLED
        );
    }

    public RandomOreSuppressionPlan invalidInput(
            ResourceRef resource,
            RandomOreSuppressionPlan.SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        return RandomOreSuppressionPlan.skipped(
                resource,
                RandomOreSuppressionPlan.Decision.INVALID_INPUT,
                skipReason,
                ORIGINAL_DENSITY_MULTIPLIER,
                ORIGINAL_DENSITY_MULTIPLIER,
                false,
                false,
                false,
                biomeId,
                provinceId,
                RandomOreSuppressionPlan.SourceReason.INVALID_INPUT
        );
    }

    public RandomOreSuppressionPlan evaluateEnabled(
            ResourceRef resource,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            OreSuppressionPolicy suppressionPolicy,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        if (resource == null) {
            return invalidInput(null, RandomOreSuppressionPlan.SkipReason.NULL_RESOURCE, biomeId, provinceId);
        }
        if (scanner == null || policyService == null || suppressionPolicy == null) {
            return invalidInput(resource, RandomOreSuppressionPlan.SkipReason.INVALID_INPUT, biomeId, provinceId);
        }

        boolean resourceLoaded = scanner.isPresent(resource);
        if (policyService.isExcludedResource(resource.id())) {
            return RandomOreSuppressionPlan.skipped(
                    resource,
                    RandomOreSuppressionPlan.Decision.STRICT_EXCLUSION,
                    RandomOreSuppressionPlan.SkipReason.STRICT_EXCLUSION,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    0.0D,
                    true,
                    resourceLoaded,
                    true,
                    biomeId,
                    provinceId,
                    RandomOreSuppressionPlan.SourceReason.RESOURCE_POLICY
            );
        }

        ResourcePolicyDecision policyDecision = policyService.evaluate(resource, scanner);
        if (!policyDecision.shouldUse()) {
            boolean approvedButUnloaded = !resourceLoaded && policyService.isApprovedResource(resource.id());
            return RandomOreSuppressionPlan.skipped(
                    resource,
                    approvedButUnloaded
                            ? RandomOreSuppressionPlan.Decision.SKIP_UNLOADED_RESOURCE
                            : RandomOreSuppressionPlan.Decision.POLICY_DENIED,
                    approvedButUnloaded
                            ? RandomOreSuppressionPlan.SkipReason.RESOURCE_NOT_LOADED
                            : RandomOreSuppressionPlan.SkipReason.RESOURCE_DENIED_BY_POLICY,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    0.0D,
                    false,
                    resourceLoaded,
                    true,
                    biomeId,
                    provinceId,
                    RandomOreSuppressionPlan.SourceReason.RESOURCE_POLICY
            );
        }

        return planApprovedLoadedResource(
                resource,
                resourceLoaded,
                suppressionPolicy.densityMultiplier(),
                biomeId,
                provinceId
        );
    }

    RandomOreSuppressionPlan planApprovedLoadedResource(
            ResourceRef resource,
            boolean resourceLoaded,
            double densityMultiplier,
            ResourceLocation biomeId,
            ResourceLocation provinceId
    ) {
        Objects.requireNonNull(resource, "resource");
        if (!isValidDensityMultiplier(densityMultiplier)) {
            return RandomOreSuppressionPlan.skipped(
                    resource,
                    RandomOreSuppressionPlan.Decision.INVALID_INPUT,
                    RandomOreSuppressionPlan.SkipReason.INVALID_DENSITY_MULTIPLIER,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    false,
                    resourceLoaded,
                    false,
                    biomeId,
                    provinceId,
                    RandomOreSuppressionPlan.SourceReason.INVALID_INPUT
            );
        }
        if (densityMultiplier == 0.0D) {
            return RandomOreSuppressionPlan.produced(
                    resource,
                    RandomOreSuppressionPlan.Decision.SUPPRESS,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    0.0D,
                    resourceLoaded,
                    biomeId,
                    provinceId,
                    RandomOreSuppressionPlan.SourceReason.ORE_SUPPRESSION_POLICY
            );
        }
        if (densityMultiplier == ORIGINAL_DENSITY_MULTIPLIER) {
            return RandomOreSuppressionPlan.produced(
                    resource,
                    RandomOreSuppressionPlan.Decision.ALLOW_ORIGINAL,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    ORIGINAL_DENSITY_MULTIPLIER,
                    resourceLoaded,
                    biomeId,
                    provinceId,
                    RandomOreSuppressionPlan.SourceReason.ORE_SUPPRESSION_POLICY
            );
        }
        return RandomOreSuppressionPlan.produced(
                resource,
                RandomOreSuppressionPlan.Decision.SCALE_DENSITY,
                ORIGINAL_DENSITY_MULTIPLIER,
                densityMultiplier,
                resourceLoaded,
                biomeId,
                provinceId,
                RandomOreSuppressionPlan.SourceReason.ORE_SUPPRESSION_POLICY
        );
    }

    private static boolean isValidDensityMultiplier(double densityMultiplier) {
        return Double.isFinite(densityMultiplier)
                && densityMultiplier >= 0.0D
                && densityMultiplier <= ORIGINAL_DENSITY_MULTIPLIER;
    }
}
