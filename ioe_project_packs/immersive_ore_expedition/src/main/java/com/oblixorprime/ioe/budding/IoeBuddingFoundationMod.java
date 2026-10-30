package com.oblixorprime.ioe.budding;

import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class IoeBuddingFoundationMod {
    private static final Logger LOGGER =
            LoggerFactory.getLogger("Immersive Ore Expedition: Budding Foundation");

    private IoeBuddingFoundationMod() {
    }

    public static void bootstrap(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        IoeBuddingBlocks.register(modEventBus);
        LOGGER.info("Registered {} functional GeOre budding rank blocks", IoeBuddingBlocks.blockItems().size());
    }
}
