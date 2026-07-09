package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.function.BiConsumer;

final class RuntimeWorldgenProofSiteRecorder {
    private RuntimeWorldgenProofSiteRecorder() {
    }

    static boolean placeAndRecordProvenSite(
            ResourceKey<Level> dimension,
            RuntimeWorldgenPlacementProofResult readyResult,
            FootprintPlacement footprintPlacement,
            BiConsumer<ResourceKey<Level>, RuntimeWorldgenPlacementProofResult> recorder
    ) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(readyResult, "readyResult");
        Objects.requireNonNull(footprintPlacement, "footprintPlacement");
        Objects.requireNonNull(recorder, "recorder");
        if (!readyResult.placementPathAllowed()) {
            logPlacementResult(readyResult);
            return false;
        }
        if (!footprintPlacement.place()) {
            RuntimeWorldgenPlacementProofResult failedResult = RuntimeWorldgenPlacementProofResult.skipped(
                    readyResult.anchorType(),
                    readyResult.origin(),
                    readyResult.siteQuality(),
                    RuntimeWorldgenPlacementProof.DEFAULT_PROOF_RESOURCE,
                    RuntimeWorldgenPlacementProofResult.SkipReason.WORLD_WRITE_FAILED,
                    readyResult.anchorPlan().orElse(null),
                    readyResult.resourceDecision().orElse(null),
                    readyResult.diagnosticsEnabled()
            );
            logPlacementResult(failedResult);
            return false;
        }

        RuntimeWorldgenPlacementProofResult placedResult = RuntimeWorldgenPlacementProofResult.placed(readyResult);
        recorder.accept(dimension, placedResult);
        logPlacementResult(placedResult);
        return true;
    }

    private static void logPlacementResult(RuntimeWorldgenPlacementProofResult result) {
        if (!result.diagnosticsEnabled()) {
            return;
        }
        if (result.blockPlaced()) {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "IOE runtime expedition site proven id={} pos={} state=proven source=biome_feature",
                    result.anchorType(),
                    result.origin()
            );
        } else {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "IOE runtime expedition site skipped id={} pos={} reason={} source=biome_feature",
                    result.anchorType(),
                    result.origin(),
                    result.skipReason()
            );
        }
    }

    @FunctionalInterface
    interface FootprintPlacement {
        boolean place();
    }
}
