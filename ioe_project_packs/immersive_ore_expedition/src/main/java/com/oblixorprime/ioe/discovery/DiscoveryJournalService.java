package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.util.List;

/** Server persistence and verified-evidence integration boundary. */
public final class DiscoveryJournalService {
    private DiscoveryJournalService() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(DiscoveryJournalService::onServerStarted);
        ProximityDiscovery.register();
    }

    private static void onServerStarted(ServerStartedEvent event) {
        data(event.getServer());
    }

    static DiscoveryJournalData data(MinecraftServer server) {
        if (!server.isSameThread()) throw new IllegalStateException("Discovery journal requires server thread");
        return server.overworld().getDataStorage().computeIfAbsent(DiscoveryJournalData.FACTORY, DiscoveryJournalData.NAME);
    }

    public static List<DiscoveryView> journal(ServerPlayer player) {
        return data(player.serverLevel().getServer()).views(player.getUUID());
    }

    public static DiscoveryPage page(ServerPlayer player, int offset) {
        return data(player.serverLevel().getServer()).page(player.getUUID(), offset);
    }

    /**
     * Only a trusted server evidence producer may call this after validating the actual
     * observation/probe. This checks placement and identity, not line of sight or distance.
     * A true return is the sole new-stage signal; repeated/old/out-of-order input returns false.
     */
    public static boolean recordVerifiedEvidence(ServerPlayer player, DiscoverySiteKey key, DiscoveryEvidence evidence) {
        var level = player.serverLevel();
        var data = data(level.getServer());
        if (!key.dimension().equals(level.dimension().location())) return false;
        boolean placed = ExpeditionLocatorService.index(level)
                .hasNaturalDiscoveryAnchor(level.dimension(), key.anchor());
        return placed && data.advance(player.getUUID(), key, evidence);
    }
}
