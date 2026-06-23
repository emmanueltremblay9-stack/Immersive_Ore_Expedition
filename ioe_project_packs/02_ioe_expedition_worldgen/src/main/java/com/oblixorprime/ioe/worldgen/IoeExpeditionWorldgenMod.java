package com.oblixorprime.ioe.worldgen;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeExpeditionWorldgenMod.MODID)
public final class IoeExpeditionWorldgenMod {
    public static final String MODID = "ioe_expedition_worldgen";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Worldgen");

    public IoeExpeditionWorldgenMod() {
        LOGGER.info("Initializing Immersive Ore Expedition: Worldgen skeleton");
        // TODO Codex: register config, events, data attachments, and optional compat gates.
    }


}
