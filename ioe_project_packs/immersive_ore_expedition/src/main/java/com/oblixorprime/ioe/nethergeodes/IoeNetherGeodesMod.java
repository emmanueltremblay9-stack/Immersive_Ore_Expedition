package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeNetherGeodesMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Nether Geodes");

    private IoeNetherGeodesMod() {
    }

    public static void bootstrap() {
        LOGGER.info("Initializing Immersive Ore Expedition: Nether Geodes alpha services");
    }
}
