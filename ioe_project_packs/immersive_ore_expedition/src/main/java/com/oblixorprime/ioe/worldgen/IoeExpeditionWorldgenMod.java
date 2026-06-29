package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeExpeditionWorldgenMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Worldgen");

    private IoeExpeditionWorldgenMod() {
    }

    public static void bootstrap() {
        LOGGER.info("Initializing Immersive Ore Expedition: Worldgen alpha services");
    }
}
