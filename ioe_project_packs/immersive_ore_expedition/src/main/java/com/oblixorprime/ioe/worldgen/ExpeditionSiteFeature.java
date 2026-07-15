package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Map;
import java.util.Objects;

public final class ExpeditionSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final int BLOCK_UPDATE_FLAGS = 2;
    private static final ResourcePolicyService RESOURCE_POLICY = new ResourcePolicyService();
    private final ExpeditionSiteType siteType;

    public ExpeditionSiteFeature(ExpeditionSiteType siteType) {
        super(NoneFeatureConfiguration.CODEC);
        this.siteType = Objects.requireNonNull(siteType, "siteType");
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Objects.requireNonNull(context, "context");
        IoeWorldgenRuntimeDiagnostics.recordSiteAttempt();
        if (!IoeWorldgenConfig.naturalExpeditionSiteGenerationEnabled()
                || !siteType.enabledFromConfig()
                || !requiredComponentsEnabled(siteType)) {
            skip(context.origin(), IoeWorldgenRuntimeDiagnostics.SiteSkipReason.CONFIG_DISABLED,
                    "natural generation or a required site component is disabled");
            return false;
        }
        if (siteType == ExpeditionSiteType.ORE_LOAD_CHAMBER
                && IoeWorldgenConfig.requireStructureAnchorForMajorOreLoads()) {
            skip(context.origin(), IoeWorldgenRuntimeDiagnostics.SiteSkipReason.STANDALONE_CHAMBER_FORBIDDEN,
                    "standalone ore-load chambers are forbidden by anchor policy");
            return false;
        }

        BlockPos origin = siteType.naturalSurfaceSite()
                ? resolveSurfaceOrigin(context.level(), context.origin(), siteType)
                : context.origin();
        if (origin == null) {
            skip(context.origin(), IoeWorldgenRuntimeDiagnostics.SiteSkipReason.SURFACE_UNSUITABLE,
                    "the surface is too steep, obstructed, or fluid-covered");
            return false;
        }

        SiteQuality quality = SiteQualityRoll.DEFAULT.roll(context.random());
        SpecialMineGeode specialGeode = null;
        if (quality.isProductive()
                && siteType == ExpeditionSiteType.BURIED_SURVEY_MARKER
                && Ae2MeteoriteIntegration.isCrystalProcessingStackLoaded()) {
            specialGeode = Ae2MeteoriteIntegration.resolve()
                    .map(material -> new SpecialMineGeode(
                            IoeWorldgenFeatureKeys.METEORITIC_AE2_GEODE,
                            material.buddingResource(),
                            material.buddingBlock(),
                            material.skyStoneResource(),
                            material.skyStoneBlock()
                    ))
                    .orElse(null);
            if (!usableSpecialGeode(specialGeode)) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.AE2_RESOURCE_MISSING,
                        "the required AE2/AE2 Crystal Science budding Certus resources are unavailable");
                return false;
            }
        }
        if (quality.isProductive()
                && siteType == ExpeditionSiteType.COLLAPSED_SHAFT
                && ExtendedAeGeodeIntegration.isLoaded()) {
            specialGeode = ExtendedAeGeodeIntegration.resolve()
                    .map(material -> new SpecialMineGeode(
                            IoeWorldgenFeatureKeys.ENTROIZED_FLUIX_GEODE,
                            material.buddingResource(),
                            material.buddingBlock(),
                            material.shellResource(),
                            material.shellBlock()
                    ))
                    .orElse(null);
            if (!usableSpecialGeode(specialGeode)) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.EXTENDED_AE_RESOURCE_MISSING,
                        "ExtendedAE is loaded but its Entroized Fluix geode resources are unavailable");
                return false;
            }
        }
        boolean specialGeodeMine = specialGeode != null;
        boolean needsOreProfile = quality.isProductive() && !specialGeodeMine;
        BiomeOreNodeProfile oreProfile = BiomeOreNodeProfile.resolve(context.level(), origin).orElse(null);
        if (needsOreProfile && oreProfile == null) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.PROFILE_MISSING,
                    "the origin biome has no ore-node profile");
            return false;
        }
        if (needsOreProfile
                && !RESOURCE_POLICY.evaluate(oreProfile.resource(), LoadedResourceScanner.runtime()).shouldUse()) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.RESOURCE_POLICY_DENIED,
                    "the biome ore-node resource is unavailable or denied by policy");
            return false;
        }

        ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                siteType,
                origin,
                quality,
                needsOreProfile ? oreProfile.resource().id() : null,
                needsOreProfile ? oreProfile.oreBlock().defaultBlockState() : null,
                needsOreProfile ? oreProfile.buddingResource().id() : null,
                needsOreProfile ? oreProfile.buddingBlock().defaultBlockState() : null,
                specialGeode == null ? null : specialGeode.componentId(),
                specialGeode == null ? null : specialGeode.buddingBlock().defaultBlockState(),
                specialGeode == null ? null : specialGeode.shellBlock().defaultBlockState(),
                needsOreProfile ? oreProfile.oreBudget(quality) : 0,
                needsOreProfile ? oreProfile.nodeCount(quality) : 0,
                context.random()
        );
        if (siteType.naturalSurfaceSite() && !plan.isConnectedExpeditionSite()) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.DISCONNECTED_PLAN,
                    "the configured anchor distance window excludes the connected chamber");
            return false;
        }
        if (!withinBuildHeight(context.level(), plan) || !apply(context.level(), plan)) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                    "the connected block plan could not be written safely");
            return false;
        }

        if (siteType.naturalSurfaceSite()) {
            IoeOrePlacementAuthorization.authorize(context.level().getLevel().dimension(), plan);
            recordPlacedSite(context.level(), plan, oreProfile);
        }
        IoeWorldgenRuntimeDiagnostics.recordSitePlaced();
        return true;
    }

    private static BlockPos resolveSurfaceOrigin(
            WorldGenLevel level,
            BlockPos requestedOrigin,
            ExpeditionSiteType siteType
    ) {
        int localX = Math.floorMod(requestedOrigin.getX(), 16);
        int localZ = Math.floorMod(requestedOrigin.getZ(), 16);
        int safeLocalX = localX <= 7 ? 4 : 11;
        int x = requestedOrigin.getX() + safeLocalX - localX;
        int z = requestedOrigin.getZ() + clamp(localZ, 6, 9) - localZ;
        int radius = siteType == ExpeditionSiteType.MINER_CAMP ? 4 : 3;
        int allowedSlope = siteType == ExpeditionSiteType.MINER_CAMP ? 2 : 4;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int dx : new int[]{-radius, 0, radius}) {
            for (int dz : new int[]{-radius, 0, radius}) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
                minY = Math.min(minY, surfaceY);
                maxY = Math.max(maxY, surfaceY);
            }
        }
        if (maxY - minY > allowedSlope) {
            return null;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 5; dy++) {
                    BlockState state = level.getBlockState(new BlockPos(x + dx, maxY + dy, z + dz));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return null;
                    }
                }
            }
        }
        return new BlockPos(x, maxY, z);
    }

    private static boolean usableSpecialGeode(SpecialMineGeode material) {
        return material != null
                && RESOURCE_POLICY.evaluate(
                material.buddingResource(),
                LoadedResourceScanner.runtime()
        ).shouldUse()
                && RESOURCE_POLICY.evaluate(
                material.shellResource(),
                LoadedResourceScanner.runtime()
        ).shouldUse();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean requiredComponentsEnabled(ExpeditionSiteType type) {
        return !type.naturalSurfaceSite()
                || IoeWorldgenConfig.basicMineshaftConnectorEnabled()
                && IoeWorldgenConfig.oreLoadChamberEnabled();
    }

    private static boolean withinBuildHeight(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        return plan.blocks().keySet().stream()
                .allMatch(pos -> pos.getY() >= minBuildHeight && pos.getY() < maxBuildHeight);
    }

    private static boolean apply(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        for (BlockPos pos : plan.blocks().keySet()) {
            if (!canWrite(level, pos)) {
                return false;
            }
        }
        for (Map.Entry<BlockPos, BlockState> placement : plan.blocks().entrySet()) {
            BlockPos pos = placement.getKey();
            BlockState target = placement.getValue();
            boolean changed = level.setBlock(pos, target, BLOCK_UPDATE_FLAGS);
            if (!changed && !level.getBlockState(pos).equals(target)) {
                return false;
            }
        }
        return true;
    }

    private static boolean canWrite(WorldGenLevel level, BlockPos pos) {
        if (!level.ensureCanWrite(pos)) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (!existing.getFluidState().isEmpty() || existing.hasBlockEntity()) {
            return false;
        }
        return existing.isAir() || OreLoadChamberReplacementRules.canReplace(existing);
    }

    private static void recordPlacedSite(
            WorldGenLevel level,
            ExpeditionSiteBlockPlan plan,
            BiomeOreNodeProfile oreProfile
    ) {
        ResourceLocation provinceId = ResourceLocation.tryParse(IoeWorldgenConfig.defaultProvince());
        ExpeditionLocatorService.record(level.getLevel(), ExpeditionSite.anchor(
                level.getLevel().dimension(),
                plan.anchorPos(),
                plan.requestedFeatureId(),
                provinceId,
                plan.quality(),
                "natural_connected_expedition_site",
                ExpeditionSitePlacementState.PROVEN,
                null
        ));
        if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            ResourceLocation biomeId = oreProfile == null
                    ? level.getBiome(plan.anchorPos()).unwrapKey().map(key -> key.location()).orElse(null)
                    : oreProfile.biomeId();
            int connectedBiomeChunks = oreProfile == null ? 0 : oreProfile.sampledConnectedChunks();
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Generated connected IOE expedition site type={} anchor={} chamber={} biome={} connectedBiomeChunks={} quality={} ore={} oreHeart={} oreNodes={} oreHearts={} oreBlocks={} blocks={}",
                    plan.requestedFeatureId(),
                    plan.anchorPos(),
                    plan.chamberCenter(),
                    biomeId,
                    connectedBiomeChunks,
                    plan.quality(),
                    plan.oreBlockId(),
                    plan.oreNodeHeartBlockId(),
                    plan.oreNodeCount(),
                    plan.oreNodeHeartCount(),
                    plan.oreBlockCount(),
                    plan.blocks().size()
            );
        }
    }

    private static void skip(
            BlockPos origin,
            IoeWorldgenRuntimeDiagnostics.SiteSkipReason skipReason,
            String reason
    ) {
        IoeWorldgenRuntimeDiagnostics.recordSiteSkip(skipReason);
        if (IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            IoeExpeditionWorldgenMod.LOGGER.info(
                    "Skipped IOE expedition site at {} code={}: {}",
                    origin,
                    skipReason.id(),
                    reason
            );
        }
    }

    private record SpecialMineGeode(
            ResourceLocation componentId,
            ResourceRef buddingResource,
            Block buddingBlock,
            ResourceRef shellResource,
            Block shellBlock
    ) {
        private SpecialMineGeode {
            Objects.requireNonNull(componentId, "componentId");
            Objects.requireNonNull(buddingResource, "buddingResource");
            Objects.requireNonNull(buddingBlock, "buddingBlock");
            Objects.requireNonNull(shellResource, "shellResource");
            Objects.requireNonNull(shellBlock, "shellBlock");
        }
    }

}
