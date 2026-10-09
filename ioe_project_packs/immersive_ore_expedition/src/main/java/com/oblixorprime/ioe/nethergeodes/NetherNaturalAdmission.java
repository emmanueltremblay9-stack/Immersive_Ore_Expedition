package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Explicit admission with deterministic lowest-surface selection. Not registered with a command, tick or generator. */
final class NetherNaturalAdmission {
    private NetherNaturalAdmission() { }

    static NetherPlacementCoordinator.Result attempt(ServerLevel level, ChunkPos trigger) {
        var host = NetherPlacementRuntime.host(level);
        host.requireServerThread();
        var candidate = NetherSitePlanner.candidate(level.getSeed(), Math.floorDiv(trigger.x, 16), Math.floorDiv(trigger.z, 16));
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        if (!new ChunkPos(origin).equals(trigger)) return NetherPlacementCoordinator.Result.NOT_CANDIDATE;
        var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
        var admission = ledger.prepare(origin);
        if (admission == null) return NetherPlacementCoordinator.Result.DUPLICATE;
        var capture = new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                () -> NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()));
        NetherPlacementCoordinator.Result result;
        try {
            if (!NetherPlacementRuntime.coordinator(level).hasFreshReceipt(host, trigger.toLong())) {
                result = NetherPlacementCoordinator.Result.NOT_FRESH;
            } else {
                var outcome = NetherSitePlanner.planLowest(candidate, capture);
                capture.validate();
                if (outcome.plan() == null) {
                    result = outcome.status() == NetherSitePlanner.Status.BUDGET
                            ? NetherPlacementCoordinator.Result.BUDGET : NetherPlacementCoordinator.Result.PLAN_REJECTED;
                } else {
                    // Acquisition and placement share the same server turn and global budget.
                    return NetherPlacementRuntime.commitPrepared(level, outcome.plan(), admission);
                }
            }
        } catch (NetherSnapshotDiagnostic.Aborted failure) {
            result = failure.state == NetherSnapshotDiagnostic.State.CAPTURE_BUDGET
                    ? NetherPlacementCoordinator.Result.BUDGET : NetherPlacementCoordinator.Result.TERRAIN_CHANGED;
        } finally { capture.discard(); }
        admission.finish(origin, result);
        return result;
    }
}
