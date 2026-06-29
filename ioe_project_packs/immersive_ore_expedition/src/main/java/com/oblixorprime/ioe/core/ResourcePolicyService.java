package com.oblixorprime.ioe.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
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
    private static final Set<String> COMMON_TAG_NAMESPACES = Set.of("c", "forge");

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

        return false;
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
        if (resourceRef.type() == ResourceType.MOD) {
            return evaluateMod(resourceRef, scanner);
        }
        if (!isGenericResourcePolicyType(resourceRef.type())) {
            return ResourcePolicyDecision.reject("Generic IOE resource policy requires a block resource or block tag: " + id);
        }
        if (isExcludedResource(id)) {
            return ResourcePolicyDecision.reject("Resource is explicitly excluded by IOE policy: " + id);
        }
        if (!isApprovedResource(resourceRef)) {
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

    private ResourcePolicyDecision evaluateMod(ResourceRef resourceRef, LoadedResourceScanner scanner) {
        String modId = resourceRef.id().getPath();
        if (scanner.isModLoaded(modId)) {
            return ResourcePolicyDecision.use("Optional mod is loaded: " + modId);
        }
        ResourcePolicyDecision decision = shouldSkipMissing(resourceRef.id())
                ? ResourcePolicyDecision.skip("Optional mod is not loaded: " + modId)
                : ResourcePolicyDecision.reject("Optional mod is not loaded: " + modId);
        logMissingResource(resourceRef, decision.reason());
        return decision;
    }

    private boolean isApprovedResource(ResourceRef resourceRef) {
        if (isCommonTagReference(resourceRef)) {
            String path = normalize(resourceRef.id().getPath());
            return containsAnyApprovedToken(path);
        }
        return isApprovedResource(resourceRef.id());
    }

    private static boolean isCommonTagReference(ResourceRef resourceRef) {
        return resourceRef.type() == ResourceType.BLOCK_TAG
                && COMMON_TAG_NAMESPACES.contains(normalize(resourceRef.id().getNamespace()));
    }

    private static boolean isGenericResourcePolicyType(ResourceType type) {
        return type == ResourceType.BLOCK || type == ResourceType.BLOCK_TAG;
    }

    private static boolean containsAnyApprovedToken(String path) {
        return containsAnyToken(path, VANILLA_TOKENS)
                || containsAnyToken(path, IMMERSIVE_ENGINEERING_TOKENS)
                || containsAnyToken(path, AE2_TOKENS)
                || containsAnyToken(path, DRACONIC_EVOLUTION_TOKENS);
    }

    public void logMissingResource(ResourceRef resourceRef, String reason) {
        Objects.requireNonNull(resourceRef, "resourceRef");
        Objects.requireNonNull(reason, "reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (IoeCoreConfig.logMissingResources()) {
            IoeCoreMod.LOGGER.warn("Skipping IOE resource {} ({})", resourceRef, reason);
        }
    }

    private static boolean containsAnyToken(String path, Set<String> tokens) {
        for (String token : tokens) {
            String normalizedToken = normalize(token);
            if (containsTokenInSegment(path, normalizedToken)) {
                return true;
            }
        }
        return false;
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
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
