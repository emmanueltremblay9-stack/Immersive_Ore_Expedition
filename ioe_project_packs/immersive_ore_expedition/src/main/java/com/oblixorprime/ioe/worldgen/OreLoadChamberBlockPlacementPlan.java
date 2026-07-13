package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

public record OreLoadChamberBlockPlacementPlan(
        OreLoadChamberPlacementPlan chamberPlan,
        List<OreBlockTarget> oreTargets,
        boolean placementReady,
        SkipReason skipReason
) {
    public OreLoadChamberBlockPlacementPlan {
        oreTargets = List.copyOf(Objects.requireNonNull(oreTargets, "oreTargets"));
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        if (placementReady && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Ready block placement plans must not carry a skip reason");
        }
        if (!placementReady && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped block placement plans require a skip reason");
        }
        if (placementReady) {
            Objects.requireNonNull(chamberPlan, "chamberPlan");
            if (!chamberPlan.placementAllowed()) {
                throw new IllegalArgumentException("Ready block placement plans require an allowed chamber plan");
            }
            if (oreTargets.isEmpty()) {
                throw new IllegalArgumentException("Ready block placement plans require ore targets");
            }
        }
    }

    public static OreLoadChamberBlockPlacementPlan ready(
            OreLoadChamberPlacementPlan chamberPlan,
            List<OreBlockTarget> oreTargets
    ) {
        return new OreLoadChamberBlockPlacementPlan(
                chamberPlan,
                oreTargets,
                true,
                SkipReason.NONE
        );
    }

    public static OreLoadChamberBlockPlacementPlan skipped(
            OreLoadChamberPlacementPlan chamberPlan,
            SkipReason skipReason
    ) {
        return new OreLoadChamberBlockPlacementPlan(
                chamberPlan,
                List.of(),
                false,
                skipReason
        );
    }

    public record OreBlockTarget(BlockPos pos, ResourceRef resource) {
        public OreBlockTarget {
            Objects.requireNonNull(pos, "pos");
            Objects.requireNonNull(resource, "resource");
            if (resource.type() != ResourceType.BLOCK) {
                throw new IllegalArgumentException("Ore block targets require a concrete block resource");
            }
        }
    }

    public enum SkipReason {
        NONE,
        NULL_CHAMBER_PLAN,
        CHAMBER_PLAN_NOT_ALLOWED,
        NULL_RESOURCE,
        UNSUPPORTED_RESOURCE_TYPE,
        MISSING_CHAMBER_METADATA,
        DRY_SITE,
        NO_CANDIDATE_POSITIONS
    }
}
