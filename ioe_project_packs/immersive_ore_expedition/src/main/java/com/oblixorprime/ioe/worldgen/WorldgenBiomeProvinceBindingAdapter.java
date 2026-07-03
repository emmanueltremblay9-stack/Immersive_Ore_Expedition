package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class WorldgenBiomeProvinceBindingAdapter {
    private final WorldgenBiomeProvinceBindingRules rules;

    public WorldgenBiomeProvinceBindingAdapter(WorldgenBiomeProvinceBindingRules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    public static WorldgenBiomeProvinceBindingAdapter fromConfig() {
        return new WorldgenBiomeProvinceBindingAdapter(WorldgenBiomeProvinceBindingRules.fromConfig());
    }

    public static WorldgenBiomeProvinceBindingAdapter defaults() {
        return new WorldgenBiomeProvinceBindingAdapter(WorldgenBiomeProvinceBindingRules.defaults());
    }

    public WorldgenBiomeProvinceContext resolve(ResourceLocation biomeId) {
        return resolve(biomeId, IoeWorldgenPlacementGates.fromConfig());
    }

    public WorldgenBiomeProvinceContext resolve(
            ResourceLocation biomeId,
            IoeWorldgenPlacementGates placementGates
    ) {
        if (placementGates == null) {
            return WorldgenBiomeProvinceContext.noOp(
                    biomeId,
                    WorldgenBiomeProvinceContext.ResolutionDecision.UNRESOLVED_INVALID_INPUT,
                    WorldgenBiomeProvinceContext.SkipReason.INVALID_INPUT,
                    false,
                    false
            );
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return WorldgenBiomeProvinceContext.noOp(
                    biomeId,
                    WorldgenBiomeProvinceContext.ResolutionDecision.NO_OP_RUNTIME_WORLDGEN_DISABLED,
                    WorldgenBiomeProvinceContext.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                    true,
                    false
            );
        }
        if (!placementGates.provinceRuntimeIntegrationEnabled()) {
            return WorldgenBiomeProvinceContext.noOp(
                    biomeId,
                    WorldgenBiomeProvinceContext.ResolutionDecision.NO_OP_PROVINCE_INTEGRATION_DISABLED,
                    WorldgenBiomeProvinceContext.SkipReason.PROVINCE_RUNTIME_INTEGRATION_DISABLED,
                    false,
                    true
            );
        }

        return rules.resolve(biomeId);
    }
}
