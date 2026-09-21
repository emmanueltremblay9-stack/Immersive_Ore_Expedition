package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.domum.DomumOrnamentumCompat;
import com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge;
import com.oblixorprime.ioe.compat.ip.IoePetroleumReservoirBridge;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ExpeditionSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final SiteQualityRoll PRODUCTIVE_SITE_QUALITY = new SiteQualityRoll(0, 25, 45, 17, 3);
    private static final int SURFACE_HAZARD_MARGIN = 2;
    private final ExpeditionSiteType siteType;
    private final ResourceProfileResolver resourceProfileResolver;

    @FunctionalInterface
    interface ResourceProfileResolver {
        BiomeMineResourceProfile.Resolution resolve(WorldGenLevel level, BlockPos chamberOrigin);
    }

    public ExpeditionSiteFeature(ExpeditionSiteType siteType) {
        this(siteType, ExpeditionSiteFeature::resolveProductionResourceProfile);
    }

    ExpeditionSiteFeature(
            ExpeditionSiteType siteType,
            ResourceProfileResolver resourceProfileResolver
    ) {
        super(NoneFeatureConfiguration.CODEC);
        this.siteType = Objects.requireNonNull(siteType, "siteType");
        this.resourceProfileResolver = Objects.requireNonNull(resourceProfileResolver, "resourceProfileResolver");
    }

    private static BiomeMineResourceProfile.Resolution resolveProductionResourceProfile(
            WorldGenLevel level,
            BlockPos chamberOrigin
    ) {
        return BiomeMineResourceProfile.resolve(level, chamberOrigin);
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

        SiteQualityRoll qualityRoll = siteType == ExpeditionSiteType.MINER_CAMP
                ? SiteQualityRoll.DEFAULT
                : PRODUCTIVE_SITE_QUALITY;
        SiteQuality quality = qualityRoll.roll(context.random());
        BlockPos origin = siteType.naturalSurfaceSite()
                ? resolveSurfaceOrigin(context.level(), context.origin(), siteType, quality)
                : context.origin();
        if (origin == null) {
            skip(context.origin(), IoeWorldgenRuntimeDiagnostics.SiteSkipReason.SURFACE_UNSUITABLE,
                    "the surface is too steep, obstructed, or fluid-covered");
            return false;
        }

        ProspectorCampVisualFamily visualFamily = siteType == ExpeditionSiteType.MINER_CAMP
                ? ProspectorCampVisualFamily.resolve(context.level().getBiome(origin))
                : ProspectorCampVisualFamily.TEMPERATE;
        if (visualFamily == ProspectorCampVisualFamily.AQUATIC) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.SURFACE_UNSUITABLE,
                    "aquatic and shoreline biomes have no reliable prospector-camp placement");
            return false;
        }

        long planSeed = context.random().nextLong();
        ProspectorCampContext prospectorCampContext = new ProspectorCampContext(
                visualFamily,
                planSeed,
                siteType == ExpeditionSiteType.MINER_CAMP
                        && ModList.get().isLoaded(DomumOrnamentumCompat.MOD_ID)
        );
        ExpeditionSiteBlockPlan previewPlan = structureOnlyPlan(
                siteType,
                origin,
                quality,
                planSeed,
                prospectorCampContext
        );
        BiomeMineResourceProfile resourceProfile = null;
        if (siteType.naturalSurfaceSite()) {
            BiomeMineResourceProfile.Resolution resolution = resourceProfileResolver.resolve(
                    context.level(),
                    previewPlan.chamberCenter()
            );
            if (resolution.failure() != BiomeMineResourceProfile.Failure.NONE) {
                IoeWorldgenRuntimeDiagnostics.SiteSkipReason skipReason =
                        resolution.failure() == BiomeMineResourceProfile.Failure.AMBIGUOUS
                                ? IoeWorldgenRuntimeDiagnostics.SiteSkipReason.PROFILE_AMBIGUOUS
                                : IoeWorldgenRuntimeDiagnostics.SiteSkipReason.PROFILE_MISSING;
                skip(origin, skipReason, "the origin biome does not select exactly one mine resource profile");
                return false;
            }
            resourceProfile = resolution.profile().orElseThrow();
        }

        DepositPreparation depositPreparation = prepareExcavatorDeposit(
                context.level().getLevel(),
                origin,
                quality,
                resourceProfile,
                siteType.naturalSurfaceSite()
        );
        if (!depositPreparation.resolution().confirmed()) {
            skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.IE_DEPOSIT_MISSING,
                    "no valid lower quality remained after an IE deposit failure");
            return false;
        }
        quality = depositPreparation.resolution().finalQuality();
        if (siteType == ExpeditionSiteType.MINER_CAMP) {
            prospectorCampContext = prospectorCampContext.withArchetype(
                    ProspectorCampArchetype.select(planSeed, quality)
            );
        }
        IoeMotherDepositReservation depositReservation = depositPreparation.reservation().orElse(null);
        IoePetroleumReservoirReservation petroleumReservation = preparePetroleumReservoir(
                context.level().getLevel(),
                origin,
                quality,
                resourceProfile,
                siteType.naturalSurfaceSite()
        );
        boolean reservationTransferred = false;
        try {
            ExpeditionSiteBlockPlan plan = quality == previewPlan.quality()
                    && prospectorCampContext.archetype() == ProspectorCampArchetype.ACTIVE
                    ? previewPlan
                    : structureOnlyPlan(siteType, origin, quality, planSeed, prospectorCampContext);
            ArrayList<ExpeditionSiteBlockPlan> fallbackPlans = new ArrayList<>();
            if (depositReservation != null && depositReservation.requiredForSiteQuality()) {
                SiteQuality lowerQuality = quality.directLower().orElse(null);
                while (lowerQuality != null && lowerQuality.isProductive()) {
                    fallbackPlans.add(structureOnlyPlan(
                            siteType,
                            origin,
                            lowerQuality,
                            planSeed,
                            prospectorCampContext
                    ));
                    lowerQuality = lowerQuality.directLower().orElse(null);
                }
            }
            if (siteType == ExpeditionSiteType.MINER_CAMP
                    && (collidesWithStructure(context.level(), plan)
                    || fallbackPlans.stream().anyMatch(fallbackPlan -> collidesWithStructure(
                            context.level(), fallbackPlan)))) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.SURFACE_UNSUITABLE,
                        "the prospector-camp volume collides with an existing structure");
                return false;
            }
            if (siteType.naturalSurfaceSite() && !plan.isConnectedExpeditionSite()) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.DISCONNECTED_PLAN,
                        "the configured anchor distance window excludes the connected chamber");
                return false;
            }
            if (fallbackPlans.stream().anyMatch(fallbackPlan -> !fallbackPlan.isConnectedExpeditionSite()
                    || !withinBuildHeight(context.level(), fallbackPlan))) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                        "a lower-tier quality plan could not satisfy the connected build envelope");
                return false;
            }
            if (!withinBuildHeight(context.level(), plan)) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                        "the connected block plan could not be written safely");
                return false;
            }
            if (siteType.naturalSurfaceSite()) {
                boolean staged = IoePendingExpeditionSites.stage(
                        context.level(),
                        plan,
                        resourceProfile,
                        depositReservation,
                        petroleumReservation,
                        List.copyOf(fallbackPlans)
                );
                if (!staged) {
                    skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                            "an expedition site at the same anchor is already pending");
                    return false;
                }
                reservationTransferred = true;
            } else {
                IoeExpeditionPlanPlacement.AppliedPlan appliedPlan;
                try {
                    appliedPlan = IoeExpeditionPlanPlacement.apply(
                            context.level(),
                            plan
                    ).orElse(null);
                } catch (IoeExpeditionPlanPlacement.PlacementCompensationException failure) {
                    boolean restored = failure.appliedPlan().rollback(context.level());
                    IoeExpeditionWorldgenMod.LOGGER.error(
                            "Standalone IOE plan at {} failed with partial compensation; retryRestored={}",
                            origin,
                            restored,
                            failure
                    );
                    skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                            "the standalone block plan failed during compensated placement");
                    return false;
                }
                if (appliedPlan == null) {
                    skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                            "the standalone block plan could not be written safely");
                    return false;
                }
                appliedPlan.accept();
            }
            return true;
        } finally {
            if (!reservationTransferred) {
                rollbackReservationBestEffort(depositReservation, origin);
                rollbackReservoirReservationBestEffort(petroleumReservation, origin);
            }
        }
    }

    private static ExpeditionSiteBlockPlan structureOnlyPlan(
            ExpeditionSiteType siteType,
            BlockPos origin,
            SiteQuality quality,
            long planSeed,
            ProspectorCampContext prospectorCampContext
    ) {
        return ExpeditionSiteBlueprints.plan(
                siteType,
                origin,
                quality,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                RandomSource.create(planSeed),
                prospectorCampContext
        );
    }

    private static IoePetroleumReservoirReservation preparePetroleumReservoir(
            ServerLevel level,
            BlockPos origin,
            SiteQuality quality,
            BiomeMineResourceProfile resourceProfile,
            boolean naturalSurfaceSite
    ) {
        if (!naturalSurfaceSite
                || !quality.isProductive()
                || resourceProfile == null
                || !ModList.get().isLoaded(IoePetroleumReservoirRules.MOD_ID)) {
            return null;
        }
        ProvinceId province = ProvinceBindingResolver.fromConfig().resolve(resourceProfile.biomeId());
        Optional<IoePetroleumReservoirRules.PetroleumReservoirRequest> request =
                IoePetroleumReservoirRules.request(level, origin, quality, province, resourceProfile);
        if (request.isEmpty()) {
            return null;
        }
        try {
            return IoePetroleumReservoirBridge.reserveReservoir(level, request.orElseThrow()).orElse(null);
        } catch (RuntimeException | LinkageError failure) {
            IoePetroleumReservoirRules.recordReservationFailed();
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to prepare the optional Immersive Petroleum reservoir at {}; preserving the IOE site",
                    origin,
                    failure
            );
            return null;
        }
    }

    private static DepositPreparation prepareExcavatorDeposit(
            ServerLevel level,
            BlockPos origin,
            SiteQuality initialQuality,
            BiomeMineResourceProfile resourceProfile,
            boolean naturalSurfaceSite
    ) {
        IoeMotherDepositReservation[] reservation = new IoeMotherDepositReservation[1];
        ProvinceId province = resourceProfile == null
                ? null
                : ProvinceBindingResolver.fromConfig().resolve(resourceProfile.biomeId());
        IoeSiteQualityFallbackResolver.Resolution resolution = IoeSiteQualityFallbackResolver.resolve(
                initialQuality,
                SiteQuality::isProductive,
                quality -> {
                    if (!quality.isProductive()) {
                        return IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
                    }
                    if (!naturalSurfaceSite || resourceProfile == null || province == null) {
                        return IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
                    }
                    Optional<IoeExcavatorDepositRules.MotherDepositRequest> request =
                            IoeExcavatorDepositRules.depositRequest(
                                    origin,
                                    quality,
                                    province.id(),
                                    resourceProfile
                            );
                    if (request.isEmpty()) {
                        return IoeSiteQualityFallbackResolver.DepositAttempt.FAILED;
                    }
                    if (!ModList.get().isLoaded("immersiveengineering")) {
                        IoeExcavatorDepositRules.recordGuaranteedMotherIeAbsent();
                        return IoeSiteQualityFallbackResolver.DepositAttempt.FAILED;
                    }
                    try {
                        Optional<IoeMotherDepositReservation> reserved =
                                IoeExcavatorMotherDepositBridge.reserveGuaranteedDeposit(
                                        level,
                                        request.orElseThrow()
                                );
                        if (reserved.isEmpty()) {
                            return IoeSiteQualityFallbackResolver.DepositAttempt.FAILED;
                        }
                        reservation[0] = reserved.orElseThrow();
                        return IoeSiteQualityFallbackResolver.DepositAttempt.RESOLVED;
                    } catch (RuntimeException | LinkageError failure) {
                        IoeExcavatorDepositRules.recordGuaranteedMotherFailed();
                        IoeExpeditionWorldgenMod.LOGGER.error(
                                "Failed to reserve the IE Excavator deposit for quality={} at {} in {}; downgrading",
                                quality,
                                origin,
                                level.dimension().location(),
                                failure
                        );
                        return IoeSiteQualityFallbackResolver.DepositAttempt.FAILED;
                    }
                },
                (currentQuality, lowerQuality) -> {
                    IoeExpeditionWorldgenMod.LOGGER.warn(
                            "Downgraded IOE site at {} after IE deposit failure: {} -> {}",
                            origin,
                            currentQuality,
                            lowerQuality
                    );
                    return true;
                }
        );
        return new DepositPreparation(resolution, Optional.ofNullable(reservation[0]));
    }

    private static void rollbackReservationBestEffort(
            IoeMotherDepositReservation reservation,
            BlockPos origin
    ) {
        if (reservation == null) {
            return;
        }
        try {
            reservation.rollback();
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to roll back an unconfirmed IE deposit reservation at {}",
                    origin,
                    failure
            );
        }
    }

    private static void rollbackReservoirReservationBestEffort(
            IoePetroleumReservoirReservation reservation,
            BlockPos origin
    ) {
        if (reservation == null) {
            return;
        }
        try {
            reservation.rollback();
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to roll back an unconfirmed Immersive Petroleum reservoir reservation at {}",
                    origin,
                    failure
            );
        }
    }

    private static BlockPos resolveSurfaceOrigin(
            WorldGenLevel level,
            BlockPos requestedOrigin,
            ExpeditionSiteType siteType,
            SiteQuality quality
    ) {
        int localX = Math.floorMod(requestedOrigin.getX(), 16);
        int localZ = Math.floorMod(requestedOrigin.getZ(), 16);
        int safeLocalX = localX <= 7 ? 4 : 11;
        int x = requestedOrigin.getX() + safeLocalX - localX;
        int z = requestedOrigin.getZ() + clamp(localZ, 6, 9) - localZ;
        int horizontalDirection = safeLocalX <= 7 ? 1 : -1;
        int minX;
        int maxX;
        int minZ;
        int maxZ;
        int clearanceHeight;
        if (siteType == ExpeditionSiteType.MINER_CAMP) {
            ProspectorCampQualitySpec qualitySpec = ProspectorCampQualitySpec.forQuality(quality);
            int chunkMinX = Math.floorDiv(x, 16) * 16;
            int chunkMinZ = Math.floorDiv(z, 16) * 16;
            int centerX = chunkMinX + 7;
            int centerZ = chunkMinZ + 7;
            minX = centerX - qualitySpec.placedRadius();
            maxX = centerX + qualitySpec.placedRadius();
            minZ = centerZ - qualitySpec.placedRadius();
            maxZ = centerZ + qualitySpec.placedRadius();
            clearanceHeight = qualitySpec.maxHeight();
        } else {
            int firstX = x + horizontalDirection * -4;
            int lastX = x + horizontalDirection * 7;
            minX = Math.min(firstX, lastX);
            maxX = Math.max(firstX, lastX);
            minZ = z - 5;
            maxZ = z + 5;
            clearanceHeight = 6;
        }
        int allowedSlope = siteType == ExpeditionSiteType.MINER_CAMP ? 1 : 2;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int sampleX = minX; sampleX <= maxX; sampleX++) {
            for (int sampleZ = minZ; sampleZ <= maxZ; sampleZ++) {
                int surfaceY = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        sampleX,
                        sampleZ
                );
                BlockPos groundPos = new BlockPos(sampleX, surfaceY - 1, sampleZ);
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.getFluidState().isEmpty()) {
                    return null;
                }
                if (siteType == ExpeditionSiteType.MINER_CAMP
                        && !isNaturalProspectorCampGround(groundState)) {
                    return null;
                }
                minY = Math.min(minY, surfaceY);
                maxY = Math.max(maxY, surfaceY);
            }
        }
        if (maxY - minY > allowedSlope) {
            return null;
        }
        for (int hazardX = minX - SURFACE_HAZARD_MARGIN; hazardX <= maxX + SURFACE_HAZARD_MARGIN; hazardX++) {
            for (int hazardZ = minZ - SURFACE_HAZARD_MARGIN; hazardZ <= maxZ + SURFACE_HAZARD_MARGIN; hazardZ++) {
                int surfaceY = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        hazardX,
                        hazardZ
                );
                for (int hazardY = surfaceY - 2; hazardY <= surfaceY; hazardY++) {
                    BlockState state = level.getBlockState(new BlockPos(hazardX, hazardY, hazardZ));
                    if (state.is(Blocks.POWDER_SNOW) || state.getFluidState().is(FluidTags.LAVA)) {
                        return null;
                    }
                }
            }
        }
        for (int writeX = minX; writeX <= maxX; writeX++) {
            for (int writeZ = minZ; writeZ <= maxZ; writeZ++) {
                for (int dy = 0; dy < clearanceHeight; dy++) {
                    BlockState state = level.getBlockState(new BlockPos(writeX, maxY + dy, writeZ));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return null;
                    }
                }
            }
        }
        return new BlockPos(x, maxY, z);
    }

    private static boolean isNaturalProspectorCampGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.TERRACOTTA)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.SNOW)
                || state.is(Tags.Blocks.STONES)
                || state.is(Tags.Blocks.GRAVELS)
                || state.is(Tags.Blocks.SANDS)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MOSS_BLOCK);
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

    static BoundingBox expandedPlanBounds(Collection<BlockPos> plannedPositions) {
        if (plannedPositions.isEmpty()) {
            throw new IllegalArgumentException("Planned positions must not be empty");
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : plannedPositions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new BoundingBox(
                minX == Integer.MIN_VALUE ? minX : minX - 1,
                minY == Integer.MIN_VALUE ? minY : minY - 1,
                minZ == Integer.MIN_VALUE ? minZ : minZ - 1,
                maxX == Integer.MAX_VALUE ? maxX : maxX + 1,
                maxY == Integer.MAX_VALUE ? maxY : maxY + 1,
                maxZ == Integer.MAX_VALUE ? maxZ : maxZ + 1
        );
    }

    static boolean intersectsStructureBounds(
            Collection<BlockPos> plannedPositions,
            Collection<BoundingBox> structureBounds
    ) {
        BoundingBox candidateBounds = expandedPlanBounds(plannedPositions);
        return structureBounds.stream().anyMatch(candidateBounds::intersects);
    }

    private static boolean collidesWithStructure(WorldGenLevel level, ExpeditionSiteBlockPlan plan) {
        BoundingBox candidateBounds = expandedPlanBounds(plan.blocks().keySet());
        Set<StructureStart> starts = new HashSet<>();
        for (int chunkX = Math.floorDiv(candidateBounds.minX(), 16);
             chunkX <= Math.floorDiv(candidateBounds.maxX(), 16);
             chunkX++) {
            for (int chunkZ = Math.floorDiv(candidateBounds.minZ(), 16);
                 chunkZ <= Math.floorDiv(candidateBounds.maxZ(), 16);
                 chunkZ++) {
                starts.addAll(level.getLevel().structureManager().startsForStructure(
                        new net.minecraft.world.level.ChunkPos(chunkX, chunkZ),
                        ignored -> true
                ));
            }
        }
        List<BoundingBox> structureBounds = starts.stream()
                .filter(StructureStart::isValid)
                .map(StructureStart::getBoundingBox)
                .toList();
        return intersectsStructureBounds(plan.blocks().keySet(), structureBounds);
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

    private record DepositPreparation(
            IoeSiteQualityFallbackResolver.Resolution resolution,
            Optional<IoeMotherDepositReservation> reservation
    ) {
        private DepositPreparation {
            Objects.requireNonNull(resolution, "resolution");
            reservation = reservation == null ? Optional.empty() : reservation;
        }
    }

}
