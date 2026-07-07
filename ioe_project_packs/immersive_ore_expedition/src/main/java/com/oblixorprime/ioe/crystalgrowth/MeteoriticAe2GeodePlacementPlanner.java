package com.oblixorprime.ioe.crystalgrowth;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.worldgen.IoeWorldgenFeatureKeys;
import com.oblixorprime.ioe.worldgen.IoeWorldgenPlacementGates;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public final class MeteoriticAe2GeodePlacementPlanner {
    private static final MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata DEFAULT_LAYER_METADATA =
            new MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata(
                    32,
                    7,
                    5,
                    3,
                    0.75D,
                    1.0D,
                    true,
                    true,
                    true,
                    true
            );

    private final Ae2CertusSiteProvider ae2Provider;
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public MeteoriticAe2GeodePlacementPlanner() {
        this(
                new Ae2CertusSiteProvider(),
                LoadedResourceScanner.runtime(),
                new ResourcePolicyService()
        );
    }

    public MeteoriticAe2GeodePlacementPlanner(
            Ae2CertusSiteProvider ae2Provider,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        this.ae2Provider = Objects.requireNonNull(ae2Provider, "ae2Provider");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    public MeteoriticAe2GeodePlacementPlan planMeteoriticAe2Geode(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            BlockPos origin
    ) {
        return planMeteoriticAe2Geode(
                anchor,
                primaryResource,
                skyStoneCrustResource,
                null,
                primaryResource,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public MeteoriticAe2GeodePlacementPlan planMeteoriticAe2Geode(
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        return planMeteoriticAe2Geode(
                MeteoriticAe2GeodePlacementPlan.GeodeType.BURIED_METEORITIC_AE2_GEODE,
                MeteoriticAe2GeodePlacementPlan.SourceSystem.AE2_METEORITIC,
                anchor,
                primaryResource,
                skyStoneCrustResource,
                middleLayerResource,
                crystalCoreResource,
                origin,
                biomeId,
                provinceId,
                anchorType,
                DEFAULT_LAYER_METADATA,
                placementGates
        );
    }

    public MeteoriticAe2GeodePlacementPlan planMeteoriticAe2Geode(
            MeteoriticAe2GeodePlacementPlan.GeodeType geodeType,
            MeteoriticAe2GeodePlacementPlan.SourceSystem sourceSystem,
            ExpeditionAnchorRef anchor,
            ResourceRef primaryResource,
            ResourceRef skyStoneCrustResource,
            ResourceRef middleLayerResource,
            ResourceRef crystalCoreResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata layerMetadata,
            IoeWorldgenPlacementGates placementGates
    ) {
        ResourceLocation resolvedAnchorType = resolveAnchorType(anchorType, anchor);
        MeteoriticAe2GeodePlacementPlan rejected = MeteoriticAe2GeodePlacementRules.validateDirect(
                geodeType,
                sourceSystem,
                anchor,
                primaryResource,
                skyStoneCrustResource,
                middleLayerResource,
                crystalCoreResource,
                origin,
                layerMetadata,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolvedAnchorType);
        }

        Optional<CrystalGrowthSitePlan> ae2Plan = ae2Provider.planCertusSite(
                anchor,
                primaryResource,
                Optional.ofNullable(skyStoneCrustResource),
                scanner,
                policyService
        );
        if (ae2Plan.isEmpty()) {
            return MeteoriticAe2GeodePlacementPlan.skipped(
                    geodeType,
                    sourceSystem,
                    primaryResource,
                    skyStoneCrustResource,
                    middleLayerResource,
                    crystalCoreResource,
                    origin,
                    MeteoriticAe2GeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    MeteoriticAe2GeodePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    resolvedAnchorType,
                    layerMetadata
            );
        }

        CrystalGrowthSitePlan certusPlan = ae2Plan.get();
        return MeteoriticAe2GeodePlacementPlan.allowed(
                geodeType,
                sourceSystem,
                certusPlan.coreResource(),
                certusPlan.outerCrustResource().orElse(null),
                middleLayerResource,
                crystalCoreResource,
                origin,
                biomeId,
                provinceId,
                resolvedAnchorType,
                layerMetadata
        );
    }

    public static MeteoriticAe2GeodePlacementPlan.GeodeLayerMetadata defaultLayerMetadata() {
        return DEFAULT_LAYER_METADATA;
    }

    private static ResourceLocation resolveAnchorType(ResourceLocation anchorType, ExpeditionAnchorRef anchor) {
        if (anchorType != null) {
            return anchorType;
        }
        if (anchor != null && !anchor.anchorType().isBlank()) {
            return ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, anchor.anchorType());
        }
        return IoeWorldgenFeatureKeys.METEORITIC_AE2_GEODE;
    }

    private static MeteoriticAe2GeodePlacementPlan withContext(
            MeteoriticAe2GeodePlacementPlan plan,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType
    ) {
        return MeteoriticAe2GeodePlacementPlan.skipped(
                plan.geodeType(),
                plan.sourceSystem(),
                plan.primaryResource(),
                plan.skyStoneCrustResource().orElse(null),
                plan.middleLayerResource().orElse(null),
                plan.crystalCoreResource().orElse(null),
                plan.origin(),
                plan.decision(),
                plan.skipReason(),
                biomeId,
                provinceId,
                anchorType,
                plan.layerMetadata().orElse(null)
        );
    }
}
