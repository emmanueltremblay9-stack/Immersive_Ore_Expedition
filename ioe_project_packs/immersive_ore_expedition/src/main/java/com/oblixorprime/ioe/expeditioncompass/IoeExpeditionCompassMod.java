package com.oblixorprime.ioe.expeditioncompass;

import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class IoeExpeditionCompassMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Expedition Compass");

    private static final String CLIENT_DIST_NAME = "CLIENT";
    private static final String FML_ENVIRONMENT_CLASS = "net.neoforged.fml.loading.FMLEnvironment";
    private static final String CLIENT_REGISTRATION_CLASS =
            "com.oblixorprime.ioe.expeditioncompass.client.ExpeditionCompassClient";

    private IoeExpeditionCompassMod() {
    }

    public static void bootstrap(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        IoeCompassDataComponents.register(modEventBus);
        IoeCompassNetworking.register(modEventBus);
        IoeItems.register(modEventBus);
        IoeCreativeModeTabs.register(modEventBus);
        registerClientOnly(modEventBus);
        LOGGER.info("Registered Immersive Ore Expedition item services");
    }

    private static void registerClientOnly(IEventBus modEventBus) {
        if (!isClientDist()) {
            return;
        }

        try {
            Class.forName(CLIENT_REGISTRATION_CLASS)
                    .getMethod("register", IEventBus.class)
                    .invoke(null, modEventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register expedition compass client services", exception);
        }
    }

    private static boolean isClientDist() {
        try {
            Object dist = Class.forName(FML_ENVIRONMENT_CLASS).getField("dist").get(null);
            return dist != null && CLIENT_DIST_NAME.equals(dist.toString());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to detect NeoForge distribution for expedition compass setup", exception);
        }
    }
}
