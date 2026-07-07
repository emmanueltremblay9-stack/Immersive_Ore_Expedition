package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ProvinceResourcePolicy;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class ProvinceRuntimeIntegration {
    private final boolean enabled;
    private final ProvinceBindingResolver bindingResolver;
    private final ProvinceResourcePolicy provinceResourcePolicy;
    private final ProvinceResourcePolicyResolver resourcePolicyResolver;
    private final ResourcePolicyService resourcePolicyService;
    private final LoadedResourceScanner scanner;

    ProvinceRuntimeIntegration(
            boolean enabled,
            ProvinceBindingResolver bindingResolver,
            ProvinceResourcePolicy provinceResourcePolicy,
            ProvinceResourcePolicyResolver resourcePolicyResolver,
            ResourcePolicyService resourcePolicyService,
            LoadedResourceScanner scanner
    ) {
        this.enabled = enabled;
        this.bindingResolver = Objects.requireNonNull(bindingResolver, "bindingResolver");
        this.provinceResourcePolicy = Objects.requireNonNull(provinceResourcePolicy, "provinceResourcePolicy");
        this.resourcePolicyResolver = Objects.requireNonNull(resourcePolicyResolver, "resourcePolicyResolver");
        this.resourcePolicyService = Objects.requireNonNull(resourcePolicyService, "resourcePolicyService");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public static ProvinceRuntimeIntegration fromConfig(
            ResourcePolicyService resourcePolicyService,
            LoadedResourceScanner scanner
    ) {
        if (!IoeWorldgenConfig.provinceRuntimeIntegrationEnabled()) {
            return disabled(resourcePolicyService, scanner);
        }
        return new ProvinceRuntimeIntegration(
                true,
                ProvinceBindingResolver.fromConfig(),
                ProvinceResourcePolicy.fromConfig(),
                ProvinceResourcePolicyResolver.fromConfig(),
                resourcePolicyService,
                scanner
        );
    }

    public static ProvinceRuntimeIntegration disabled(
            ResourcePolicyService resourcePolicyService,
            LoadedResourceScanner scanner
    ) {
        return new ProvinceRuntimeIntegration(
                false,
                ProvinceBindingResolver.defaults(),
                ProvinceResourcePolicy.defaults(),
                ProvinceResourcePolicyResolver.empty(),
                resourcePolicyService,
                scanner
        );
    }

    public boolean enabled() {
        return enabled;
    }

    public ResourcePolicyDecision evaluateOreLoadResource(ExpeditionAnchorRef anchor, ResourceRef resource) {
        return evaluateOreLoadResource(anchor, resource, null);
    }

    public ResourcePolicyDecision evaluateOreLoadResource(
            ExpeditionAnchorRef anchor,
            ResourceRef resource,
            ResourceLocation biomeId
    ) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");

        if (!enabled) {
            return ResourcePolicyDecision.use(
                    "Province runtime integration is disabled; existing ore-load resource policy remains authoritative"
            );
        }

        ResourcePolicyDecision provinceDecision = provinceResourcePolicy.evaluate(resource);
        if (!provinceDecision.shouldUse()) {
            return provinceDecision;
        }

        ProvinceId runtimeProvince = bindingResolver.resolve(biomeId);
        ResourcePolicyDecision provinceResourceRuleDecision =
                resourcePolicyResolver.evaluate(runtimeProvince, resource);
        if (!provinceResourceRuleDecision.shouldUse()) {
            return provinceResourceRuleDecision;
        }

        ResourcePolicyDecision runtimeDecision = resourcePolicyService.evaluate(resource, scanner);
        if (!runtimeDecision.shouldUse()) {
            return runtimeDecision;
        }

        return ResourcePolicyDecision.use(
                "Province runtime integration allowed " + resource.id() + " for " + runtimeProvince
        );
    }
}
