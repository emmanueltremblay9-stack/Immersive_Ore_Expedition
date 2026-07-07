package com.oblixorprime.ioe.nethergeodes;

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

public final class NetherSubLavaGeodePlacementPlanner {
    private static final NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata DEFAULT_LAVA_LAKE_METADATA =
            new NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata(
                    IoeNetherGeodesConfig.lavaSampleRadius(),
                    IoeNetherGeodesConfig.minimumLavaCoverage(),
                    IoeNetherGeodesConfig.minimumLavaDepth(),
                    IoeNetherGeodesConfig.requireGiantLavaLakeAbove()
            );
    private static final NetherSubLavaGeodePlacementPlan.LayerMetadata DEFAULT_LAYER_METADATA =
            new NetherSubLavaGeodePlacementPlan.LayerMetadata(
                    7,
                    5,
                    3,
                    false,
                    IoeNetherGeodesConfig.ancientDebrisMotherlodeChance(),
                    true,
                    true,
                    IoeNetherGeodesConfig.requireSafeCrust()
            );
    private static final NetherSubLavaGeodePlacementPlan.LayerMetadata ANCIENT_DEBRIS_HEART_LAYER_METADATA =
            new NetherSubLavaGeodePlacementPlan.LayerMetadata(
                    7,
                    5,
                    3,
                    true,
                    IoeNetherGeodesConfig.ancientDebrisMotherlodeChance(),
                    true,
                    true,
                    IoeNetherGeodesConfig.requireSafeCrust()
            );

    private final SubLavaGeodeGenerator generator;
    private final LoadedResourceScanner scanner;
    private final ResourcePolicyService policyService;

    public NetherSubLavaGeodePlacementPlanner() {
        this(
                new SubLavaGeodeGenerator(),
                LoadedResourceScanner.runtime(),
                new ResourcePolicyService()
        );
    }

    public NetherSubLavaGeodePlacementPlanner(
            SubLavaGeodeGenerator generator,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        this.generator = Objects.requireNonNull(generator, "generator");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.policyService = Objects.requireNonNull(policyService, "policyService");
    }

    public NetherSubLavaGeodePlacementPlan planSubLavaQuartzGeode(
            ExpeditionAnchorRef anchor,
            LavaLakeAnchorSample lavaLake,
            ResourceRef quartzResource,
            BlockPos origin
    ) {
        return planSubLavaQuartzGeode(
                anchor,
                lavaLake,
                quartzResource,
                origin,
                null,
                null,
                null,
                IoeWorldgenPlacementGates.fromConfig()
        );
    }

    public NetherSubLavaGeodePlacementPlan planSubLavaQuartzGeode(
            ExpeditionAnchorRef anchor,
            LavaLakeAnchorSample lavaLake,
            ResourceRef quartzResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            IoeWorldgenPlacementGates placementGates
    ) {
        return planNetherSubLavaGeode(
                NetherSubLavaGeodePlacementPlan.GeodeType.SUB_LAVA_QUARTZ_GEODE,
                NetherSubLavaGeodePlacementPlan.SourceSystem.IOE_NETHER_GEODE,
                anchor,
                lavaLake,
                quartzResource,
                null,
                origin,
                biomeId,
                provinceId,
                anchorType,
                DEFAULT_LAVA_LAKE_METADATA,
                defaultDepthMetadata(),
                DEFAULT_LAYER_METADATA,
                placementGates
        );
    }

    public NetherSubLavaGeodePlacementPlan planNetherSubLavaGeode(
            NetherSubLavaGeodePlacementPlan.GeodeType geodeType,
            NetherSubLavaGeodePlacementPlan.SourceSystem sourceSystem,
            ExpeditionAnchorRef anchor,
            LavaLakeAnchorSample lavaLake,
            ResourceRef quartzResource,
            ResourceRef ancientDebrisHeartResource,
            BlockPos origin,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata lavaLakeAnchorMetadata,
            NetherSubLavaGeodePlacementPlan.DepthMetadata depthMetadata,
            NetherSubLavaGeodePlacementPlan.LayerMetadata layerMetadata,
            IoeWorldgenPlacementGates placementGates
    ) {
        ResourceLocation resolvedAnchorType = resolveAnchorType(anchorType, anchor);
        NetherSubLavaGeodePlacementPlan rejected = NetherSubLavaGeodePlacementRules.validateDirect(
                geodeType,
                sourceSystem,
                anchor,
                lavaLake,
                quartzResource,
                ancientDebrisHeartResource,
                origin,
                lavaLakeAnchorMetadata,
                depthMetadata,
                layerMetadata,
                scanner,
                policyService,
                placementGates
        );
        if (rejected != null) {
            return withContext(rejected, biomeId, provinceId, resolvedAnchorType, anchor);
        }

        Optional<SubLavaGeodePlan> generatorPlan = generator.planBelowLake(
                anchor,
                lavaLake,
                quartzResource,
                Optional.ofNullable(ancientDebrisHeartResource),
                depthMetadata.blocksBelowLava(),
                scanner,
                policyService
        );
        if (generatorPlan.isEmpty()) {
            return NetherSubLavaGeodePlacementPlan.skipped(
                    geodeType,
                    sourceSystem,
                    quartzResource,
                    ancientDebrisHeartResource,
                    origin,
                    NetherSubLavaGeodePlacementPlan.Decision.SKIP_INVALID_INPUT,
                    NetherSubLavaGeodePlacementPlan.SkipReason.INVALID_INPUT,
                    biomeId,
                    provinceId,
                    resolvedAnchorType,
                    anchor.dimension(),
                    lavaLakeAnchorMetadata,
                    depthMetadata,
                    layerMetadata
            );
        }

        SubLavaGeodePlan planned = generatorPlan.get();
        return NetherSubLavaGeodePlacementPlan.allowed(
                geodeType,
                sourceSystem,
                planned.quartzResource(),
                planned.ancientDebrisHeart().orElse(null),
                origin,
                biomeId,
                provinceId,
                resolvedAnchorType,
                anchor.dimension(),
                lavaLakeAnchorMetadata,
                depthMetadata,
                layerMetadata
        );
    }

    public static NetherSubLavaGeodePlacementPlan.LavaLakeAnchorMetadata defaultLavaLakeAnchorMetadata() {
        return DEFAULT_LAVA_LAKE_METADATA;
    }

    public static NetherSubLavaGeodePlacementPlan.DepthMetadata defaultDepthMetadata() {
        int min = IoeNetherGeodesConfig.minBlocksBelowLava();
        int max = IoeNetherGeodesConfig.maxBlocksBelowLava();
        int blocksBelowLava = Math.min(max, Math.max(min, 16));
        return new NetherSubLavaGeodePlacementPlan.DepthMetadata(blocksBelowLava, min, max);
    }

    public static NetherSubLavaGeodePlacementPlan.LayerMetadata defaultLayerMetadata() {
        return DEFAULT_LAYER_METADATA;
    }

    public static NetherSubLavaGeodePlacementPlan.LayerMetadata ancientDebrisHeartLayerMetadata() {
        return ANCIENT_DEBRIS_HEART_LAYER_METADATA;
    }

    private static ResourceLocation resolveAnchorType(ResourceLocation anchorType, ExpeditionAnchorRef anchor) {
        if (anchorType != null) {
            return anchorType;
        }
        if (anchor != null && !anchor.anchorType().isBlank()) {
            return ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, anchor.anchorType());
        }
        return IoeWorldgenFeatureKeys.SUB_LAVA_GEODE;
    }

    private static NetherSubLavaGeodePlacementPlan withContext(
            NetherSubLavaGeodePlacementPlan plan,
            ResourceLocation biomeId,
            ResourceLocation provinceId,
            ResourceLocation anchorType,
            ExpeditionAnchorRef anchor
    ) {
        return NetherSubLavaGeodePlacementPlan.skipped(
                plan.geodeType(),
                plan.sourceSystem(),
                plan.primaryQuartzResource(),
                plan.ancientDebrisHeartResource().orElse(null),
                plan.origin(),
                plan.decision(),
                plan.skipReason(),
                biomeId,
                provinceId,
                anchorType,
                anchor == null ? null : anchor.dimension(),
                plan.lavaLakeAnchorMetadata().orElse(null),
                plan.depthMetadata().orElse(null),
                plan.layerMetadata().orElse(null)
        );
    }
}
