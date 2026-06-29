package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

public record ProvinceId(ResourceLocation id) {
    public static final String CONSOLIDATED_NAMESPACE = "immersive_ore_expedition";
    public static final Set<String> LEGACY_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    public ProvinceId {
        Objects.requireNonNull(id, "id");
    }

    public static ProvinceId of(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        if (!CONSOLIDATED_NAMESPACE.equals(id.getNamespace())) {
            throw new IllegalArgumentException("new province ids must use namespace " + CONSOLIDATED_NAMESPACE);
        }
        return new ProvinceId(id);
    }

    public static ProvinceId parse(String value) {
        return parse(value, false);
    }

    public static ProvinceId parse(String value, boolean allowLegacyNamespace) {
        Objects.requireNonNull(value, "value");
        String candidate = value.trim();
        if (candidate.isBlank()) {
            throw new IllegalArgumentException("province id must not be blank");
        }

        ResourceLocation parsed = parseLocation(candidate);
        String namespace = parsed.getNamespace();
        if (CONSOLIDATED_NAMESPACE.equals(namespace) || allowLegacyNamespace && isLegacyNamespace(namespace)) {
            return new ProvinceId(parsed);
        }

        throw new IllegalArgumentException("province id must use namespace " + CONSOLIDATED_NAMESPACE
                + (allowLegacyNamespace ? " or a documented legacy IOE namespace" : ""));
    }

    public static ProvinceId legacy(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        if (!isLegacyNamespace(id.getNamespace())) {
            throw new IllegalArgumentException("legacy province namespace is not documented for IOE: " + namespace);
        }
        return new ProvinceId(id);
    }

    public String namespace() {
        return id.getNamespace();
    }

    public String path() {
        return id.getPath();
    }

    public boolean isConsolidatedNamespace() {
        return CONSOLIDATED_NAMESPACE.equals(namespace());
    }

    public boolean isLegacyNamespace() {
        return isLegacyNamespace(namespace());
    }

    public static boolean isLegacyNamespace(String namespace) {
        return namespace != null && LEGACY_NAMESPACES.contains(namespace);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private static ResourceLocation parseLocation(String candidate) {
        int separator = candidate.indexOf(':');
        try {
            if (separator >= 0) {
                return ResourceLocation.fromNamespaceAndPath(
                        candidate.substring(0, separator),
                        candidate.substring(separator + 1)
                );
            }
            return ResourceLocation.fromNamespaceAndPath(CONSOLIDATED_NAMESPACE, candidate);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid province id: " + candidate, exception);
        }
    }
}
