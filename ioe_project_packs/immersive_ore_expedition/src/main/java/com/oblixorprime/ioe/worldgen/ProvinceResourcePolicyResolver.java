package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class ProvinceResourcePolicyResolver {
    private final List<ResourceRule> rules;

    private ProvinceResourcePolicyResolver(List<ResourceRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public static ProvinceResourcePolicyResolver fromConfig() {
        return parse(
                IoeWorldgenConfig.provinceResourcePolicyRules(),
                IoeWorldgenConfig.allowLegacyProvinceNamespaces()
        );
    }

    public static ProvinceResourcePolicyResolver empty() {
        return parse(List.of(), false);
    }

    static ProvinceResourcePolicyResolver parse(Collection<String> ruleSpecs, boolean allowLegacyNamespaces) {
        List<ResourceRule> parsedRules = new ArrayList<>();
        Collection<String> specs = ruleSpecs == null ? List.of() : ruleSpecs;
        for (String ruleSpec : specs) {
            parseRule(ruleSpec, allowLegacyNamespaces).ifPresent(parsedRules::add);
        }
        return new ProvinceResourcePolicyResolver(parsedRules);
    }

    public ResourcePolicyDecision evaluate(ProvinceId provinceId, ResourceRef resourceRef) {
        Objects.requireNonNull(provinceId, "provinceId");
        Objects.requireNonNull(resourceRef, "resourceRef");

        for (ResourceRule rule : rules) {
            if (rule.matches(provinceId, resourceRef.id())) {
                return rule.toDecision(resourceRef);
            }
        }

        return ResourcePolicyDecision.use(
                "No configured province resource policy rule matched " + resourceRef.id() + " for " + provinceId
        );
    }

    public int ruleCount() {
        return rules.size();
    }

    private static Optional<ResourceRule> parseRule(String ruleSpec, boolean allowLegacyNamespaces) {
        if (ruleSpec == null) {
            return Optional.empty();
        }
        String[] parts = ruleSpec.trim().split("\\|", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }

        Optional<ProvinceId> province = parseProvince(parts[0], allowLegacyNamespaces);
        Optional<ResourceLocation> resource = parseResourceId(parts[1]);
        Optional<RuleDecision> decision = RuleDecision.parse(parts[2]);
        if (province.isEmpty() || resource.isEmpty() || decision.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ResourceRule(province.get(), resource.get(), decision.get()));
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

    private static Optional<ResourceLocation> parseResourceId(String resourceSpec) {
        if (resourceSpec == null) {
            return Optional.empty();
        }
        String candidate = resourceSpec.trim();
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

    private record ResourceRule(ProvinceId province, ResourceLocation resourceId, RuleDecision decision) {
        private ResourceRule {
            Objects.requireNonNull(province, "province");
            Objects.requireNonNull(resourceId, "resourceId");
            Objects.requireNonNull(decision, "decision");
        }

        private boolean matches(ProvinceId runtimeProvince, ResourceLocation resource) {
            return province.equals(runtimeProvince) && resourceId.equals(resource);
        }

        private ResourcePolicyDecision toDecision(ResourceRef resourceRef) {
            return switch (decision) {
                case ALLOW -> ResourcePolicyDecision.use(
                        "Province resource policy rule allows " + resourceRef.id() + " for " + province
                );
                case DENY -> ResourcePolicyDecision.reject(
                        "Province resource policy rule denies " + resourceRef.id() + " for " + province
                );
                case EXCLUDE -> ResourcePolicyDecision.reject(
                        "Province resource policy rule excludes " + resourceRef.id() + " for " + province
                );
            };
        }
    }

    private enum RuleDecision {
        ALLOW,
        DENY,
        EXCLUDE;

        private static Optional<RuleDecision> parse(String value) {
            if (value == null) {
                return Optional.empty();
            }
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "allow" -> Optional.of(ALLOW);
                case "deny" -> Optional.of(DENY);
                case "exclude" -> Optional.of(EXCLUDE);
                default -> Optional.empty();
            };
        }
    }
}
