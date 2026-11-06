package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.server.level.ServerPlayer;

/** Server-authoritative presence in persisted, committed chamber geometry. No terrain reads. */
final class ChamberEntryDiscovery {
    private ChamberEntryDiscovery() { }
    static boolean scan(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return false;
        var level = player.serverLevel();
        var data = DiscoveryJournalService.data(level.getServer());
        boolean changed = false;
        for (var site : ExpeditionLocatorService.index(level).chamberCandidates(level.dimension(), player.blockPosition())) {
            if (site.chamberVolume().contains(player.position()))
                changed |= data.recordChamberEntry(player.getUUID(), DiscoverySiteKey.from(site));
        }
        return changed;
    }
}
