package com.oblixorprime.ioe.surface02;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Passive biome input for Surface 02. The observed categories cover the families already named by
 * the 0.2.53 biome data; aquatic and shoreline inputs remain explicit refusals.
 */
public enum Surface02BiomeFamily {
    TEMPERATE(Canonical.TEMPERATE),
    CONIFER(Canonical.TEMPERATE),
    TROPICAL(Canonical.WET),
    ARID(Canonical.ARID),
    COLD(Canonical.COLD),
    WET(Canonical.WET),
    VOLCANIC(Canonical.RELIEF),
    ROCKY(Canonical.RELIEF),
    MOUNTAIN(Canonical.RELIEF),
    AQUATIC(null),
    SHORELINE(null);

    private final Canonical canonical;

    Surface02BiomeFamily(Canonical canonical) {
        this.canonical = canonical;
    }

    public Optional<Canonical> canonical() {
        return Optional.ofNullable(canonical);
    }

    public boolean refusedByCurrentPipeline() {
        return canonical == null;
    }

    public enum Canonical {
        TEMPERATE(new Palette("oak_log", "oak_planks", "cobblestone")),
        ARID(new Palette("stripped_acacia_log", "acacia_planks", "sandstone")),
        COLD(new Palette("spruce_log", "spruce_planks", "cobbled_deepslate")),
        WET(new Palette("mangrove_log", "mangrove_planks", "mossy_cobblestone")),
        RELIEF(new Palette("dark_oak_log", "spruce_planks", "stone_bricks"));

        private final Palette palette;

        Canonical(Palette palette) {
            this.palette = palette;
        }

        public Palette palette() {
            return palette;
        }
    }

    public record Palette(
            ResourceLocation supportBlockId,
            ResourceLocation plankBlockId,
            ResourceLocation facingBlockId
    ) {
        private Palette(String supportPath, String plankPath, String facingPath) {
            this(vanilla(supportPath), vanilla(plankPath), vanilla(facingPath));
        }

        public Palette {
            Objects.requireNonNull(supportBlockId, "supportBlockId");
            Objects.requireNonNull(plankBlockId, "plankBlockId");
            Objects.requireNonNull(facingBlockId, "facingBlockId");
        }

        private static ResourceLocation vanilla(String path) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", path);
        }
    }
}
