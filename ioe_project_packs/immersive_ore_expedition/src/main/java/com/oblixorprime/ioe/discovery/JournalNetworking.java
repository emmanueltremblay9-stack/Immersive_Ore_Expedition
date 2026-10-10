package com.oblixorprime.ioe.discovery;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class JournalNetworking {
    private static final String CLIENT = "com.oblixorprime.ioe.discovery.client.JournalClient";
    private JournalNetworking() { }
    public static void register(IEventBus bus) {
        bus.addListener(JournalNetworking::payloads);
        if (FMLEnvironment.dist == Dist.CLIENT) invoke("register", IEventBus.class, bus);
    }
    private static void payloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("ioe-journal-1");
        registrar.playToServer(JournalRequest.TYPE, JournalRequest.CODEC, (request, context) -> {
            if (context.player() instanceof ServerPlayer player)
                PacketDistributor.sendToPlayer(player, response(player, request));
        });
        registrar.playToClient(JournalResponse.TYPE, JournalResponse.CODEC,
                (response, context) -> invoke("receive", JournalResponse.class, response));
    }
    static JournalResponse response(ServerPlayer player, JournalRequest request) {
        return new JournalResponse(request.token(), DiscoveryJournalService.page(player, request.offset()));
    }
    private static void invoke(String method, Class<?> type, Object value) {
        try { Class.forName(CLIENT).getMethod(method, type).invoke(null, value); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException("Journal client bridge failed", e); }
    }
}
