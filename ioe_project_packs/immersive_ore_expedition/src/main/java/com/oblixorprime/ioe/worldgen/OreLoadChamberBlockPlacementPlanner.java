package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OreLoadChamberBlockPlacementPlanner {
    public OreLoadChamberBlockPlacementPlan planBlockPlacements(OreLoadChamberPlacementPlan chamberPlan) {
        if (chamberPlan == null) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    null,
                    OreLoadChamberBlockPlacementPlan.SkipReason.NULL_CHAMBER_PLAN
            );
        }
        if (!chamberPlan.placementAllowed()) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    chamberPlan,
                    OreLoadChamberBlockPlacementPlan.SkipReason.CHAMBER_PLAN_NOT_ALLOWED
            );
        }

        ResourceRef resource = chamberPlan.resource();
        if (resource == null) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    chamberPlan,
                    OreLoadChamberBlockPlacementPlan.SkipReason.NULL_RESOURCE
            );
        }
        if (resource.type() != ResourceType.BLOCK) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    chamberPlan,
                    OreLoadChamberBlockPlacementPlan.SkipReason.UNSUPPORTED_RESOURCE_TYPE
            );
        }
        if (chamberPlan.chamberMetadata().isEmpty()) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    chamberPlan,
                    OreLoadChamberBlockPlacementPlan.SkipReason.MISSING_CHAMBER_METADATA
            );
        }

        List<OreLoadChamberBlockPlacementPlan.OreBlockTarget> targets =
                targetsFor(chamberPlan.chamberCenter(), resource, chamberPlan.chamberMetadata().orElseThrow());
        if (targets.isEmpty()) {
            return OreLoadChamberBlockPlacementPlan.skipped(
                    chamberPlan,
                    OreLoadChamberBlockPlacementPlan.SkipReason.NO_CANDIDATE_POSITIONS
            );
        }

        return OreLoadChamberBlockPlacementPlan.ready(chamberPlan, targets);
    }

    private static List<OreLoadChamberBlockPlacementPlan.OreBlockTarget> targetsFor(
            BlockPos center,
            ResourceRef resource,
            OreLoadChamberPlacementPlan.ChamberMetadata metadata
    ) {
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(metadata, "metadata");

        int horizontalRadius = metadata.horizontalRadius();
        int verticalHalfSize = metadata.verticalHalfSize();
        List<OreLoadChamberBlockPlacementPlan.OreBlockTarget> targets = new ArrayList<>();
        for (int dy = -verticalHalfSize; dy <= verticalHalfSize; dy++) {
            for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
                for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                    if (insideChamberEnvelope(dx, dy, dz, horizontalRadius, verticalHalfSize)) {
                        targets.add(new OreLoadChamberBlockPlacementPlan.OreBlockTarget(
                                center.offset(dx, dy, dz),
                                resource
                        ));
                    }
                }
            }
        }
        return targets;
    }

    private static boolean insideChamberEnvelope(
            int dx,
            int dy,
            int dz,
            int horizontalRadius,
            int verticalHalfSize
    ) {
        double horizontal = (double) (dx * dx + dz * dz) / (double) (horizontalRadius * horizontalRadius);
        double vertical = (double) (dy * dy) / (double) (verticalHalfSize * verticalHalfSize);
        return horizontal + vertical <= 1.0D;
    }
}
