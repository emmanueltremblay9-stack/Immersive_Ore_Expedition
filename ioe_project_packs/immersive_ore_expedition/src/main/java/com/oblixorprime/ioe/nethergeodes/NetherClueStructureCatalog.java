package com.oblixorprime.ioe.nethergeodes;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class NetherClueStructureCatalog {
    public static final ResourceLocation LAVA_SHORE_MARKER = ResourceLocation.fromNamespaceAndPath(IoeNetherGeodesMod.MODID, "lava_shore_marker");
    public static final ResourceLocation BASALT_SURVEY_SPIKE = ResourceLocation.fromNamespaceAndPath(IoeNetherGeodesMod.MODID, "basalt_survey_spike");
    public static final ResourceLocation ASHEN_QUARTZ_RUBBLE = ResourceLocation.fromNamespaceAndPath(IoeNetherGeodesMod.MODID, "ashen_quartz_rubble");
    public static final ResourceLocation RUINED_HEAT_VENT = ResourceLocation.fromNamespaceAndPath(IoeNetherGeodesMod.MODID, "ruined_heat_vent");

    private NetherClueStructureCatalog() {
    }

    public static List<ResourceLocation> enabledClueIds() {
        if (!IoeNetherGeodesConfig.clueStructuresEnabled()) {
            return List.of();
        }
        return List.of(LAVA_SHORE_MARKER, BASALT_SURVEY_SPIKE, ASHEN_QUARTZ_RUBBLE, RUINED_HEAT_VENT);
    }
}
