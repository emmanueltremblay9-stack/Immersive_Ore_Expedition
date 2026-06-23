package com.oblixorprime.ioe.nethergeodes;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeNetherGeodesMod.MODID)
public final class IoeNetherGeodesMod {
    public static final String MODID = "ioe_nether_geodes";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Nether Geodes");

    public IoeNetherGeodesMod() {
        LOGGER.info("Initializing Immersive Ore Expedition: Nether Geodes skeleton");
        // TODO Codex: register config, events, data attachments, and optional compat gates.
    }


}
