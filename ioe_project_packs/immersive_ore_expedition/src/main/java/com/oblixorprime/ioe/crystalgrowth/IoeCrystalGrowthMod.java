package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeCrystalGrowthMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Crystal Growth");

    private IoeCrystalGrowthMod() {
    }

    public static void bootstrap() {
        LOGGER.info("Initializing Immersive Ore Expedition: Crystal Growth alpha services");
    }
}
