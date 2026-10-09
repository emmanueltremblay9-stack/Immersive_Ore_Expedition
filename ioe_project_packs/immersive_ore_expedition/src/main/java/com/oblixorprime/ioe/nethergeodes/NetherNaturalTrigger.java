package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Closed production gate; no config switch or command can enable it. */
final class NetherNaturalTrigger {
    private static final boolean GENERATION_ENABLED = false;
    private NetherNaturalTrigger() { }

    static void tick(ServerTickEvent.Post event) {
        if (!GENERATION_ENABLED) return;
        var level = event.getServer().getLevel(Level.NETHER);
        if (level != null) dispatch(level);
    }

    // Explicit integration-test entry. Uses only the same real lifecycle coordinator as production.
    static Map<Long, NetherPlacementCoordinator.Result> dispatch(ServerLevel level) {
        NetherPlacementRuntime.host(level).requireServerThread();
        return dispatchCandidates(level, NetherPlacementRuntime.coordinator(level)
                .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount()));
    }

    static Map<Long, NetherPlacementCoordinator.Result> dispatchCandidates(ServerLevel level,
                                                                          java.util.List<net.minecraft.world.level.ChunkPos> candidates) {
        NetherPlacementRuntime.host(level).requireServerThread();
        var results = new LinkedHashMap<Long, NetherPlacementCoordinator.Result>();
        for (var chunk : candidates) {
            results.put(chunk.toLong(), NetherNaturalAdmission.attempt(level, chunk));
        }
        return Map.copyOf(results);
    }
}
