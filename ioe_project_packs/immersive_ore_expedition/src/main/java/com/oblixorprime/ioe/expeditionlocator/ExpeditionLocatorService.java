package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import com.oblixorprime.ioe.worldgen.IoeWorldgenConfig;
import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class ExpeditionLocatorService {
    private static final ExpeditionLocatorIndex RUNTIME_INDEX = new ExpeditionLocatorIndex();

    private ExpeditionLocatorService() {
    }

    public static ExpeditionLocatorIndex index() {
        return RUNTIME_INDEX;
    }

    public static ExpeditionLocatorIndex compassIndex(ResourceKey<Level> dimension, BlockPos origin) {
        logCompassDiagnostics(
                Objects.requireNonNull(dimension, "dimension"),
                Objects.requireNonNull(origin, "origin")
        );
        return RUNTIME_INDEX;
    }

    public static void recordPlacedProof(
            ResourceKey<Level> dimension,
            RuntimeWorldgenPlacementProofResult result
    ) {
        RUNTIME_INDEX.recordPlacedProof(
                Objects.requireNonNull(dimension, "dimension"),
                Objects.requireNonNull(result, "result")
        );
    }

    public static void clearForTesting() {
        RUNTIME_INDEX.clear();
    }

    private static void logCompassDiagnostics(ResourceKey<Level> dimension, BlockPos origin) {
        if (!IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            return;
        }

        RUNTIME_INDEX.diagnosticSites().stream()
                .filter(site -> site.dimension().equals(dimension))
                .forEach(site -> IoeExpeditionWorldgenMod.LOGGER.info(
                        "IOE compass indexed site id={} type={} dimension={} chunk={} section={} anchorPos={} targetPos={} pipelineStage={} state={} reason={} gates(runtimePlacement={}, proofFeature={}, provinceRuntime={}) biome={} province={} distanceFromPlayer={}",
                        site.primaryId().map(Object::toString).orElse("unknown"),
                        site.kind().getSerializedName(),
                        site.dimension().location(),
                        chunkText(site.pos()),
                        site.pos().getY() >> 4,
                        site.pos(),
                        site.pos(),
                        site.source().orElse("unknown"),
                        site.placementState().getSerializedName(),
                        site.placementReason().orElse("none"),
                        IoeWorldgenConfig.runtimePlacementEnabled(),
                        IoeWorldgenConfig.runtimeProofFeatureEnabled(),
                        IoeWorldgenConfig.provinceRuntimeIntegrationEnabled(),
                        "unknown",
                        site.provinceId().map(Object::toString).orElse("unknown"),
                        Math.round(Math.sqrt(ExpeditionLocatorIndex.distanceSquared(origin, site.pos())))
                ));
    }

    private static String chunkText(BlockPos pos) {
        return (pos.getX() >> 4) + "," + (pos.getZ() >> 4);
    }
}
