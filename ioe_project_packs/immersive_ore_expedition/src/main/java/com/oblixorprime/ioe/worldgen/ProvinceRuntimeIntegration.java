package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ProvinceResourcePolicy;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;

import java.util.Objects;

public final class ProvinceRuntimeIntegration {
    private final boolean enabled;
    private final String provinceNamespace;
    private final boolean allowLegacyNamespaces;
    private final ProvinceResourcePolicy provinceResourcePolicy;
    private final ResourcePolicyService resourcePolicyService;
    private final LoadedResourceScanner scanner;

    ProvinceRuntimeIntegration(
            boolean enabled,
            String provinceNamespace,
            boolean allowLegacyNamespaces,
            ProvinceResourcePolicy provinceResourcePolicy,
            ResourcePolicyService resourcePolicyService,
            LoadedResourceScanner scanner
    ) {
        this.enabled = enabled;
        this.provinceNamespace = Objects.requireNonNull(provinceNamespace, "provinceNamespace").trim();
        this.allowLegacyNamespaces = allowLegacyNamespaces;
        this.provinceResourcePolicy = Objects.requireNonNull(provinceResourcePolicy, "provinceResourcePolicy");
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
                IoeWorldgenConfig.provinceNamespace(),
                IoeWorldgenConfig.allowLegacyProvinceNamespaces(),
                ProvinceResourcePolicy.fromConfig(),
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
                ProvinceId.CONSOLIDATED_NAMESPACE,
                false,
                ProvinceResourcePolicy.defaults(),
                resourcePolicyService,
                scanner
        );
    }

    public boolean enabled() {
        return enabled;
    }

    public ResourcePolicyDecision evaluateOreLoadResource(ExpeditionAnchorRef anchor, ResourceRef resource) {
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

        ProvinceId runtimeProvince;
        try {
            runtimeProvince = runtimeProvinceFor(anchor);
        } catch (IllegalArgumentException exception) {
            return ResourcePolicyDecision.reject(exception.getMessage());
        }

        ResourcePolicyDecision runtimeDecision = resourcePolicyService.evaluate(resource, scanner);
        if (!runtimeDecision.shouldUse()) {
            return runtimeDecision;
        }

        return ResourcePolicyDecision.use(
                "Province runtime integration allowed " + resource.id() + " for " + runtimeProvince
        );
    }

    private ProvinceId runtimeProvinceFor(ExpeditionAnchorRef anchor) {
        String anchorType = anchor.anchorType().trim();
        String candidate = anchorType.contains(":") ? anchorType : provinceNamespace + ":" + anchorType;
        try {
            return ProvinceId.parse(candidate, allowLegacyNamespaces);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(rejectReason(candidate, exception), exception);
        }
    }

    private static String rejectReason(String candidate, IllegalArgumentException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "invalid province id";
        }
        return "Province runtime integration rejected province id " + candidate + ": " + message;
    }
}
