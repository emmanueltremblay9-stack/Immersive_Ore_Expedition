package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.core.SiteQualityRoll;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.fml.ModList;

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
        BiomeMineResourceProfile resourceProfile = null;
        GeOreNodeIntegration.NodeMaterial geOreMaterial = null;
        SpecialMineGeode specialGeode = null;
        if (quality.isProductive()) {
            BiomeMineResourceProfile.Resolution resolution = BiomeMineResourceProfile.resolve(
                    context.level(),
                    origin
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
            switch (resourceProfile.resourceKind()) {
                case GEORE -> {
                    geOreMaterial = GeOreNodeIntegration.resolve(resourceProfile.profileName()).orElse(null);
                    if (!usableGeOreMaterial(geOreMaterial)) {
                        skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.RESOURCE_POLICY_DENIED,
                                "the biome GeOre node or budding resource is unavailable or denied by policy");
                        return false;
                    }
                }
                case AE2_CERTUS -> {
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
                                "the biome requires AE2 and AE2 Crystal Science budding Certus resources");
                        return false;
                    }
                }
                case EXTENDEDAE_FLUIX -> {
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
                                "the biome requires ExtendedAE Entroized Fluix geode resources");
                        return false;
                    }
                }
            }
        }

        DepositPreparation depositPreparation = prepareMotherDeposit(
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
        boolean geOreMine = geOreMaterial != null;

        boolean reservationTransferred = false;
        try {
            ExpeditionSiteBlockPlan plan = ExpeditionSiteBlueprints.plan(
                    siteType,
                    origin,
                    quality,
                    geOreMine ? geOreMaterial.nodeResource().id() : null,
                    geOreMine ? geOreMaterial.nodeBlock().defaultBlockState() : null,
                    geOreMine ? geOreMaterial.buddingResource().id() : null,
                    geOreMine ? geOreMaterial.buddingBlock().defaultBlockState() : null,
                    specialGeode == null ? null : specialGeode.componentId(),
                    specialGeode == null ? null : specialGeode.buddingBlock().defaultBlockState(),
                    specialGeode == null ? null : specialGeode.shellBlock().defaultBlockState(),
                    resourceProfile == null ? 0 : resourceProfile.oreBudget(quality),
                    resourceProfile == null ? 0 : resourceProfile.nodeCount(quality),
                    resourceProfile == null ? 0 : resourceProfile.specialBuddingCount(quality),
                    context.random()
            );
            ExpeditionSiteBlockPlan fallbackPlan = null;
            if (depositReservation != null && depositReservation.requiredForSiteQuality()) {
                SiteQuality lowerQuality = quality.directLower().orElse(null);
                if (lowerQuality == null) {
                    skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.IE_DEPOSIT_MISSING,
                            "a required IE deposit had no lower quality fallback");
                    return false;
                }
                fallbackPlan = ExpeditionSiteBlueprints.plan(
                        siteType,
                        origin,
                        lowerQuality,
                        geOreMine ? geOreMaterial.nodeResource().id() : null,
                        geOreMine ? geOreMaterial.nodeBlock().defaultBlockState() : null,
                        geOreMine ? geOreMaterial.buddingResource().id() : null,
                        geOreMine ? geOreMaterial.buddingBlock().defaultBlockState() : null,
                        specialGeode == null ? null : specialGeode.componentId(),
                        specialGeode == null ? null : specialGeode.buddingBlock().defaultBlockState(),
                        specialGeode == null ? null : specialGeode.shellBlock().defaultBlockState(),
                        resourceProfile == null ? 0 : resourceProfile.oreBudget(lowerQuality),
                        resourceProfile == null ? 0 : resourceProfile.nodeCount(lowerQuality),
                        resourceProfile == null ? 0 : resourceProfile.specialBuddingCount(lowerQuality),
                        net.minecraft.util.RandomSource.create(context.random().nextLong())
                );
            }
            if (siteType.naturalSurfaceSite() && !plan.isConnectedExpeditionSite()) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.DISCONNECTED_PLAN,
                        "the configured anchor distance window excludes the connected chamber");
                return false;
            }
            if (fallbackPlan != null && (!fallbackPlan.isConnectedExpeditionSite()
                    || !withinBuildHeight(context.level(), fallbackPlan))) {
                skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                        "the direct lower quality plan could not satisfy the connected build envelope");
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
                        fallbackPlan
                );
                if (!staged) {
                    skip(origin, IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE,
                            "an expedition site at the same anchor is already pending");
                    return false;
                }
                reservationTransferred = true;
            } else {
                IoeExpeditionPlanPlacement.AppliedPlan appliedPlan =
                        IoeExpeditionPlanPlacement.apply(context.level(), plan).orElse(null);
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
            }
        }
    }

    private static DepositPreparation prepareMotherDeposit(
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
                ignored -> true,
                quality -> {
                    if (!naturalSurfaceSite || resourceProfile == null || province == null) {
                        return IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
                    }
                    Optional<IoeExcavatorDepositRules.MotherDepositRequest> request =
                            IoeExcavatorDepositRules.guaranteedMotherRequest(
                                    origin,
                                    quality,
                                    province.id(),
                                    resourceProfile
                            );
                    if (request.isEmpty()) {
                        return IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
                    }
                    if (!ModList.get().isLoaded("immersiveengineering")) {
                        IoeExcavatorDepositRules.recordGuaranteedMotherIeAbsent();
                        return IoeSiteQualityFallbackResolver.DepositAttempt.NOT_REQUIRED;
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
        if (resolution.confirmed()
                && reservation[0] == null
                && naturalSurfaceSite
                && resourceProfile != null
                && province != null
                && ModList.get().isLoaded("immersiveengineering")) {
            Optional<IoeExcavatorDepositRules.MotherDepositRequest> optionalMajor =
                    IoeExcavatorDepositRules.optionalMajorRequest(
                            level,
                            origin,
                            resolution.finalQuality(),
                            province.id(),
                            resourceProfile
                    );
            if (optionalMajor.isPresent()) {
                try {
                    reservation[0] = IoeExcavatorMotherDepositBridge.reserveOptionalDeposit(
                            level,
                            optionalMajor.orElseThrow()
                    ).orElse(null);
                } catch (RuntimeException | LinkageError failure) {
                    IoeExcavatorDepositRules.recordOptionalMajorFailed();
                    IoeExpeditionWorldgenMod.LOGGER.error(
                            "Failed to prepare the optional IE Excavator deposit for Major Node at {}",
                            origin,
                            failure
                    );
                }
            }
        }
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

    private static boolean usableGeOreMaterial(GeOreNodeIntegration.NodeMaterial material) {
        return material != null
                && RESOURCE_POLICY.evaluate(
                material.nodeResource(),
                LoadedResourceScanner.runtime()
        ).shouldUse()
                && RESOURCE_POLICY.evaluate(
                material.buddingResource(),
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
