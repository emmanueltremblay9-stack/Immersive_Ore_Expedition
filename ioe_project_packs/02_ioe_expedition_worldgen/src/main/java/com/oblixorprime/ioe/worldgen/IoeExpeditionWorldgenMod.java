package com.oblixorprime.ioe.worldgen;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeExpeditionWorldgenMod.MODID)
public final class IoeExpeditionWorldgenMod {
    public static final String MODID = "ioe_expedition_worldgen";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Worldgen");

    public IoeExpeditionWorldgenMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeWorldgenConfig.SPEC);
        LOGGER.info("Initializing Immersive Ore Expedition: Worldgen alpha services");
    }
}
