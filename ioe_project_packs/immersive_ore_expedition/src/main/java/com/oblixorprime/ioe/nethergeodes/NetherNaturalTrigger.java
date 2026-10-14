package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Closed production gate; no config switch or command can enable it. */
final class NetherNaturalTrigger {
    private static final boolean GENERATION_ENABLED = NetherWorldActivation.PRODUCTION_ENABLED;
    private NetherNaturalTrigger() { }

    static void tick(ServerTickEvent.Post event) {
        if (!GENERATION_ENABLED) return;
        var level = event.getServer().getLevel(Level.NETHER);
        if (level != null && NetherWorldActivation.permits(level)) dispatch(level);
    }

    // Explicit integration-test entry. Uses only the same real lifecycle coordinator as production.
    static Map<Long, NetherPlacementCoordinator.Result> dispatch(ServerLevel level) {
        NetherPlacementRuntime.host(level).requireServerThread();
        if (!NetherWorldActivation.permits(level)) return Map.of();
        return dispatchCandidates(level, NetherPlacementRuntime.coordinator(level)
                .takeNaturalCandidates(level.getSeed(), level.getServer().getTickCount(), chunk -> neighborhoodReady(level, chunk)));
    }

    private static boolean neighborhoodReady(ServerLevel level, net.minecraft.world.level.ChunkPos chunk) {
        // Metadata-only, loaded-only readiness for the canonical 74x74 window. Never generate/ticket.
        for (int z=-2;z<=2;z++) for(int x=-2;x<=2;x++)
            if(level.getChunkSource().getChunkNow(chunk.x+x,chunk.z+z)==null) return false;
        return true;
    }

    static Map<Long, NetherPlacementCoordinator.Result> dispatchCandidates(ServerLevel level,
                                                                          java.util.List<net.minecraft.world.level.ChunkPos> candidates) {
        NetherPlacementRuntime.host(level).requireServerThread();
        var results = new LinkedHashMap<Long, NetherPlacementCoordinator.Result>();
        for (var chunk : candidates) {
            try {
                results.put(chunk.toLong(), NetherNaturalAdmission.attempt(level, chunk));
            } finally {
                NetherPlacementRuntime.coordinator(level).endNaturalAdmission(chunk);
            }
        }
        return Map.copyOf(results);
    }
}
