package com.oblixorprime.ioe.core;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeCoreMod.MODID)
public final class IoeCoreMod {
    public static final String MODID = "ioe_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Core");

    public IoeCoreMod() {
        LOGGER.info("Initializing Immersive Ore Expedition: Core skeleton");
        // TODO Codex: register config, events, data attachments, and optional compat gates.
    }


}
