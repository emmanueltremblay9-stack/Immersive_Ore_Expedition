package com.oblixorprime.ioe.core;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IoeCoreMod.MODID)
public final class IoeCoreMod {
    public static final String MODID = "ioe_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition: Core");
    public static final ResourcePolicyService RESOURCE_POLICY = new ResourcePolicyService();

    public IoeCoreMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, IoeCoreConfig.SPEC);
        LOGGER.info("Initializing Immersive Ore Expedition: Core resource policy services");
    }
}
