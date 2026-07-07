package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record WorldgenBiomeProvinceContext(
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        boolean contextResolved,
        boolean provinceIntegrationAllowed,
        ResolutionDecision decision,
        SkipReason skipReason,
        boolean defaultProvinceFallbackUsed,
        boolean runtimeWorldgenGateDisabled,
        boolean provinceRuntimeIntegrationGateDisabled
) {
    public WorldgenBiomeProvinceContext {
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        if (contextResolved && provinceId.isEmpty()) {
            throw new IllegalArgumentException("resolved biome province contexts require a province id");
        }
        if (contextResolved && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("resolved biome province contexts must not carry a skip reason");
        }
        if (!contextResolved && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("unresolved biome province contexts require a skip reason");
        }
    }

    public static WorldgenBiomeProvinceContext resolved(
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            boolean defaultProvinceFallbackUsed
    ) {
        return new WorldgenBiomeProvinceContext(
                Optional.of(Objects.requireNonNull(biomeId, "biomeId")),
                Optional.of(Objects.requireNonNull(provinceId, "provinceId")),
                true,
                true,
                defaultProvinceFallbackUsed
                        ? ResolutionDecision.RESOLVED_FROM_DEFAULT_PROVINCE
                        : ResolutionDecision.RESOLVED_FROM_BIOME,
                SkipReason.NONE,
                defaultProvinceFallbackUsed,
                false,
                false
        );
    }

    public static WorldgenBiomeProvinceContext noOp(
            ResourceLocation biomeId,
            ResolutionDecision decision,
            SkipReason skipReason,
            boolean runtimeWorldgenGateDisabled,
            boolean provinceRuntimeIntegrationGateDisabled
    ) {
        return new WorldgenBiomeProvinceContext(
                Optional.ofNullable(biomeId),
                Optional.empty(),
                false,
                false,
                decision,
                skipReason,
                false,
                runtimeWorldgenGateDisabled,
                provinceRuntimeIntegrationGateDisabled
        );
    }

    public enum ResolutionDecision {
        RESOLVED_FROM_BIOME,
        RESOLVED_FROM_DEFAULT_PROVINCE,
        NO_OP_RUNTIME_WORLDGEN_DISABLED,
        NO_OP_PROVINCE_INTEGRATION_DISABLED,
        UNRESOLVED_INVALID_INPUT,
        UNRESOLVED_MALFORMED_BINDING
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        PROVINCE_RUNTIME_INTEGRATION_DISABLED,
        NULL_BIOME,
        INVALID_BIOME_ID,
        MALFORMED_BINDING,
        INVALID_PROVINCE_ID,
        INVALID_INPUT
    }
}
