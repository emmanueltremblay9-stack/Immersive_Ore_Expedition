package com.oblixorprime.ioe.crystalgrowth;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeCrystalGrowthMod.MODID)
public final class IoeCrystalGrowthMod {
    public static final String MODID = "ioe_crystal_growth";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Crystal Growth");

    public IoeCrystalGrowthMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeCrystalGrowthConfig.SPEC);
        LOGGER.info("Initializing Immersive Ore Expedition: Crystal Growth alpha services");
    }
}
