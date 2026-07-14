package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import com.oblixorprime.ioe.worldgen.IoeWorldgenConfig;
import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class ExpeditionLocatorService {
    private static final ExpeditionLocatorIndex TEST_INDEX = new ExpeditionLocatorIndex();

    private ExpeditionLocatorService() {
    }

    public static ExpeditionLocatorIndex index() {
        return TEST_INDEX;
    }

    public static ExpeditionLocatorIndex index(ServerLevel level) {
        return savedData(level).index();
    }

    public static void record(ServerLevel level, ExpeditionSite site) {
        Objects.requireNonNull(level, "level");
        ExpeditionSite recordedSite = Objects.requireNonNull(site, "site");
        runOnServerThread(level, () -> savedData(level).record(recordedSite));
    }

    public static ExpeditionLocatorIndex compassIndex(ServerLevel level, BlockPos origin) {
        ExpeditionLocatorIndex index = index(Objects.requireNonNull(level, "level"));
        logCompassDiagnostics(index, level.dimension(), Objects.requireNonNull(origin, "origin"));
        return index;
    }

    public static ExpeditionLocatorIndex compassIndex(ResourceKey<Level> dimension, BlockPos origin) {
        logCompassDiagnostics(
                TEST_INDEX,
                Objects.requireNonNull(dimension, "dimension"),
                Objects.requireNonNull(origin, "origin")
        );
        return TEST_INDEX;
    }

    public static void recordPlacedProof(
            ServerLevel level,
            RuntimeWorldgenPlacementProofResult result
    ) {
        Objects.requireNonNull(level, "level");
        RuntimeWorldgenPlacementProofResult recordedResult = Objects.requireNonNull(result, "result");
        runOnServerThread(level, () -> savedData(level).recordPlacedProof(level.dimension(), recordedResult));
    }

    public static void clearForTesting() {
        TEST_INDEX.clear();
    }

    private static ExpeditionLocatorSavedData savedData(ServerLevel level) {
        ServerLevel overworld = Objects.requireNonNull(level, "level").getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                ExpeditionLocatorSavedData.FACTORY,
                ExpeditionLocatorSavedData.STORAGE_NAME
        );
    }

    private static void runOnServerThread(ServerLevel level, Runnable action) {
        if (level.getServer().isSameThread()) {
            action.run();
        } else {
            level.getServer().execute(action);
        }
    }

    private static void logCompassDiagnostics(
            ExpeditionLocatorIndex index,
            ResourceKey<Level> dimension,
            BlockPos origin
    ) {
        if (!IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            return;
        }

        index.diagnosticSites().stream()
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
