package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.CrystalGrowthSiteType;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

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
    }

    public boolean hasOuterCrust() {
        return outerCrustResource.isPresent();
    }
}
