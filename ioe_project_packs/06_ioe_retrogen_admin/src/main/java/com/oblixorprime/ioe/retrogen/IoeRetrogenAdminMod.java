package com.oblixorprime.ioe.retrogen;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeRetrogenAdminMod.MODID)
public final class IoeRetrogenAdminMod {
    public static final String MODID = "ioe_retrogen_admin";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Retrogen & Admin");

    public IoeRetrogenAdminMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeRetrogenAdminConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(IoeAdminCommands::registerCommands);
        LOGGER.info("Initializing Immersive Ore Expedition: Retrogen & Admin alpha services");
    }
}
