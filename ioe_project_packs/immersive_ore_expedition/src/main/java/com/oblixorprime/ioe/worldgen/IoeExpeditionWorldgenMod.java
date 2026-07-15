package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeExpeditionWorldgenMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Worldgen");

    private IoeExpeditionWorldgenMod() {
    }

    public static void bootstrap() {
        IoeWorldgenBootstrap.bootstrap();
        LOGGER.info("Initializing Immersive Ore Expedition: Worldgen alpha services");
    }

    public static void bootstrap(IEventBus modEventBus) {
        IoeOreNodeBiomeModifiers.register(modEventBus);
        GeOreAdditionsRestrictions.register();
        IoeWorldgenBootstrap.bootstrap(modEventBus);
        LOGGER.info("Initializing Immersive Ore Expedition: Worldgen alpha services");
    }
}
