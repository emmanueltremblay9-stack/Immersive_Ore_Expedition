package com.oblixorprime.ioe.worldgen;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ExpeditionStructureRegistry {
    public static final ResourceLocation TINY_VERTICAL_MINE_ENTRANCE = id("tiny_vertical_mine_entrance");
    public static final ResourceLocation COLLAPSED_SHAFT = id("collapsed_shaft");
    public static final ResourceLocation MINER_CAMP = id("miner_camp");
    public static final ResourceLocation BURIED_SURVEY_MARKER = id("buried_survey_marker");
    public static final ResourceLocation BASIC_MINESHAFT_CONNECTOR = id("basic_mineshaft_connector");
    public static final ResourceLocation ORE_LOAD_CHAMBER = id("ore_load_chamber");

    private ExpeditionStructureRegistry() {
    }

    public static void register() {
        IoeExpeditionWorldgenMod.LOGGER.info("IOE worldgen structure data is cataloged; runtime structure registration is deferred to the data-driven alpha slice.");
    }

    public static List<ResourceLocation> enabledStructureIds() {
        List<ResourceLocation> enabled = new ArrayList<>();
        addIfEnabled(enabled, IoeWorldgenConfig.tinyVerticalMineEntranceEnabled(), TINY_VERTICAL_MINE_ENTRANCE);
        addIfEnabled(enabled, IoeWorldgenConfig.collapsedShaftEnabled(), COLLAPSED_SHAFT);
        addIfEnabled(enabled, IoeWorldgenConfig.minerCampEnabled(), MINER_CAMP);
        addIfEnabled(enabled, IoeWorldgenConfig.buriedSurveyMarkerEnabled(), BURIED_SURVEY_MARKER);
        addIfEnabled(enabled, IoeWorldgenConfig.basicMineshaftConnectorEnabled(), BASIC_MINESHAFT_CONNECTOR);
        addIfEnabled(enabled, IoeWorldgenConfig.oreLoadChamberEnabled(), ORE_LOAD_CHAMBER);
        return List.copyOf(enabled);
    }

    public static boolean isEnabledStructureId(String anchorType) {
        if (anchorType == null || anchorType.isBlank()) {
            return false;
        }
        return enabledStructureIds().stream()
                .anyMatch(id -> anchorType.equals(id.toString()) || anchorType.equals(id.getPath()));
    }

    private static void addIfEnabled(List<ResourceLocation> enabled, boolean condition, ResourceLocation id) {
        if (condition) {
            enabled.add(id);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(IoeExpeditionWorldgenMod.MODID, path);
    }
}
