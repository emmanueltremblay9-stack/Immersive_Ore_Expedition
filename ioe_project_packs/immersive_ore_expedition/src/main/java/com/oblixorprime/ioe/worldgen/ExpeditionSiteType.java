package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ExpeditionSiteType {
    TINY_VERTICAL_MINE_ENTRANCE(IoeWorldgenFeatureKeys.TINY_VERTICAL_MINE_ENTRANCE, true),
    COLLAPSED_SHAFT(IoeWorldgenFeatureKeys.COLLAPSED_SHAFT, true),
    MINER_CAMP(IoeWorldgenFeatureKeys.MINER_CAMP, true),
    BURIED_SURVEY_MARKER(IoeWorldgenFeatureKeys.BURIED_SURVEY_MARKER, true),
    BASIC_MINESHAFT_CONNECTOR(IoeWorldgenFeatureKeys.BASIC_MINESHAFT_CONNECTOR, false),
    ORE_LOAD_CHAMBER(IoeWorldgenFeatureKeys.ORE_LOAD_CHAMBER, false);

    private static final List<ExpeditionSiteType> NATURAL_SURFACE_SITES = Arrays.stream(values())
            .filter(ExpeditionSiteType::naturalSurfaceSite)
            .toList();

    private final ResourceLocation id;
    private final boolean naturalSurfaceSite;

    ExpeditionSiteType(ResourceLocation id, boolean naturalSurfaceSite) {
        this.id = id;
        this.naturalSurfaceSite = naturalSurfaceSite;
    }

    public ResourceLocation id() {
        return id;
    }

    public boolean naturalSurfaceSite() {
        return naturalSurfaceSite;
    }

    public boolean enabledFromConfig() {
        return switch (this) {
            case TINY_VERTICAL_MINE_ENTRANCE -> IoeWorldgenConfig.tinyVerticalMineEntranceEnabled();
            case COLLAPSED_SHAFT -> IoeWorldgenConfig.collapsedShaftEnabled();
            case MINER_CAMP -> IoeWorldgenConfig.minerCampEnabled();
            case BURIED_SURVEY_MARKER -> IoeWorldgenConfig.buriedSurveyMarkerEnabled();
            case BASIC_MINESHAFT_CONNECTOR -> IoeWorldgenConfig.basicMineshaftConnectorEnabled();
            case ORE_LOAD_CHAMBER -> IoeWorldgenConfig.oreLoadChamberEnabled();
        };
    }

    public static List<ExpeditionSiteType> naturalSurfaceSites() {
        return NATURAL_SURFACE_SITES;
    }

    public static List<ResourceLocation> registeredFeatureIds() {
        return Arrays.stream(values()).map(ExpeditionSiteType::id).toList();
    }

    public static Optional<ExpeditionSiteType> fromId(ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(type -> type.id.equals(id)).findFirst();
    }
}
