package com.oblixorprime.ioe.retrogen;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeRetrogenAdminMod.MODID)
public final class IoeRetrogenAdminMod {
    public static final String MODID = "ioe_retrogen_admin";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Retrogen & Admin");

    public IoeRetrogenAdminMod() {
        LOGGER.info("Initializing Immersive Ore Expedition: Retrogen & Admin skeleton");
        // TODO Codex: register config, events, data attachments, and optional compat gates.
    }


}
