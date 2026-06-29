package com.oblixorprime.ioe.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record ProvinceResourcePolicy(
        Set<String> allowedCategories,
        Set<String> deniedCategories,
        Set<String> excludedResourceNames,
        boolean debugDiagnostics
) {
    public static final Set<String> DEFAULT_ALLOWED_CATEGORIES = Set.of(
            "vanilla",
            "immersive_engineering",
            "ae2",
            "geore",
            "draconic_evolution",
            "common_ore_tag"
    );

    public ProvinceResourcePolicy {
        allowedCategories = normalizeSet(Objects.requireNonNull(allowedCategories, "allowedCategories"));
        deniedCategories = normalizeSet(Objects.requireNonNull(deniedCategories, "deniedCategories"));
        excludedResourceNames = normalizeSet(Objects.requireNonNull(excludedResourceNames, "excludedResourceNames"));
    }

    public static ProvinceResourcePolicy defaults() {
        return new ProvinceResourcePolicy(
                DEFAULT_ALLOWED_CATEGORIES,
                Set.of(),
                Set.copyOf(ResourcePolicyService.STRICT_EXCLUDED_RESOURCE_NAMES),
                false
        );
    }

    public static ProvinceResourcePolicy fromConfig() {
        return new ProvinceResourcePolicy(
                Set.copyOf(IoeCoreConfig.allowedResourceCategories()),
                Set.copyOf(IoeCoreConfig.deniedResourceCategories()),
                Set.copyOf(IoeCoreConfig.excludedResourceNames()),
                IoeCoreConfig.resourcePolicyDebugDiagnostics()
        );
    }

    public ResourcePolicyDecision evaluate(ResourceRef resourceRef) {
        if (resourceRef == null) {
            return ResourcePolicyDecision.reject("Province resource reference is missing");
        }
        if (isExcluded(resourceRef)) {
            return ResourcePolicyDecision.reject("Province resource is explicitly excluded: " + resourceRef.id());
        }

        String category = categoryFor(resourceRef);
        if (deniedCategories.contains(category)) {
            return ResourcePolicyDecision.reject("Province resource category is denied: " + category);
        }
        if (!allowedCategories.isEmpty() && !allowedCategories.contains(category)) {
            return ResourcePolicyDecision.reject("Province resource category is not allowed: " + category);
        }
        return ResourcePolicyDecision.use("Province resource category is allowed: " + category);
    }

    public String diagnosticLine(ResourceRef resourceRef, ResourcePolicyDecision decision) {
        Objects.requireNonNull(resourceRef, "resourceRef");
        Objects.requireNonNull(decision, "decision");
        return "resource=" + resourceRef.id()
                + ", category=" + categoryFor(resourceRef)
                + ", action=" + decision.action()
                + ", reason=" + decision.reason();
    }

    public boolean isExcluded(ResourceRef resourceRef) {
        if (resourceRef == null) {
            return true;
        }
        if (new ResourcePolicyService().isExcludedResource(resourceRef.id())) {
            return true;
        }

        String normalizedId = normalize(resourceRef.id().getNamespace() + "_" + resourceRef.id().getPath());
        String normalizedPath = normalize(resourceRef.id().getPath());
        for (String excluded : excludedResourceNames) {
            if (matchesExcludedName(normalizedId, normalizedPath, excluded)) {
                return true;
            }
        }
        return false;
    }

    public static String categoryFor(ResourceRef resourceRef) {
        Objects.requireNonNull(resourceRef, "resourceRef");
        String namespace = normalize(resourceRef.id().getNamespace());

        if (resourceRef.type() == ResourceType.BLOCK_TAG && ("c".equals(namespace) || "forge".equals(namespace))) {
            return "common_ore_tag";
        }
        if ("minecraft".equals(namespace)) {
            return "vanilla";
        }
        if ("immersiveengineering".equals(namespace)) {
            return "immersive_engineering";
        }
        if ("ae2".equals(namespace) || "appeng".equals(namespace)) {
            return "ae2";
        }
        if ("geore".equals(namespace)) {
            return "geore";
        }
        if ("draconicevolution".equals(namespace)) {
            return "draconic_evolution";
        }
        if (resourceRef.type() == ResourceType.MOD) {
            return "optional_mod";
        }
        return "unknown";
    }

    private static Set<String> normalizeSet(Collection<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String normalizedValue = normalize(value);
            if (!normalizedValue.isBlank()) {
                normalized.add(normalizedValue);
            }
        }
        return Collections.unmodifiableSet(normalized);
    }

    private static boolean matchesExcludedName(String normalizedId, String normalizedPath, String excluded) {
        if (excluded.contains("_")) {
            return normalizedPath.contains(excluded) || normalizedId.contains(excluded);
        }
        return containsTokenInSegment(normalizedPath, excluded) || containsTokenInSegment(normalizedId, excluded);
    }

    private static boolean containsTokenInSegment(String path, String normalizedToken) {
        for (String segment : path.split("/")) {
            if (segment.equals(normalizedToken)
                    || segment.startsWith(normalizedToken + "_")
                    || segment.endsWith("_" + normalizedToken)
                    || segment.contains("_" + normalizedToken + "_")) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
