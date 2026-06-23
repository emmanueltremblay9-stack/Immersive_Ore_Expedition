package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Set;

public final class ResourcePolicyService {
    private static final Set<String> EXCLUDED_TOKENS = Set.of(
            "apatite",
            "tin",
            "platinum",
            "osmium",
            "tungsten",
            "black_quartz",
            "uraninite",
            "monazite"
    );
    private static final Set<String> VANILLA_TOKENS = Set.of(
            "coal",
            "iron",
            "copper",
            "gold",
            "redstone",
            "lapis",
            "diamond",
            "emerald",
            "amethyst",
            "quartz",
            "debris"
    );
    private static final Set<String> IMMERSIVE_ENGINEERING_TOKENS = Set.of(
            "bauxite",
            "aluminum",
            "aluminium",
            "lead",
            "silver",
            "nickel",
            "uranium"
    );
    private static final Set<String> AE2_TOKENS = Set.of(
            "certus",
            "quartz",
            "sky_stone",
            "skystone"
    );
    private static final Set<String> DRACONIC_EVOLUTION_TOKENS = Set.of("draconium");

    public boolean isApprovedResource(ResourceLocation id) {
        if (id == null || isExcludedResource(id)) {
            return false;
        }

        String namespace = normalize(id.getNamespace());
        String path = normalize(id.getPath());
        if (path.contains("fluix")) {
            return false;
        }

        if ("minecraft".equals(namespace)) {
            return containsAnyToken(path, VANILLA_TOKENS);
        }
        if ("immersiveengineering".equals(namespace)) {
            return containsAnyToken(path, IMMERSIVE_ENGINEERING_TOKENS);
        }
        if ("ae2".equals(namespace) || "appeng".equals(namespace)) {
            return containsAnyToken(path, AE2_TOKENS);
        }
        if ("draconicevolution".equals(namespace)) {
            return containsAnyToken(path, DRACONIC_EVOLUTION_TOKENS);
        }
        if ("geore".equals(namespace)) {
            return containsAnyToken(path, VANILLA_TOKENS)
                    || containsAnyToken(path, IMMERSIVE_ENGINEERING_TOKENS)
                    || containsAnyToken(path, AE2_TOKENS)
                    || containsAnyToken(path, DRACONIC_EVOLUTION_TOKENS);
        }

        return containsAnyToken(path, VANILLA_TOKENS)
                || containsAnyToken(path, IMMERSIVE_ENGINEERING_TOKENS)
                || containsAnyToken(path, AE2_TOKENS)
                || containsAnyToken(path, DRACONIC_EVOLUTION_TOKENS);
    }

    public boolean shouldSkipMissing(ResourceLocation id) {
        return IoeCoreConfig.skipMissingResources();
    }

    public boolean isExcludedResource(ResourceLocation id) {
        if (id == null) {
            return true;
        }
        String path = normalize(id.getPath());
        return path.contains("forestry_copper") || containsAnyToken(path, EXCLUDED_TOKENS);
    }

    public ResourcePolicyDecision evaluate(ResourceRef resourceRef, LoadedResourceScanner scanner) {
        if (resourceRef == null) {
            return ResourcePolicyDecision.reject("Resource reference is missing");
        }
        if (scanner == null) {
            return ResourcePolicyDecision.reject("Loaded resource scanner is missing");
        }
        ResourceLocation id = resourceRef.id();
        if (isExcludedResource(id)) {
            return ResourcePolicyDecision.reject("Resource is explicitly excluded by IOE policy: " + id);
        }
        if (!isApprovedResource(id)) {
            return ResourcePolicyDecision.reject("Resource is not on the IOE approved resource policy: " + id);
        }
        if (IoeCoreConfig.existingResourcesOnly() && !scanner.isPresent(resourceRef)) {
            ResourcePolicyDecision decision = shouldSkipMissing(id)
                    ? ResourcePolicyDecision.skip("Resource is approved but not loaded: " + id)
                    : ResourcePolicyDecision.reject("Resource is approved but not loaded: " + id);
            logMissingResource(resourceRef, decision.reason());
            return decision;
        }
        return ResourcePolicyDecision.use("Resource passed IOE policy and runtime availability checks: " + id);
    }

    public void logMissingResource(ResourceRef resourceRef, String reason) {
        if (IoeCoreConfig.logMissingResources()) {
            IoeCoreMod.LOGGER.warn("Skipping IOE resource {} ({})", resourceRef, reason);
        }
    }

    private static boolean containsAnyToken(String path, Set<String> tokens) {
        for (String token : tokens) {
            String normalizedToken = normalize(token);
            if (path.equals(normalizedToken)
                    || path.startsWith(normalizedToken + "_")
                    || path.endsWith("_" + normalizedToken)
                    || path.contains("_" + normalizedToken + "_")
                    || path.contains("/" + normalizedToken + "/")
                    || path.endsWith("/" + normalizedToken)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
