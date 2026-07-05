package com.oblixorprime.ioe;

import com.oblixorprime.ioe.config.ImmersiveOreExpeditionConfig;
import com.oblixorprime.ioe.core.IoeCoreMod;
import com.oblixorprime.ioe.crystalgrowth.IoeCrystalGrowthMod;
import com.oblixorprime.ioe.ieip.IoeIeipProspectingMod;
import com.oblixorprime.ioe.nethergeodes.IoeNetherGeodesMod;
import com.oblixorprime.ioe.retrogen.IoeRetrogenAdminMod;
import com.oblixorprime.ioe.worldgen.IoeExpeditionWorldgenMod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ImmersiveOreExpeditionMod.MODID)
public final class ImmersiveOreExpeditionMod {
    public static final String MODID = "immersive_ore_expedition";
    public static final Logger LOGGER = LoggerFactory.getLogger("Immersive Ore Expedition");

    public ImmersiveOreExpeditionMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ImmersiveOreExpeditionConfig.SPEC,
                "immersive_ore_expedition-common.toml");
        IoeCoreMod.bootstrap();
        IoeExpeditionWorldgenMod.bootstrap(modContainer.getEventBus());
        IoeCrystalGrowthMod.bootstrap();
        IoeNetherGeodesMod.bootstrap();
        IoeIeipProspectingMod.bootstrap();
        IoeRetrogenAdminMod.bootstrap();
        LOGGER.info("Initialized Immersive Ore Expedition consolidated services");
    }
}
