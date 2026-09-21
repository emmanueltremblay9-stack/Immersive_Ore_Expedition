package com.oblixorprime.ioe.budding;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public enum BuddingResourceFamily {
    GEORE_ALUMINUM(Kind.GEORE, "aluminum"),
    GEORE_COAL(Kind.GEORE, "coal"),
    GEORE_COPPER(Kind.GEORE, "copper"),
    GEORE_DIAMOND(Kind.GEORE, "diamond"),
    GEORE_EMERALD(Kind.GEORE, "emerald"),
    GEORE_GOLD(Kind.GEORE, "gold"),
    GEORE_IRON(Kind.GEORE, "iron"),
    GEORE_LAPIS(Kind.GEORE, "lapis"),
    GEORE_LEAD(Kind.GEORE, "lead"),
    GEORE_NICKEL(Kind.GEORE, "nickel"),
    GEORE_REDSTONE(Kind.GEORE, "redstone"),
    GEORE_SILVER(Kind.GEORE, "silver"),
    GEORE_URANIUM(Kind.GEORE, "uranium"),
    CERTUS_QUARTZ(Kind.CERTUS, "certus_quartz"),
    ENTROIZED_FLUIX(Kind.EXTENDED_AE, "entroized_fluix");

    private final Kind kind;
    private final String key;

    BuddingResourceFamily(Kind kind, String key) {
        this.kind = kind;
        this.key = key;
    }

    public Kind kind() {
        return kind;
    }

    public String key() {
        return key;
    }

    public String dependencyModId() {
        return switch (kind) {
            case GEORE -> "geore";
            case CERTUS -> "ae2";
            case EXTENDED_AE -> "extendedae";
        };
    }

    public ResourceLocation storageBlockId() {
        return switch (kind) {
            case GEORE -> id("geore", key + "_block");
            case CERTUS, EXTENDED_AE -> id("ae2", "quartz_block");
        };
    }

    public List<ResourceLocation> growthProductIds() {
        return switch (kind) {
            case GEORE -> List.of(
                    id("geore", "small_" + key + "_bud"),
                    id("geore", "medium_" + key + "_bud"),
                    id("geore", "large_" + key + "_bud"),
                    id("geore", key + "_cluster")
            );
            case CERTUS -> List.of(
                    id("ae2", "small_quartz_bud"),
                    id("ae2", "medium_quartz_bud"),
                    id("ae2", "large_quartz_bud"),
                    id("ae2", "quartz_cluster")
            );
            case EXTENDED_AE -> List.of(
                    id("extendedae", "entro_cluster_small"),
                    id("extendedae", "entro_cluster_medium"),
                    id("extendedae", "entro_cluster_large"),
                    id("extendedae", "entro_cluster")
            );
        };
    }

    public Optional<ResourceLocation> observedNativeBuddingId(BuddingRank rank) {
        Objects.requireNonNull(rank, "rank");
        return switch (kind) {
            case GEORE -> Optional.empty();
            case CERTUS -> Optional.of(id("ae2", rank.path() + "_budding_quartz"));
            case EXTENDED_AE -> Optional.of(id("extendedae", switch (rank) {
                case DAMAGED -> "entro_budding_hardly";
                case CHIPPED -> "entro_budding_half";
                case FLAWED -> "entro_budding_mostly";
                case FLAWLESS -> "entro_budding_fully";
            }));
        };
    }

    public static Optional<BuddingResourceFamily> fromGeOreMaterial(String material) {
        Objects.requireNonNull(material, "material");
        for (BuddingResourceFamily family : values()) {
            if (family.kind == Kind.GEORE && family.key.equals(material)) {
                return Optional.of(family);
            }
        }
        return Optional.empty();
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public enum Kind {
        GEORE,
        CERTUS,
        EXTENDED_AE
    }
}
