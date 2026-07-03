package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class OreLoadChamberPlacementPlanner {
    private final OreLoadChamberPlacementRules rules;

    public OreLoadChamberPlacementPlanner(OreLoadChamberPlacementRules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    public static OreLoadChamberPlacementPlanner defaults() {
        return new OreLoadChamberPlacementPlanner(OreLoadChamberPlacementRules.defaults());
    }

    public OreLoadChamberPlacementPlan planChamberPlacement(
            OreLoadPlan sourcePlan,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        return planChamberPlacement(
                sourcePlan,
                scanner,
                policyService,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public OreLoadChamberPlacementPlan planChamberPlacement(
            OreLoadPlan sourcePlan,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            IoeWorldgenPlacementGates placementGates
    ) {
        return planChamberPlacement(sourcePlan, scanner, policyService, null, null, placementGates);
    }

    public OreLoadChamberPlacementPlan planChamberPlacement(
            OreLoadPlan sourcePlan,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            IoeWorldgenPlacementGates placementGates
    ) {
        OreLoadChamberPlacementPlan.SkipReason sourceSkipReason = rules.validateSourcePlan(sourcePlan);
        ResourceLocation anchorType = rules.resolveAnchorType(sourcePlan).orElse(null);
        OreLoadChamberPlacementPlan.ChamberMetadata metadata = rules.metadataFor(sourcePlan).orElse(null);
        if (sourceSkipReason != OreLoadChamberPlacementPlan.SkipReason.NONE) {
            return OreLoadChamberPlacementPlan.skipped(
                    sourcePlan,
                    anchorType,
                    sourceSkipReason,
                    biomeId,
                    provinceId,
                    metadata
            );
        }
        if (placementGates == null) {
            return OreLoadChamberPlacementPlan.skipped(
                    sourcePlan,
                    anchorType,
                    OreLoadChamberPlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    metadata
            );
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return OreLoadChamberPlacementPlan.skipped(
                    sourcePlan,
                    anchorType,
                    OreLoadChamberPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                    biomeId,
                    provinceId,
                    metadata
            );
        }

        OreLoadChamberPlacementPlan.SkipReason resourceSkipReason =
                rules.validateResourcePolicy(sourcePlan, scanner, policyService);
        if (resourceSkipReason != OreLoadChamberPlacementPlan.SkipReason.NONE) {
            return OreLoadChamberPlacementPlan.skipped(
                    sourcePlan,
                    anchorType,
                    resourceSkipReason,
                    biomeId,
                    provinceId,
                    metadata
            );
        }

        return OreLoadChamberPlacementPlan.allowed(
                sourcePlan,
                anchorType,
                biomeId,
                provinceId,
                metadata
        );
    }
}
