package com.oblixorprime.ioe.expeditioncompass;

import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class IoeExpeditionCompassMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Expedition Compass");

    private IoeExpeditionCompassMod() {
    }

    public static void bootstrap(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        IoeItems.register(modEventBus);
        IoeCreativeModeTabs.register(modEventBus);
        LOGGER.info("Registered Immersive Ore Expedition item services");
    }
}
