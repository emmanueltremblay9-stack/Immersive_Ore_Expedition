package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.server.MinecraftServer;
import java.util.Map;
import java.util.WeakHashMap;

/** Shared across all Nether analyses for a server, never per command or per dimension. */
final class NetherAnalysisBudget {
    static final int READS_PER_TICK = 65_536;
    private static final Map<MinecraftServer, NetherAnalysisBudget> SERVERS = new WeakHashMap<>();
    private int tick;
    private boolean initialized;
    private int remaining;

    static synchronized NetherAnalysisBudget forServer(MinecraftServer server) {
        return SERVERS.computeIfAbsent(server, ignored -> new NetherAnalysisBudget());
    }

    synchronized boolean acquire(int currentTick) {
        if (!initialized || currentTick != tick) {
            initialized = true;
            tick = currentTick;
            remaining = READS_PER_TICK;
        }
        if (remaining == 0) return false;
        remaining--;
        return true;
    }
}
