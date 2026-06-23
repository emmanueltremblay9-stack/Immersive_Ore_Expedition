package com.oblixorprime.ioe.ieip;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeIeipProspectingMod.MODID)
public final class IoeIeipProspectingMod {
    public static final String MODID = "ioe_ieip_prospecting";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: IE/IP Prospecting");

    public IoeIeipProspectingMod() {
        LOGGER.info("Initializing Immersive Ore Expedition: IE/IP Prospecting skeleton");
        // TODO Codex: register config, events, data attachments, and optional compat gates.
    }


}
