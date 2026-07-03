package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ExpeditionAnchorPlacementRules {
    private static final Set<String> LEGACY_SPLIT_NAMESPACES = Set.of(
            "ioe_core",
            "ioe_expedition_worldgen",
            "ioe_crystal_growth",
            "ioe_nether_geodes",
            "ioe_ieip_prospecting",
            "ioe_retrogen_admin"
    );

    private final LinkedHashSet<ResourceLocation> knownAnchorKeys;

    public ExpeditionAnchorPlacementRules(Collection<ResourceLocation> knownAnchorKeys) {
        Objects.requireNonNull(knownAnchorKeys, "knownAnchorKeys");
        this.knownAnchorKeys = new LinkedHashSet<>();
        for (ResourceLocation anchorKey : knownAnchorKeys) {
            this.knownAnchorKeys.add(Objects.requireNonNull(anchorKey, "anchorKey"));
        }
    }

    public static ExpeditionAnchorPlacementRules defaults() {
        return new ExpeditionAnchorPlacementRules(IoeWorldgenFeatureKeys.anchorFeatureKeys());
    }

    public List<ResourceLocation> knownAnchorKeys() {
        return List.copyOf(knownAnchorKeys);
    }

    public boolean isKnownAnchorKey(ResourceLocation anchorType) {
        return anchorType != null
                && ImmersiveOreExpeditionMod.MODID.equals(anchorType.getNamespace())
                && knownAnchorKeys.contains(anchorType);
    }

    public ExpeditionAnchorPlacementPlan.SkipReason validate(
            ResourceLocation anchorType,
            BlockPos origin,
            SiteQuality siteQuality
    ) {
        if (anchorType == null) {
            return ExpeditionAnchorPlacementPlan.SkipReason.INVALID_INPUT;
        }
        if (origin == null) {
            return ExpeditionAnchorPlacementPlan.SkipReason.NULL_ORIGIN;
        }
        if (siteQuality == null) {
            return ExpeditionAnchorPlacementPlan.SkipReason.NULL_SITE_QUALITY;
        }
        String namespace = anchorType.getNamespace();
        if (!ImmersiveOreExpeditionMod.MODID.equals(namespace) || LEGACY_SPLIT_NAMESPACES.contains(namespace)) {
            return ExpeditionAnchorPlacementPlan.SkipReason.INVALID_NAMESPACE;
        }
        if (!knownAnchorKeys.contains(anchorType)) {
            return ExpeditionAnchorPlacementPlan.SkipReason.UNKNOWN_ANCHOR_TYPE;
        }
        return ExpeditionAnchorPlacementPlan.SkipReason.NONE;
    }
}
