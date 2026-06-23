package com.oblixorprime.ioe.ieip;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeIeipProspectingMod.MODID)
public final class IoeIeipProspectingMod {
    public static final String MODID = "ioe_ieip_prospecting";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: IE/IP Prospecting");

    public IoeIeipProspectingMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeIeipProspectingConfig.SPEC);
        LOGGER.info("Initializing Immersive Ore Expedition: IE/IP Prospecting alpha services");
    }
}
