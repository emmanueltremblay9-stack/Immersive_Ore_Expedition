package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record RandomOreSuppressionPlan(
        ResourceRef resource,
        boolean decisionProduced,
        Decision decision,
        SkipReason skipReason,
        double originalDensityMultiplier,
        double effectiveDensityMultiplier,
        boolean strictExclusionApplied,
        boolean resourceLoaded,
        boolean resourceDeniedByPolicy,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        SourceReason sourceReason
) {
    public RandomOreSuppressionPlan {
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        sourceReason = Objects.requireNonNull(sourceReason, "sourceReason");
        if (!Double.isFinite(originalDensityMultiplier) || originalDensityMultiplier < 0.0D) {
            throw new IllegalArgumentException("originalDensityMultiplier must be finite and non-negative");
        }
        if (!Double.isFinite(effectiveDensityMultiplier) || effectiveDensityMultiplier < 0.0D) {
            throw new IllegalArgumentException("effectiveDensityMultiplier must be finite and non-negative");
        }
        if (decisionProduced && skipReason == SkipReason.RUNTIME_WORLDGEN_DISABLED) {
            throw new IllegalArgumentException("disabled runtime worldgen plans must not produce suppression decisions");
        }
        if (skipReason == SkipReason.NONE && !decisionProduced) {
            throw new IllegalArgumentException("successful suppression plans must produce a decision");
        }
    }

    public static RandomOreSuppressionPlan skipped(
            ResourceRef resource,
            Decision decision,
            SkipReason skipReason,
            double originalDensityMultiplier,
            double effectiveDensityMultiplier,
            boolean strictExclusionApplied,
            boolean resourceLoaded,
            boolean resourceDeniedByPolicy,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            SourceReason sourceReason
    ) {
        return new RandomOreSuppressionPlan(
                resource,
                producesDecision(skipReason),
                decision,
                skipReason,
                originalDensityMultiplier,
                effectiveDensityMultiplier,
                strictExclusionApplied,
                resourceLoaded,
                resourceDeniedByPolicy,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                sourceReason
        );
    }

    private static boolean producesDecision(SkipReason skipReason) {
        return skipReason == SkipReason.RESOURCE_NOT_LOADED
                || skipReason == SkipReason.RESOURCE_DENIED_BY_POLICY
                || skipReason == SkipReason.STRICT_EXCLUSION;
    }

    public static RandomOreSuppressionPlan produced(
            ResourceRef resource,
            Decision decision,
            double originalDensityMultiplier,
            double effectiveDensityMultiplier,
            boolean resourceLoaded,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            SourceReason sourceReason
    ) {
        Objects.requireNonNull(resource, "resource");
        return new RandomOreSuppressionPlan(
                resource,
                true,
                decision,
                SkipReason.NONE,
                originalDensityMultiplier,
                effectiveDensityMultiplier,
                false,
                resourceLoaded,
                false,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                sourceReason
        );
    }

    public enum Decision {
        ALLOW_ORIGINAL,
        SCALE_DENSITY,
        SUPPRESS,
        SKIP_UNLOADED_RESOURCE,
        STRICT_EXCLUSION,
        POLICY_DENIED,
        NO_OP_DISABLED,
        INVALID_INPUT
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NULL_RESOURCE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        INVALID_DENSITY_MULTIPLIER,
        INVALID_INPUT
    }

    public enum SourceReason {
        CONFIG_DISABLED,
        RESOURCE_POLICY,
        ORE_SUPPRESSION_POLICY,
        INVALID_INPUT
    }
}
