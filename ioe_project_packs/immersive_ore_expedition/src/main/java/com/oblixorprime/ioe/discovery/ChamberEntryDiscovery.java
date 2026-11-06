package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.server.level.ServerPlayer;

/** Server-authoritative presence in persisted, committed chamber geometry. No terrain reads. */
final class ChamberEntryDiscovery {
    private ChamberEntryDiscovery() { }
    private record Cursor(net.minecraft.resources.ResourceLocation dimension, long chunk, long anchor) { }
    private static final java.util.Map<ServerPlayer, Cursor> CURSORS = new java.util.WeakHashMap<>();
    static void clear() { CURSORS.clear(); }
    static boolean scan(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return false;
        var level = player.serverLevel();
        var data = DiscoveryJournalService.data(level.getServer());
        boolean changed = false;
        long chunk = player.chunkPosition().toLong();
        var previous = CURSORS.get(player);
        long after = previous != null && previous.chunk() == chunk && previous.dimension().equals(level.dimension().location())
                ? previous.anchor() : Long.MIN_VALUE;
        for (var site : ExpeditionLocatorService.index(level).chamberCandidates(level.dimension(), player.blockPosition(), after)) {
            CURSORS.put(player, new Cursor(level.dimension().location(), chunk, site.pos().asLong()));
            if (site.chamberVolume().contains(player.position()))
                changed |= data.recordChamberEntry(player.getUUID(), DiscoverySiteKey.from(site));
        }
        return changed;
    }
}
