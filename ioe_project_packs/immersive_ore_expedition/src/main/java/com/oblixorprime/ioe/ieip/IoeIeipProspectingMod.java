package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeIeipProspectingMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: IE/IP Prospecting");

    private IoeIeipProspectingMod() {
    }

    public static void bootstrap() {
        LOGGER.info("Initializing Immersive Ore Expedition: IE/IP Prospecting alpha services");
    }
}
