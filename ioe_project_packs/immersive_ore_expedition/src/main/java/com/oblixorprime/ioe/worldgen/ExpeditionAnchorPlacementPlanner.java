package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class ExpeditionAnchorPlacementPlanner {
    private final ExpeditionAnchorPlacementRules rules;

    public ExpeditionAnchorPlacementPlanner(ExpeditionAnchorPlacementRules rules) {
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    public static ExpeditionAnchorPlacementPlanner defaults() {
        return new ExpeditionAnchorPlacementPlanner(ExpeditionAnchorPlacementRules.defaults());
    }

    public ExpeditionAnchorPlacementPlan planAnchorPlacement(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality
    ) {
        return planAnchorPlacement(
                anchorType,
                origin,
                siteQuality,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public ExpeditionAnchorPlacementPlan planAnchorPlacement(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            IoeWorldgenPlacementGates placementGates
    ) {
        return planAnchorPlacement(anchorType, origin, siteQuality, null, null, placementGates);
    }

    public ExpeditionAnchorPlacementPlan planAnchorPlacement(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            IoeWorldgenPlacementGates placementGates
    ) {
        Objects.requireNonNull(placementGates, "placementGates");

        ExpeditionAnchorPlacementPlan.SkipReason validationSkipReason =
                rules.validate(anchorType, origin, siteQuality);
        if (validationSkipReason != ExpeditionAnchorPlacementPlan.SkipReason.NONE) {
            return ExpeditionAnchorPlacementPlan.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    validationSkipReason,
                    biomeId,
                    provinceId
            );
        }
        if (placementGates.shouldNoOpRuntimePlacement()) {
            return ExpeditionAnchorPlacementPlan.skipped(
                    anchorType,
                    origin,
                    siteQuality,
                    ExpeditionAnchorPlacementPlan.SkipReason.RUNTIME_WORLDGEN_DISABLED,
                    biomeId,
                    provinceId
            );
        }

        return ExpeditionAnchorPlacementPlan.allowed(
                anchorType,
                origin,
                siteQuality,
                biomeId,
                provinceId
        );
    }
}
