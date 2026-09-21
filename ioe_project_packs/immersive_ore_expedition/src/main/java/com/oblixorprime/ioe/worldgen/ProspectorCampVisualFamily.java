package com.oblixorprime.ioe.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import java.util.Objects;

/** Visual-only biome classification. This resolver never selects or changes an IOE mineral profile. */
public enum ProspectorCampVisualFamily {
    AQUATIC,
    SNOWY,
    VOLCANIC,
    ARID,
    WETLAND,
    TROPICAL,
    ROCKY,
    CONIFER,
    TEMPERATE;

    public static final TagKey<Biome> VOLCANIC_CAMPS = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(
                    "immersive_ore_expedition",
                    "prospector_camp/volcanic"
            )
    );

    /** Priority is explicit so overlapping modded-biome tags cannot produce competing palettes. */
    public static ProspectorCampVisualFamily resolve(Holder<Biome> biome) {
        Objects.requireNonNull(biome, "biome");
        if (isAny(biome, Tags.Biomes.IS_AQUATIC, Tags.Biomes.IS_OCEAN, Tags.Biomes.IS_RIVER,
                Tags.Biomes.IS_BEACH)) {
            return AQUATIC;
        }
        if (isAny(biome, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_ICY)) {
            return SNOWY;
        }
        if (biome.is(VOLCANIC_CAMPS)) {
            return VOLCANIC;
        }
        if (isAny(biome, Tags.Biomes.IS_DESERT, Tags.Biomes.IS_BADLANDS, Tags.Biomes.IS_SAVANNA)) {
            return ARID;
        }
        if (biome.is(Tags.Biomes.IS_SWAMP)) {
            return WETLAND;
        }
        if (biome.is(Tags.Biomes.IS_JUNGLE)) {
            return TROPICAL;
        }
        if (isAny(biome, Tags.Biomes.IS_MOUNTAIN, Tags.Biomes.IS_MOUNTAIN_PEAK,
                Tags.Biomes.IS_MOUNTAIN_SLOPE, Tags.Biomes.IS_HILL, Tags.Biomes.IS_WINDSWEPT)) {
            return ROCKY;
        }
        if (isAny(biome, Tags.Biomes.IS_TAIGA, Tags.Biomes.IS_CONIFEROUS_TREE)) {
            return CONIFER;
        }
        return TEMPERATE;
    }

    @SafeVarargs
    private static boolean isAny(Holder<Biome> biome, TagKey<Biome>... tags) {
        for (TagKey<Biome> tag : tags) {
            if (biome.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
