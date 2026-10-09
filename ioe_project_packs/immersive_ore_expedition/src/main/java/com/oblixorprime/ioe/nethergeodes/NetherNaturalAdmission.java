package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Explicit admission with deterministic lowest-surface selection. Not registered with a command, tick or generator. */
final class NetherNaturalAdmission {
    private NetherNaturalAdmission() { }

    record Report(NetherPlacementCoordinator.Result result, String plannerStatus, int worldReads,
                  int connectedColumns, int plannedWrites) { }

    static NetherPlacementCoordinator.Result attempt(ServerLevel level, ChunkPos trigger) {
        return attemptDetailed(level, trigger).result();
    }

    static Report attemptDetailed(ServerLevel level, ChunkPos trigger) {
        var host = NetherPlacementRuntime.host(level);
        host.requireServerThread();
        var candidate = NetherSitePlanner.candidate(level.getSeed(), Math.floorDiv(trigger.x, 16), Math.floorDiv(trigger.z, 16));
        var origin = new BlockPos(candidate.x(), level.getMinBuildHeight(), candidate.z());
        if (!new ChunkPos(origin).equals(trigger)) return new Report(NetherPlacementCoordinator.Result.NOT_CANDIDATE, "NOT_EVALUATED", 0, 0, 0);
        var ledger = level.getDataStorage().computeIfAbsent(NetherPlacementLedger.FACTORY, NetherPlacementLedger.NAME);
        var admission = ledger.prepare(origin);
        if (admission == null) return new Report(NetherPlacementCoordinator.Result.DUPLICATE, "NOT_EVALUATED", 0, 0, 0);
        var capture = new NetherSnapshotDiagnostic.Capture(NetherSnapshotDiagnostic.source(level),
                () -> NetherAnalysisBudget.forServer(level.getServer()).acquire(level.getServer().getTickCount()));
        NetherPlacementCoordinator.Result result;
        String plannerStatus = "NOT_EVALUATED";
        int connectedColumns = 0;
        try {
            if (!NetherPlacementRuntime.coordinator(level).hasFreshReceipt(host, trigger.toLong())) {
                result = NetherPlacementCoordinator.Result.NOT_FRESH;
            } else {
                var outcome = NetherSitePlanner.planLowest(candidate, capture);
                capture.validate();
                plannerStatus = outcome.status().name();
                connectedColumns = outcome.connectedDeepColumns();
                if (outcome.plan() == null) {
                    result = outcome.status() == NetherSitePlanner.Status.BUDGET
                            ? NetherPlacementCoordinator.Result.BUDGET : NetherPlacementCoordinator.Result.PLAN_REJECTED;
                } else {
                    // Acquisition and placement share the same server turn and global budget.
                    var placed = NetherPlacementRuntime.commitPrepared(level, outcome.plan(), admission);
                    return new Report(placed, plannerStatus, capture.reads, connectedColumns, outcome.plan().writes().size());
                }
            }
        } catch (NetherSnapshotDiagnostic.Aborted failure) {
            plannerStatus = "CAPTURE_" + failure.state.name();
            result = failure.state == NetherSnapshotDiagnostic.State.CAPTURE_BUDGET
                    ? NetherPlacementCoordinator.Result.BUDGET : NetherPlacementCoordinator.Result.TERRAIN_CHANGED;
        } finally { capture.discard(); }
        admission.finish(origin, result);
        return new Report(result, plannerStatus, capture.reads, connectedColumns, 0);
    }
}
