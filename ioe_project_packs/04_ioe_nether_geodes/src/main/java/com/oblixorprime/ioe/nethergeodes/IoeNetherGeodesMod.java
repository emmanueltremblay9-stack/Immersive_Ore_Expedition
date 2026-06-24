package com.oblixorprime.ioe.nethergeodes;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeNetherGeodesMod.MODID)
public final class IoeNetherGeodesMod {
    public static final String MODID = "ioe_nether_geodes";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Nether Geodes");

    public IoeNetherGeodesMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeNetherGeodesConfig.SPEC);
        LOGGER.info("Initializing Immersive Ore Expedition: Nether Geodes alpha services");
    }
}
