package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ProvinceBindingResolver {
    private static final String FALLBACK_DEFAULT_PROVINCE = "immersive_ore_expedition:default";

    private final ProvinceId defaultProvince;
    private final List<BindingRule> bindingRules;

    private ProvinceBindingResolver(ProvinceId defaultProvince, List<BindingRule> bindingRules) {
        this.defaultProvince = Objects.requireNonNull(defaultProvince, "defaultProvince");
        this.bindingRules = List.copyOf(Objects.requireNonNull(bindingRules, "bindingRules"));
    }

    public static ProvinceBindingResolver fromConfig() {
        return parse(
                IoeWorldgenConfig.defaultProvince(),
                IoeWorldgenConfig.biomeProvinceBindings(),
                IoeWorldgenConfig.allowLegacyProvinceNamespaces()
        );
    }

    public static ProvinceBindingResolver defaults() {
        return parse(FALLBACK_DEFAULT_PROVINCE, List.of(), false);
    }

    static ProvinceBindingResolver parse(
            String defaultProvinceSpec,
            Collection<String> bindingSpecs,
            boolean allowLegacyNamespaces
    ) {
        ProvinceId defaultProvince = parseProvince(defaultProvinceSpec, allowLegacyNamespaces)
                .orElseGet(() -> ProvinceId.parse(FALLBACK_DEFAULT_PROVINCE));
        List<BindingRule> rules = new ArrayList<>();
        for (String bindingSpec : bindingSpecs) {
            parseRule(bindingSpec, allowLegacyNamespaces).ifPresent(rules::add);
        }
        return new ProvinceBindingResolver(defaultProvince, rules);
    }

    public ProvinceId resolve(ResourceLocation biomeId) {
        if (biomeId == null) {
            return defaultProvince;
        }
        for (BindingRule rule : bindingRules) {
            if (rule.selector().specificity() == Specificity.EXACT && rule.matches(biomeId)) {
                return rule.province();
            }
        }
        for (BindingRule rule : bindingRules) {
            if (rule.selector().specificity() == Specificity.NAMESPACE && rule.matches(biomeId)) {
                return rule.province();
            }
        }
        return defaultProvince;
    }

    public ProvinceId defaultProvince() {
        return defaultProvince;
    }

    public int bindingCount() {
        return bindingRules.size();
    }

    private static Optional<BindingRule> parseRule(String bindingSpec, boolean allowLegacyNamespaces) {
        if (bindingSpec == null) {
            return Optional.empty();
        }
        String candidate = bindingSpec.trim();
        int separator = candidate.indexOf('=');
        if (separator <= 0 || separator == candidate.length() - 1) {
            return Optional.empty();
        }

        Optional<BiomeSelector> selector = parseSelector(candidate.substring(0, separator));
        Optional<ProvinceId> province = parseProvince(candidate.substring(separator + 1), allowLegacyNamespaces);
        if (selector.isEmpty() || province.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BindingRule(selector.get(), province.get()));
    }

    private static Optional<BiomeSelector> parseSelector(String rawSelector) {
        String selector = rawSelector.trim();
        if (selector.endsWith(":*")) {
            String namespace = selector.substring(0, selector.length() - 2).trim();
            if (isValidNamespace(namespace)) {
                return Optional.of(new BiomeSelector(null, namespace, Specificity.NAMESPACE));
            }
            return Optional.empty();
        }

        return parseLocation(selector).map(location -> new BiomeSelector(location, "", Specificity.EXACT));
    }

    private static Optional<ProvinceId> parseProvince(String provinceSpec, boolean allowLegacyNamespaces) {
        if (provinceSpec == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(ProvinceId.parse(provinceSpec.trim(), allowLegacyNamespaces));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static Optional<ResourceLocation> parseLocation(String candidate) {
        int separator = candidate.indexOf(':');
        if (separator <= 0 || separator == candidate.length() - 1) {
            return Optional.empty();
        }
        try {
            return Optional.of(ResourceLocation.fromNamespaceAndPath(
                    candidate.substring(0, separator),
                    candidate.substring(separator + 1)
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static boolean isValidNamespace(String namespace) {
        try {
            ResourceLocation.fromNamespaceAndPath(namespace, "placeholder");
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private record BindingRule(BiomeSelector selector, ProvinceId province) {
        private BindingRule {
            Objects.requireNonNull(selector, "selector");
            Objects.requireNonNull(province, "province");
        }

        private boolean matches(ResourceLocation biomeId) {
            return selector.matches(biomeId);
        }
    }

    private record BiomeSelector(ResourceLocation exactBiome, String namespace, Specificity specificity) {
        private BiomeSelector {
            Objects.requireNonNull(specificity, "specificity");
            if (specificity == Specificity.EXACT) {
                Objects.requireNonNull(exactBiome, "exactBiome");
            } else if (namespace == null || namespace.isBlank()) {
                throw new IllegalArgumentException("namespace selector must not be blank");
            }
        }

        private boolean matches(ResourceLocation biomeId) {
            return switch (specificity) {
                case EXACT -> exactBiome.equals(biomeId);
                case NAMESPACE -> namespace.equals(biomeId.getNamespace());
            };
        }
    }

    private enum Specificity {
        EXACT,
        NAMESPACE
    }
}
