package com.oblixorprime.ioe.retrogen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IoeRetrogenAdminMod {
    public static final String MODID = ImmersiveOreExpeditionMod.MODID;
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Retrogen & Admin");

    private IoeRetrogenAdminMod() {
    }

    public static void bootstrap() {
        NeoForge.EVENT_BUS.addListener(IoeAdminCommands::registerCommands);
        LOGGER.info("Initializing Immersive Ore Expedition: Retrogen & Admin alpha services");
    }
}
