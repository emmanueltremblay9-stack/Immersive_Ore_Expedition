package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Explicit admission with deterministic lowest-surface selection. Not registered with a command, tick or generator. */
final class NetherNaturalAdmission {
    private NetherNaturalAdmission() { }

    record Report(NetherPlacementCoordinator.Result result, String plannerStatus, int worldReads,
                  int connectedColumns, int plannedWrites, java.util.List<BlockPos> committedShoreMarkers) {
        Report(NetherPlacementCoordinator.Result result,String plannerStatus,int worldReads,int connectedColumns,int plannedWrites) {
            this(result,plannerStatus,worldReads,connectedColumns,plannedWrites,java.util.List.of());
        }
        Report { committedShoreMarkers=java.util.List.copyOf(committedShoreMarkers); }
    }

    static NetherPlacementCoordinator.Result attempt(ServerLevel level, ChunkPos trigger) {
        return attemptDetailed(level, trigger).result();
    }

    static Report attemptDetailed(ServerLevel level, ChunkPos trigger) {
        var host = NetherPlacementRuntime.host(level);
        host.requireServerThread();
        if (!NetherWorldActivation.permits(level))
            return new Report(NetherPlacementCoordinator.Result.BACKEND_UNVERIFIED, "WORLD_NOT_ENABLED", 0, 0, 0);
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
                    // Read-only evidence from the actual committed plan; no additional terrain scan or write.
                    var markers=placed==NetherPlacementCoordinator.Result.COMMITTED
                            ? outcome.plan().writes().entrySet().stream()
                                .filter(entry->entry.getValue().is(net.minecraft.world.level.block.Blocks.BLACKSTONE))
                                .map(entry->entry.getKey().immutable()).toList()
                            : java.util.List.<BlockPos>of();
                    return new Report(placed, plannerStatus, capture.reads, connectedColumns, outcome.plan().writes().size(),markers);
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
