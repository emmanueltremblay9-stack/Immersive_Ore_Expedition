package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CrystalGrowthSitePlan(
        CrystalGrowthSiteType siteType,
        ExpeditionAnchorRef anchor,
        ResourceRef coreResource,
        Optional<ResourceRef> outerCrustResource,
        boolean structureAnchored,
        boolean randomFreeSite,
        boolean meteoriticVariant,
        boolean disablesFreeGeOreWorldgen,
        List<ResourcePolicyDecision> skippedResources,
        List<ResourcePolicyDecision> rejectedResources
) {
    public CrystalGrowthSitePlan {
        Objects.requireNonNull(siteType, "siteType");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(coreResource, "coreResource");
        outerCrustResource = Objects.requireNonNull(outerCrustResource, "outerCrustResource");
        skippedResources = List.copyOf(Objects.requireNonNull(skippedResources, "skippedResources"));
        rejectedResources = List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
        if (randomFreeSite) {
            throw new IllegalArgumentException("Crystal-growth sites must not be random free sites");
        }
        if (!structureAnchored) {
            throw new IllegalArgumentException("Crystal-growth sites must be structure anchored");
        }
        validateResourceShape(siteType, coreResource, outerCrustResource, meteoriticVariant);
    }

    public boolean hasOuterCrust() {
        return outerCrustResource.isPresent();
    }

    private static void validateResourceShape(
            CrystalGrowthSiteType siteType,
            ResourceRef coreResource,
            Optional<ResourceRef> outerCrustResource,
            boolean meteoriticVariant
    ) {
        switch (siteType) {
            case AMETHYST -> {
                requireBlock(coreResource, "Amethyst core");
                requireNamespace(coreResource, "minecraft", "Amethyst core");
                requirePathToken(coreResource, "amethyst", "Amethyst core");
                requireNoOuterCrust(outerCrustResource, "Amethyst sites");
                if (meteoriticVariant) {
                    throw new IllegalArgumentException("Amethyst site plans must not advertise a meteoritic variant");
                }
            }
            case AE2_CERTUS -> {
                requireBlock(coreResource, "AE2 Certus core");
                requireAe2Namespace(coreResource, "AE2 Certus core");
                requirePathToken(coreResource, "certus", "AE2 Certus core");
                outerCrustResource.ifPresent(CrystalGrowthSitePlan::requireSkyStoneCrust);
                if (meteoriticVariant && outerCrustResource.isEmpty()) {
                    throw new IllegalArgumentException("Meteoritic AE2 Certus plans require a sky-stone outer crust");
                }
            }
            case GEORE -> {
                requireBlock(coreResource, "GeOre core");
                requireNamespace(coreResource, CrystalGrowthCompatGates.GEORE, "GeOre core");
                requireNoOuterCrust(outerCrustResource, "GeOre sites");
                if (meteoriticVariant) {
                    throw new IllegalArgumentException("GeOre site plans must use METEORITIC_GEODE for sky-stone-wrapped variants");
                }
            }
            case METEORITIC_GEODE -> {
                requireMeteoriticCore(coreResource);
                ResourceRef crust = outerCrustResource.orElseThrow(
                        () -> new IllegalArgumentException("Meteoritic geode plans require a sky-stone outer crust")
                );
                requireSkyStoneCrust(crust);
                if (!meteoriticVariant) {
                    throw new IllegalArgumentException("Meteoritic geode plans must set meteoriticVariant");
                }
            }
        }
    }

    private static void requireMeteoriticCore(ResourceRef coreResource) {
        requireBlock(coreResource, "Meteoritic geode core");
        if (isNamespace(coreResource, "minecraft") && coreResource.id().getPath().contains("amethyst")) {
            return;
        }
        if (isAe2Namespace(coreResource) && coreResource.id().getPath().contains("certus")) {
            return;
        }
        if (isNamespace(coreResource, CrystalGrowthCompatGates.GEORE)) {
            return;
        }
        throw new IllegalArgumentException("Meteoritic geode core must be amethyst, AE2 Certus, or GeOre: " + coreResource.id());
    }

    private static void requireSkyStoneCrust(ResourceRef crustResource) {
        requireBlock(crustResource, "Sky-stone crust");
        requireAe2Namespace(crustResource, "Sky-stone crust");
        String path = crustResource.id().getPath();
        if (!"sky_stone".equals(path) && !"skystone".equals(path)) {
            throw new IllegalArgumentException("Sky-stone crust must use an AE2 sky-stone block: " + crustResource.id());
        }
    }

    private static void requireBlock(ResourceRef resource, String label) {
        if (resource.type() != ResourceType.BLOCK) {
            throw new IllegalArgumentException(label + " must be a block resource: " + resource);
        }
    }

    private static void requireNamespace(ResourceRef resource, String namespace, String label) {
        if (!isNamespace(resource, namespace)) {
            throw new IllegalArgumentException(label + " must use namespace '" + namespace + "': " + resource.id());
        }
    }

    private static void requireAe2Namespace(ResourceRef resource, String label) {
        if (!isAe2Namespace(resource)) {
            throw new IllegalArgumentException(label + " must use the ae2/appeng namespace: " + resource.id());
        }
    }

    private static void requirePathToken(ResourceRef resource, String token, String label) {
        if (!resource.id().getPath().contains(token)) {
            throw new IllegalArgumentException(label + " must contain '" + token + "': " + resource.id());
        }
    }

    private static void requireNoOuterCrust(Optional<ResourceRef> outerCrustResource, String label) {
        if (outerCrustResource.isPresent()) {
            throw new IllegalArgumentException(label + " must not carry an outer crust resource");
        }
    }

    private static boolean isAe2Namespace(ResourceRef resource) {
        return isNamespace(resource, CrystalGrowthCompatGates.AE2) || isNamespace(resource, "appeng");
    }

    private static boolean isNamespace(ResourceRef resource, String namespace) {
        return namespace.equals(resource.id().getNamespace());
    }
}
