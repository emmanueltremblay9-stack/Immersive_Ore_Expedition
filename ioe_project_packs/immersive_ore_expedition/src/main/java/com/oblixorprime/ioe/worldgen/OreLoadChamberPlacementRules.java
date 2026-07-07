package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class OreLoadChamberPlacementRules {
    private final LinkedHashSet<ResourceLocation> knownChamberKeys;
    private final LinkedHashSet<ResourceLocation> knownAnchorKeys;

    public OreLoadChamberPlacementRules(
            Collection<ResourceLocation> knownChamberKeys,
            Collection<ResourceLocation> knownAnchorKeys
    ) {
        Objects.requireNonNull(knownChamberKeys, "knownChamberKeys");
        Objects.requireNonNull(knownAnchorKeys, "knownAnchorKeys");
        this.knownChamberKeys = copyKeys(knownChamberKeys);
        this.knownAnchorKeys = copyKeys(knownAnchorKeys);
    }

    public static OreLoadChamberPlacementRules defaults() {
        return new OreLoadChamberPlacementRules(
                IoeWorldgenFeatureKeys.oreLoadChamberFeatureKeys(),
                IoeWorldgenFeatureKeys.anchorFeatureKeys()
        );
    }

    public List<ResourceLocation> knownChamberKeys() {
        return List.copyOf(knownChamberKeys);
    }

    public boolean isKnownChamberKey(ResourceLocation chamberKey) {
        return chamberKey != null && knownChamberKeys.contains(chamberKey);
    }

    public OreLoadChamberPlacementPlan.SkipReason validateSourcePlan(OreLoadPlan sourcePlan) {
        if (sourcePlan == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_ORE_LOAD_PLAN;
        }
        if (sourcePlan.resource() == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_RESOURCE;
        }
        if (sourcePlan.loadCenter() == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_CENTER;
        }
        if (sourcePlan.quality() == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_SITE_QUALITY;
        }
        if (resolveAnchorType(sourcePlan).isEmpty()) {
            return OreLoadChamberPlacementPlan.SkipReason.UNKNOWN_OR_INVALID_ANCHOR;
        }
        if (metadataFor(sourcePlan).isEmpty()) {
            return OreLoadChamberPlacementPlan.SkipReason.INVALID_CHAMBER_BOUNDS;
        }
        return OreLoadChamberPlacementPlan.SkipReason.NONE;
    }

    public OreLoadChamberPlacementPlan.SkipReason validateResourcePolicy(
            OreLoadPlan sourcePlan,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        if (sourcePlan == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_ORE_LOAD_PLAN;
        }
        ResourceRef resource = sourcePlan.resource();
        if (resource == null) {
            return OreLoadChamberPlacementPlan.SkipReason.NULL_RESOURCE;
        }
        if (scanner == null || policyService == null) {
            return OreLoadChamberPlacementPlan.SkipReason.INVALID_INPUT;
        }
        if (policyService.isExcludedResource(resource.id())) {
            return OreLoadChamberPlacementPlan.SkipReason.STRICT_EXCLUSION;
        }

        ResourcePolicyDecision decision = policyService.evaluate(resource, scanner);
        if (decision.shouldUse()) {
            return OreLoadChamberPlacementPlan.SkipReason.NONE;
        }
        if (!scanner.isPresent(resource) && policyService.isApprovedResource(resource.id())) {
            return OreLoadChamberPlacementPlan.SkipReason.RESOURCE_NOT_LOADED;
        }
        return OreLoadChamberPlacementPlan.SkipReason.RESOURCE_DENIED_BY_POLICY;
    }

    public Optional<ResourceLocation> resolveAnchorType(OreLoadPlan sourcePlan) {
        if (sourcePlan == null || sourcePlan.anchor() == null) {
            return Optional.empty();
        }
        String anchorType = sourcePlan.anchor().anchorType();
        if (anchorType == null || anchorType.isBlank()) {
            return Optional.empty();
        }
        return knownAnchorKeys.stream()
                .filter(anchorKey -> anchorType.equals(anchorKey.toString())
                        || anchorType.equals(anchorKey.getPath()))
                .findFirst();
    }

    public Optional<OreLoadChamberPlacementPlan.ChamberMetadata> metadataFor(OreLoadPlan sourcePlan) {
        if (sourcePlan == null || sourcePlan.quality() == null) {
            return Optional.empty();
        }
        SiteQuality quality = sourcePlan.quality();
        OreLoadChamberPlacementPlan.ChamberShape shape = shapeFor(quality);
        int horizontalRadius = horizontalRadiusFor(quality);
        int verticalHalfSize = verticalHalfSizeFor(quality);
        int diameter = horizontalRadius * 2 + 1;
        int height = verticalHalfSize * 2 + 1;
        int approximateVolume = diameter * diameter * height;
        if (horizontalRadius <= 0 || verticalHalfSize <= 0 || approximateVolume <= 0) {
            return Optional.empty();
        }
        return Optional.of(new OreLoadChamberPlacementPlan.ChamberMetadata(
                shape,
                horizontalRadius,
                verticalHalfSize,
                approximateVolume
        ));
    }

    private static OreLoadChamberPlacementPlan.ChamberShape shapeFor(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> OreLoadChamberPlacementPlan.ChamberShape.ROUGH_CHAMBER;
            case NORMAL, RICH -> OreLoadChamberPlacementPlan.ChamberShape.VEIN_CLUSTER;
            case MOTHERLODE -> OreLoadChamberPlacementPlan.ChamberShape.MOTHERLODE_CORE;
        };
    }

    private static int horizontalRadiusFor(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 2;
            case NORMAL -> 3;
            case RICH -> 4;
            case MOTHERLODE -> 5;
        };
    }

    private static int verticalHalfSizeFor(SiteQuality quality) {
        return switch (quality) {
            case DRY, POOR -> 1;
            case NORMAL, RICH -> 2;
            case MOTHERLODE -> 3;
        };
    }

    private static LinkedHashSet<ResourceLocation> copyKeys(Collection<ResourceLocation> keys) {
        LinkedHashSet<ResourceLocation> copied = new LinkedHashSet<>();
        for (ResourceLocation key : keys) {
            copied.add(Objects.requireNonNull(key, "key"));
        }
        return copied;
    }
}
