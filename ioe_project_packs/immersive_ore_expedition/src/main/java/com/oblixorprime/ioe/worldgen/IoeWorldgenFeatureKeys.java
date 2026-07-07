package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class IoeWorldgenFeatureKeys {
    public static final ResourceLocation ORE_LOAD_CHAMBER = id("ore_load_chamber");
    public static final ResourceLocation TINY_VERTICAL_MINE_ENTRANCE = id("tiny_vertical_mine_entrance");
    public static final ResourceLocation COLLAPSED_SHAFT = id("collapsed_shaft");
    public static final ResourceLocation MINER_CAMP = id("miner_camp");
    public static final ResourceLocation BURIED_SURVEY_MARKER = id("buried_survey_marker");
    public static final ResourceLocation BASIC_MINESHAFT_CONNECTOR = id("basic_mineshaft_connector");
    public static final ResourceLocation IE_MINERAL_OUTCROP = id("ie_mineral_outcrop");
    public static final ResourceLocation IP_RESERVOIR_SEEP = id("ip_reservoir_seep");
    public static final ResourceLocation AMETHYST_GROWTH_SITE = id("amethyst_growth_site");
    public static final ResourceLocation AE2_CERTUS_GROWTH_SITE = id("ae2_certus_growth_site");
    public static final ResourceLocation GEORE_GROWTH_SITE = id("geore_growth_site");
    public static final ResourceLocation METEORITIC_AE2_GEODE = id("meteoritic_ae2_geode");
    public static final ResourceLocation SUB_LAVA_GEODE = id("sub_lava_geode");

    private static final List<ResourceLocation> ANCHOR_FEATURE_KEYS = List.of(
            TINY_VERTICAL_MINE_ENTRANCE,
            COLLAPSED_SHAFT,
            MINER_CAMP,
            BURIED_SURVEY_MARKER,
            BASIC_MINESHAFT_CONNECTOR
    );

    private static final List<ResourceLocation> ORE_LOAD_CHAMBER_FEATURE_KEYS = List.of(
            ORE_LOAD_CHAMBER
    );

    private static final List<ResourceLocation> ALL_FEATURE_KEYS = List.of(
            ORE_LOAD_CHAMBER,
            TINY_VERTICAL_MINE_ENTRANCE,
            COLLAPSED_SHAFT,
            MINER_CAMP,
            BURIED_SURVEY_MARKER,
            BASIC_MINESHAFT_CONNECTOR,
            IE_MINERAL_OUTCROP,
            IP_RESERVOIR_SEEP,
            AMETHYST_GROWTH_SITE,
            AE2_CERTUS_GROWTH_SITE,
            GEORE_GROWTH_SITE,
            METEORITIC_AE2_GEODE,
            SUB_LAVA_GEODE
    );

    private IoeWorldgenFeatureKeys() {
    }

    public static List<ResourceLocation> allFeatureKeys() {
        return ALL_FEATURE_KEYS;
    }

    public static List<ResourceLocation> anchorFeatureKeys() {
        return ANCHOR_FEATURE_KEYS;
    }

    public static List<ResourceLocation> oreLoadChamberFeatureKeys() {
        return ORE_LOAD_CHAMBER_FEATURE_KEYS;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, path);
    }
}
