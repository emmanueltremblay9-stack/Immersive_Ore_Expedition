package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge;
import com.oblixorprime.ioe.compat.ip.IoePetroleumReservoirBridge;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ExpeditionSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final SiteQualityRoll PRODUCTIVE_SITE_QUALITY = new SiteQualityRoll(0, 25, 45, 17, 3);
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

        SiteQuality quality = PRODUCTIVE_SITE_QUALITY.roll(context.random());
        long planSeed = context.random().nextLong();
        ExpeditionSiteBlockPlan previewPlan = structureOnlyPlan(siteType, origin, quality, planSeed);
        BiomeMineResourceProfile resourceProfile = null;
        if (quality.isProductive() && siteType.naturalSurfaceSite()) {
            BiomeMineResourceProfile.Resolution resolution = BiomeMineResourceProfile.resolve(
                    context.level(),
                    siteType.naturalSurfaceSite() ? previewPlan.chamberCenter() : origin
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
        ResourcePreparation resourcePreparation = prepareEmbeddedResource(resourceProfile);
        if (!resourcePreparation.resolved()) {
            skip(
                    origin,
                    resourcePreparation.failureReason(),
                    resourcePreparation.failureMessage()
            );
            return false;
        }
        ResolvedSiteResource embeddedResource = resourcePreparation.resource();

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
            ExpeditionSiteBlockPlan plan = planWithEmbeddedResource(
                    siteType,
                    origin,
                    quality,
                    planSeed,
                    embeddedResource
            );
            ArrayList<ExpeditionSiteBlockPlan> fallbackPlans = new ArrayList<>();
            if (depositReservation != null && depositReservation.requiredForSiteQuality()) {
                SiteQuality lowerQuality = quality.directLower().orElse(null);
                while (lowerQuality != null && lowerQuality.isProductive()) {
                    fallbackPlans.add(planWithEmbeddedResource(
                            siteType,
                            origin,
                            lowerQuality,
                            planSeed,
                            embeddedResource
                    ));
                    lowerQuality = lowerQuality.directLower().orElse(null);
                }
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

    private static ResourcePreparation prepareEmbeddedResource(BiomeMineResourceProfile resourceProfile) {
        if (resourceProfile == null) {
            return ResourcePreparation.resolved(null);
        }
        return prepareEmbeddedResource(resourceProfile.profileName());
    }

    static ResourcePreparation prepareEmbeddedResource(String profileName) {
        Objects.requireNonNull(profileName, "profileName");
        return switch (profileName) {
            case "certus" -> {
                Ae2MeteoriteIntegration.MeteoriteMaterial material =
                        Ae2MeteoriteIntegration.resolve().orElse(null);
                if (material == null) {
                    yield ResourcePreparation.failed(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.AE2_RESOURCE_MISSING,
                            "the biome requires AE2 and AE2 Crystal Science budding Certus resources"
                    );
                }
                if (!resourcesAllowed(material.buddingResource(), material.skyStoneResource())) {
                    yield ResourcePreparation.failed(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.RESOURCE_POLICY_DENIED,
                            "the biome Certus budding or shell resource is denied by policy"
                    );
                }
                yield ResourcePreparation.resolved(ResolvedSiteResource.specialGeode(
                        IoeWorldgenFeatureKeys.METEORITIC_AE2_GEODE,
                        material.buddingBlock(),
                        material.skyStoneBlock()
                ));
            }
            case "entroized_fluix" -> {
                ExtendedAeGeodeIntegration.GeodeMaterial material =
                        ExtendedAeGeodeIntegration.resolve().orElse(null);
                if (material == null) {
                    yield ResourcePreparation.failed(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.EXTENDED_AE_RESOURCE_MISSING,
                            "the biome requires ExtendedAE Entroized Fluix geode resources"
                    );
                }
                if (!resourcesAllowed(material.buddingResource(), material.shellResource())) {
                    yield ResourcePreparation.failed(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.RESOURCE_POLICY_DENIED,
                            "the biome Entroized Fluix budding or shell resource is denied by policy"
                    );
                }
                yield ResourcePreparation.resolved(ResolvedSiteResource.specialGeode(
                        IoeWorldgenFeatureKeys.ENTROIZED_FLUIX_GEODE,
                        material.buddingBlock(),
                        material.shellBlock()
                ));
            }
            default -> {
                GeOreNodeIntegration.NodeMaterial material =
                        GeOreNodeIntegration.resolve(profileName).orElse(null);
                if (material == null || !resourcesAllowed(material.nodeResource(), material.buddingResource())) {
                    yield ResourcePreparation.failed(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.RESOURCE_POLICY_DENIED,
                            "the biome GeOre node or budding resource is unavailable or denied by policy"
                    );
                }
                yield ResourcePreparation.resolved(ResolvedSiteResource.geOre(material));
            }
        };
    }

    private static boolean resourcesAllowed(ResourceRef... resources) {
        LoadedResourceScanner scanner = LoadedResourceScanner.runtime();
        for (ResourceRef resource : resources) {
            if (!RESOURCE_POLICY.evaluate(resource, scanner).shouldUse()) {
                return false;
            }
        }
        return true;
    }

    static ExpeditionSiteBlockPlan planWithEmbeddedResource(
            ExpeditionSiteType siteType,
            BlockPos origin,
            SiteQuality quality,
            long planSeed,
            ResolvedSiteResource resource
    ) {
        if (resource == null || !quality.isProductive()) {
            return structureOnlyPlan(siteType, origin, quality, planSeed);
        }
        if (resource.oreBlockId() != null) {
            return ExpeditionSiteBlueprints.plan(
                    siteType,
                    origin,
                    quality,
                    resource.oreBlockId(),
                    resource.oreState(),
                    resource.oreNodeHeartBlockId(),
                    resource.oreNodeHeartState(),
                    ExpeditionSiteBlueprints.oreCount(quality),
                    ExpeditionSiteBlueprints.oreNodeCount(quality),
                    RandomSource.create(planSeed)
            );
        }
        return ExpeditionSiteBlueprints.plan(
                siteType,
                origin,
                quality,
                null,
                null,
                null,
                null,
                resource.specialGeodeComponentId(),
                resource.specialBuddingState(),
                resource.specialShellState(),
                0,
                0,
                RandomSource.create(planSeed)
        );
    }

    private static ExpeditionSiteBlockPlan structureOnlyPlan(
            ExpeditionSiteType siteType,
            BlockPos origin,
            SiteQuality quality,
            long planSeed
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
                RandomSource.create(planSeed)
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
            ExpeditionSiteType siteType
    ) {
        int localX = Math.floorMod(requestedOrigin.getX(), 16);
        int localZ = Math.floorMod(requestedOrigin.getZ(), 16);
        int safeLocalX = localX <= 7 ? 4 : 11;
        int x = requestedOrigin.getX() + safeLocalX - localX;
        int z = requestedOrigin.getZ() + clamp(localZ, 6, 9) - localZ;
        int horizontalDirection = safeLocalX <= 7 ? 1 : -1;
        int allowedSlope = 2;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int forward : new int[]{-4, 0, 7}) {
            for (int lateral : new int[]{-5, 0, 5}) {
                int surfaceY = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x + horizontalDirection * forward,
                        z + lateral
                );
                minY = Math.min(minY, surfaceY);
                maxY = Math.max(maxY, surfaceY);
            }
        }
        if (maxY - minY > allowedSlope) {
            return null;
        }
        for (int forward = -4; forward <= 7; forward++) {
            for (int lateral = -5; lateral <= 5; lateral++) {
                for (int dy = 0; dy <= 5; dy++) {
                    BlockState state = level.getBlockState(new BlockPos(
                            x + horizontalDirection * forward,
                            maxY + dy,
                            z + lateral
                    ));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return null;
                    }
                }
            }
        }
        return new BlockPos(x, maxY, z);
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

    record ResourcePreparation(
            ResolvedSiteResource resource,
            IoeWorldgenRuntimeDiagnostics.SiteSkipReason failureReason,
            String failureMessage
    ) {
        ResourcePreparation {
            if ((failureReason == null) != (failureMessage == null)) {
                throw new IllegalArgumentException("Resource failure reason and message must be paired");
            }
            if (resource != null && failureReason != null) {
                throw new IllegalArgumentException("Resolved resources cannot also carry a failure");
            }
        }

        private static ResourcePreparation resolved(ResolvedSiteResource resource) {
            return new ResourcePreparation(resource, null, null);
        }

        private static ResourcePreparation failed(
                IoeWorldgenRuntimeDiagnostics.SiteSkipReason failureReason,
                String failureMessage
        ) {
            return new ResourcePreparation(
                    null,
                    Objects.requireNonNull(failureReason, "failureReason"),
                    Objects.requireNonNull(failureMessage, "failureMessage")
            );
        }

        boolean resolved() {
            return failureReason == null;
        }
    }

    record ResolvedSiteResource(
            ResourceLocation oreBlockId,
            BlockState oreState,
            ResourceLocation oreNodeHeartBlockId,
            BlockState oreNodeHeartState,
            ResourceLocation specialGeodeComponentId,
            BlockState specialBuddingState,
            BlockState specialShellState
    ) {
        ResolvedSiteResource {
            boolean geOre = oreBlockId != null
                    && oreState != null
                    && oreNodeHeartBlockId != null
                    && oreNodeHeartState != null;
            boolean specialGeode = specialGeodeComponentId != null
                    && specialBuddingState != null
                    && specialShellState != null;
            if (geOre == specialGeode) {
                throw new IllegalArgumentException(
                        "An embedded site resource must be exactly one GeOre node or special geode"
                );
            }
        }

        private static ResolvedSiteResource geOre(GeOreNodeIntegration.NodeMaterial material) {
            return new ResolvedSiteResource(
                    material.nodeResource().id(),
                    material.nodeBlock().defaultBlockState(),
                    material.buddingResource().id(),
                    material.buddingBlock().defaultBlockState(),
                    null,
                    null,
                    null
            );
        }

        private static ResolvedSiteResource specialGeode(
                ResourceLocation componentId,
                Block buddingBlock,
                Block shellBlock
        ) {
            return new ResolvedSiteResource(
                    null,
                    null,
                    null,
                    null,
                    componentId,
                    buddingBlock.defaultBlockState(),
                    shellBlock.defaultBlockState()
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
