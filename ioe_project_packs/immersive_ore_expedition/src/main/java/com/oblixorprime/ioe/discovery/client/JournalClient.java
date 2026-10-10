package com.oblixorprime.ioe.discovery.client;

import com.oblixorprime.ioe.discovery.JournalResponse;
import com.oblixorprime.ioe.discovery.JournalSession;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class JournalClient {
    static final JournalSession SESSION = new JournalSession();
    private JournalClient() { }
    public static void register(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(JournalClient::logout);
        NeoForge.EVENT_BUS.addListener(JournalClient::commands);
        if (ModList.get().isLoaded("immersiveengineering")) bus.addListener(JournalClient::loadComplete);
    }
    private static void commands(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event) {
        event.getDispatcher().register(com.mojang.brigadier.builder.LiteralArgumentBuilder
                .<net.minecraft.commands.CommandSourceStack>literal("ioejournal").executes(context -> {
                    // Defer past chat's own close callback so the journal remains open.
                    Minecraft.getInstance().tell(() -> JournalScreen.open(null));
                    return 1;
                }));
    }
    private static void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            // No IE type is resolved in the IE-absent path or on a dedicated server.
            try { Class.forName("com.oblixorprime.ioe.discovery.client.ie.JournalManualEntry").getMethod("register").invoke(null); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException("IE journal registration failed", e); }
        });
    }
    private static void logout(ClientPlayerNetworkEvent.LoggingOut event) { SESSION.clear(); }
    public static void request(int offset) {
        if (Minecraft.getInstance().getConnection() == null) { SESSION.clear(); return; }
        PacketDistributor.sendToServer(SESSION.request(offset));
    }
    public static void receive(JournalResponse response) {
        if (Minecraft.getInstance().getConnection() == null) return;
        if (SESSION.accept(response) && Minecraft.getInstance().screen instanceof JournalScreen screen) screen.refresh();
    }
}
