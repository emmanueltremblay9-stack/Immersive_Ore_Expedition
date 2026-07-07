package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record CrystalSitePlacementPlan(
        SiteType siteType,
        SourceSystem sourceSystem,
        ResourceRef primaryResource,
        Optional<ResourceRef> shellResource,
        BlockPos origin,
        boolean placementAllowed,
        Decision decision,
        SkipReason skipReason,
        Optional<ResourceLocation> biomeId,
        Optional<ResourceLocation> provinceId,
        Optional<ResourceLocation> anchorType,
        Optional<SiteMetadata> siteMetadata
) {
    public CrystalSitePlacementPlan {
        shellResource = shellResource == null ? Optional.empty() : shellResource;
        decision = Objects.requireNonNull(decision, "decision");
        skipReason = Objects.requireNonNull(skipReason, "skipReason");
        biomeId = biomeId == null ? Optional.empty() : biomeId;
        provinceId = provinceId == null ? Optional.empty() : provinceId;
        anchorType = anchorType == null ? Optional.empty() : anchorType;
        siteMetadata = siteMetadata == null ? Optional.empty() : siteMetadata;
        if (placementAllowed && decision != Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Allowed crystal site plans must use ALLOW_PLAN");
        }
        if (placementAllowed && skipReason != SkipReason.NONE) {
            throw new IllegalArgumentException("Allowed crystal site plans must not carry a skip reason");
        }
        if (!placementAllowed && decision == Decision.ALLOW_PLAN) {
            throw new IllegalArgumentException("Skipped crystal site plans must not use ALLOW_PLAN");
        }
        if (!placementAllowed && skipReason == SkipReason.NONE) {
            throw new IllegalArgumentException("Skipped crystal site plans require a skip reason");
        }
        if (placementAllowed) {
            Objects.requireNonNull(siteType, "siteType");
            Objects.requireNonNull(sourceSystem, "sourceSystem");
            Objects.requireNonNull(primaryResource, "primaryResource");
            Objects.requireNonNull(origin, "origin");
            if (siteMetadata.isEmpty()) {
                throw new IllegalArgumentException("Allowed crystal site plans require site metadata");
            }
        }
    }

    public static CrystalSitePlacementPlan allowed(
            SiteType siteType,
            SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            SiteMetadata siteMetadata
    ) {
        return new CrystalSitePlacementPlan(
                siteType,
                sourceSystem,
                primaryResource,
                Optional.ofNullable(shellResource),
                origin,
                true,
                Decision.ALLOW_PLAN,
                SkipReason.NONE,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.of(Objects.requireNonNull(siteMetadata, "siteMetadata"))
        );
    }

    public static CrystalSitePlacementPlan skipped(
            SiteType siteType,
            SourceSystem sourceSystem,
            ResourceRef primaryResource,
            ResourceRef shellResource,
            BlockPos origin,
            Decision decision,
            SkipReason skipReason,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            SiteMetadata siteMetadata
    ) {
        return new CrystalSitePlacementPlan(
                siteType,
                sourceSystem,
                primaryResource,
                Optional.ofNullable(shellResource),
                origin,
                false,
                decision,
                skipReason,
                Optional.ofNullable(biomeId),
                Optional.ofNullable(provinceId),
                Optional.ofNullable(anchorType),
                Optional.ofNullable(siteMetadata)
        );
    }

    public record SiteMetadata(
            double rarityWeight,
            double densityWeight,
            int radius,
            int siteSize,
            boolean renewableSite,
            boolean disablesFreeGeOreWorldgen,
            boolean requiresStructureAnchor
    ) {
        public SiteMetadata {
            if (!Double.isFinite(rarityWeight) || rarityWeight < 0.0D) {
                throw new IllegalArgumentException("rarityWeight must be finite and non-negative");
            }
            if (!Double.isFinite(densityWeight) || densityWeight < 0.0D) {
                throw new IllegalArgumentException("densityWeight must be finite and non-negative");
            }
            if (radius < 0) {
                throw new IllegalArgumentException("radius must not be negative");
            }
            if (siteSize < 0) {
                throw new IllegalArgumentException("siteSize must not be negative");
            }
        }
    }

    public enum SiteType {
        AMETHYST_GROWTH_SITE,
        AE2_CERTUS_GROWTH_SITE,
        GEORE_GROWTH_SITE
    }

    public enum SourceSystem {
        VANILLA_AMETHYST,
        AE2_CERTUS,
        GEORE
    }

    public enum Decision {
        ALLOW_PLAN,
        SKIP_RUNTIME_DISABLED,
        SKIP_RESOURCE_NOT_LOADED,
        SKIP_RESOURCE_DENIED,
        SKIP_STRICT_EXCLUSION,
        SKIP_OPTIONAL_MOD_ABSENT,
        SKIP_INVALID_SITE_TYPE,
        SKIP_FAKE_FLUIX_FORBIDDEN,
        SKIP_INVALID_INPUT
    }

    public enum SkipReason {
        NONE,
        RUNTIME_WORLDGEN_DISABLED,
        NULL_RESOURCE,
        NULL_ORIGIN,
        INVALID_SITE_TYPE,
        RESOURCE_NOT_LOADED,
        RESOURCE_DENIED_BY_POLICY,
        STRICT_EXCLUSION,
        OPTIONAL_MOD_ABSENT,
        FAKE_FLUIX_FORBIDDEN,
        INVALID_INPUT
    }
}
