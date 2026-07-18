package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge;
import com.oblixorprime.ioe.compat.ip.IoePetroleumReservoirBridge;
import com.oblixorprime.ioe.core.ProvinceId;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSitePlacementState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Defers every durable site mutation until the new-chunk guard's final server-thread pass. Worldgen can run in
 * temporary dependency chunks that never become durable; those plans must never write blocks, create IE veins,
 * become compass targets, or retain unbounded pending state.
 */
final class IoePendingExpeditionSites {
    private static final int MAX_PENDING_CHUNKS = 256;
    private static final int PRUNE_INTERVAL_TICKS = 1200;
    private static final long MAX_PENDING_AGE_NANOS = Duration.ofMinutes(10).toNanos();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final AtomicLong NEXT_SEQUENCE = new AtomicLong();
    private static final ConcurrentHashMap<
            ResourceKey<Level>,
            ConcurrentHashMap<Long, List<PendingSite>>
    > PENDING = new ConcurrentHashMap<>();

    private IoePendingExpeditionSites() {
    }

    static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(IoePendingExpeditionSites::onServerTick);
        }
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile
    ) {
        return stage(worldGenLevel, plan, resourceProfile, null, null, List.of());
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation
    ) {
        return stage(worldGenLevel, plan, resourceProfile, motherDepositReservation, null, List.of());
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation,
            ExpeditionSiteBlockPlan fallbackPlan
    ) {
        return stage(
                worldGenLevel,
                plan,
                resourceProfile,
                motherDepositReservation,
                null,
                fallbackPlan == null ? List.of() : List.of(fallbackPlan)
        );
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation,
            IoePetroleumReservoirReservation petroleumReservoirReservation,
            List<ExpeditionSiteBlockPlan> fallbackPlans
    ) {
        Objects.requireNonNull(worldGenLevel, "worldGenLevel");
        ServerLevel level = Objects.requireNonNull(worldGenLevel.getLevel(), "level");
        Objects.requireNonNull(plan, "plan");
        ChunkPos chunkPos = new ChunkPos(plan.anchorPos());
        PendingSite pendingSite = pendingSite(
                level,
                plan,
                resourceProfile,
                motherDepositReservation,
                petroleumReservoirReservation,
                fallbackPlans,
                chunkPos
        );
        level.getServer().execute(() -> prune(level));
        ConcurrentHashMap<Long, List<PendingSite>> byChunk = PENDING.computeIfAbsent(
                level.dimension(),
                ignored -> new ConcurrentHashMap<>()
        );
        AtomicBoolean accepted = new AtomicBoolean();
        byChunk.compute(
                chunkPos.toLong(),
                (ignored, existing) -> appendDistinct(existing, pendingSite, accepted)
        );
        return accepted.get();
    }

    static int pendingResourcePositionCount() {
        return PENDING.values().stream()
                .flatMap(byChunk -> byChunk.values().stream())
                .flatMap(List::stream)
                .mapToInt(pendingSite -> pendingSite.signature().resourcePositionCount())
                .sum();
    }

    /**
     * Runs only from the new-chunk guard after its final sanitization pass. It applies the plan, commits the
     * optional IE deposit, and records the locator entry as one compensatable server-thread transaction.
     */
    static Confirmation confirmLoadedChunk(ServerLevel level, ChunkPos chunkPos) {
        return confirmLoadedChunk(
                level,
                chunkPos,
                IoeExcavatorMotherDepositBridge::reserveGuaranteedDeposit
        );
    }

    static Confirmation confirmLoadedChunk(
            ServerLevel level,
            ChunkPos chunkPos,
            DepositReservationFactory depositReservationFactory
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");
        Objects.requireNonNull(depositReservationFactory, "depositReservationFactory");
        if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            return Confirmation.NONE;
        }
        ConcurrentHashMap<Long, List<PendingSite>> byChunk = PENDING.get(level.dimension());
        if (byChunk == null) {
            return Confirmation.NONE;
        }
        List<PendingSite> pendingSites = byChunk.remove(chunkPos.toLong());
        if (pendingSites == null) {
            return Confirmation.NONE;
        }
        ArrayList<BlockPos> rejectedResourcePositions = new ArrayList<>();
        int confirmedSites = 0;
        int rejectedSites = 0;
        for (PendingSite pendingSite : pendingSites) {
            IoeExpeditionPlanPlacement.AppliedPlan effectivePlacement;
            try {
                effectivePlacement = IoeExpeditionPlanPlacement.apply(
                        level,
                        pendingSite.plan()
                ).orElse(null);
            } catch (IoeExpeditionPlanPlacement.PlacementCompensationException failure) {
                rollbackPlacementBestEffort(
                        level,
                        failure.appliedPlan(),
                        pendingSite,
                        "partial final plan application"
                );
                effectivePlacement = null;
            }
            if (effectivePlacement == null) {
                rejectedSites++;
                rollbackReservationBestEffort(pendingSite, "final plan application failed");
                pendingSite.signature().appendResourcePositions(rejectedResourcePositions);
                IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                        IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE
                );
                IoeExpeditionWorldgenMod.LOGGER.warn(
                        "Discarded unsafe IOE expedition plan type={} anchor={} chunk={}",
                        pendingSite.summary().requestedFeatureId(),
                        pendingSite.site().pos(),
                        chunkPos
                );
                continue;
            }

            PendingSite effectiveSite = pendingSite;
            IoeMotherDepositReservation reservation = pendingSite.motherDepositReservation();
            if (reservation != null) {
                try {
                    reservation.commit();
                } catch (RuntimeException | LinkageError failure) {
                    rollbackMotherReservationBestEffort(pendingSite, "commit failure");
                    if (!reservation.requiredForSiteQuality()) {
                        IoeExpeditionWorldgenMod.LOGGER.error(
                                "Discarded the optional IE Major deposit at {}; preserving the confirmed IOE site",
                                pendingSite.site().pos(),
                                failure
                        );
                    } else {
                        IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                                IoeWorldgenRuntimeDiagnostics.SiteSkipReason.IE_DEPOSIT_MISSING
                        );
                        IoeExpeditionWorldgenMod.LOGGER.error(
                                "Failed to commit the IE deposit at {}; applying the direct lower quality pipeline",
                                pendingSite.site().pos(),
                                failure
                        );
                        FallbackApplication fallback = applyLowerTierFallbacks(
                                level,
                                pendingSite,
                                effectivePlacement,
                                chunkPos,
                                depositReservationFactory
                        );
                        if (fallback == null) {
                            rejectedSites++;
                            rollbackPetroleumReservationBestEffort(
                                    pendingSite,
                                    "IE direct lower quality fallback failed"
                            );
                            pendingSite.signature().appendResourcePositions(rejectedResourcePositions);
                            continue;
                        }
                        effectiveSite = fallback.site();
                        effectivePlacement = fallback.placement();
                    }
                }
            }

            IoePetroleumReservoirReservation petroleumReservation =
                    effectiveSite.petroleumReservoirReservation();
            if (petroleumReservation != null) {
                try {
                    petroleumReservation.commit();
                } catch (RuntimeException | LinkageError failure) {
                    rollbackPetroleumReservationBestEffort(effectiveSite, "commit failure");
                    IoeExpeditionWorldgenMod.LOGGER.error(
                            "Discarded the optional Immersive Petroleum reservoir at {}; preserving the confirmed IOE site",
                            effectiveSite.site().pos(),
                            failure
                    );
                }
            }

            try {
                ExpeditionLocatorService.record(level, effectiveSite.site());
            } catch (RuntimeException | LinkageError failure) {
                rejectedSites++;
                rollbackReservationBestEffort(effectiveSite, "locator failure after reservation commit");
                rollbackPlacementBestEffort(level, effectivePlacement, effectiveSite, "locator failure");
                effectiveSite.signature().appendResourcePositions(rejectedResourcePositions);
                IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                        IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE
                );
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Failed to persist the final IOE locator entry at {}; compensated the site transaction",
                        effectiveSite.site().pos(),
                        failure
                );
                continue;
            }
            effectivePlacement.accept();
            IoeWorldgenRuntimeDiagnostics.recordSitePlaced();
            confirmedSites++;
            logConfirmedSite(effectiveSite);
        }
        return new Confirmation(confirmedSites, rejectedSites, List.copyOf(rejectedResourcePositions));
    }

    private static FallbackApplication applyLowerTierFallbacks(
            ServerLevel level,
            PendingSite pendingSite,
            IoeExpeditionPlanPlacement.AppliedPlan initialPlacement,
            ChunkPos chunkPos,
            DepositReservationFactory depositReservationFactory
    ) {
        List<ExpeditionSiteBlockPlan> fallbackPlans = pendingSite.fallbackPlans();
        if (fallbackPlans.isEmpty()) {
            rollbackPlacementBestEffort(level, initialPlacement, pendingSite, "missing lower quality fallback");
            return null;
        }
        SiteQuality currentQuality = pendingSite.site().quality().orElseThrow();
        SiteQuality expectedLower = currentQuality;
        for (ExpeditionSiteBlockPlan fallbackPlan : fallbackPlans) {
            expectedLower = expectedLower.directLower().orElse(null);
            if (expectedLower == null || !expectedLower.isProductive() || fallbackPlan.quality() != expectedLower) {
                rollbackPlacementBestEffort(level, initialPlacement, pendingSite, "invalid lower quality fallback chain");
                return null;
            }
        }
        if (!rollbackPlacementBestEffort(
                level,
                initialPlacement,
                pendingSite,
                "lower-tier quality downgrade"
        )) {
            return null;
        }
        BiomeMineResourceProfile resourceProfile = pendingSite.resourceProfile();
        ProvinceId province = ProvinceBindingResolver.fromConfig().resolve(resourceProfile.biomeId());
        for (ExpeditionSiteBlockPlan fallbackPlan : fallbackPlans) {
            IoeMotherDepositReservation fallbackDepositReservation = null;
            try {
                Optional<IoeExcavatorDepositRules.MotherDepositRequest> fallbackRequest =
                        IoeExcavatorDepositRules.depositRequest(
                                pendingSite.site().pos(),
                                fallbackPlan.quality(),
                                province.id(),
                                resourceProfile
                        );
                if (fallbackRequest.isEmpty()) {
                    continue;
                }
                fallbackDepositReservation = depositReservationFactory.reserve(
                        level,
                        fallbackRequest.orElseThrow()
                ).orElse(null);
                if (fallbackDepositReservation == null) {
                    continue;
                }
                fallbackDepositReservation.commit();
            } catch (RuntimeException | LinkageError failure) {
                rollbackReservationBestEffort(
                        fallbackDepositReservation,
                        pendingSite.site().pos(),
                        "lower-tier IE deposit commit failure"
                );
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Failed to commit the {} IE deposit at {}; trying the next lower tier",
                        fallbackPlan.quality(),
                        pendingSite.site().pos(),
                        failure
                );
                continue;
            }

            IoeExpeditionPlanPlacement.AppliedPlan fallbackPlacement;
            try {
                fallbackPlacement = IoeExpeditionPlanPlacement.apply(
                        level,
                        fallbackPlan
                ).orElse(null);
            } catch (IoeExpeditionPlanPlacement.PlacementCompensationException failure) {
                rollbackPlacementBestEffort(
                        level,
                        failure.appliedPlan(),
                        pendingSite,
                        "partial lower-tier site placement"
                );
                fallbackPlacement = null;
            }
            if (fallbackPlacement == null) {
                rollbackReservationBestEffort(
                        fallbackDepositReservation,
                        pendingSite.site().pos(),
                        "lower-tier site placement failure"
                );
                continue;
            }
            IoePetroleumReservoirReservation fallbackPetroleumReservation = null;
            try {
                fallbackPetroleumReservation =
                        prepareFallbackPetroleumReservation(level, pendingSite, fallbackPlan.quality());
                PendingSite fallbackSite = pendingSite(
                        level,
                        fallbackPlan,
                        resourceProfile,
                        fallbackDepositReservation,
                        fallbackPetroleumReservation,
                        List.of(),
                        chunkPos
                );
                IoeExpeditionWorldgenMod.LOGGER.warn(
                        "Downgraded IOE site after IE deposit commit failure: anchor={} {} -> {}",
                        pendingSite.site().pos(),
                        currentQuality,
                        fallbackPlan.quality()
                );
                return new FallbackApplication(fallbackSite, fallbackPlacement);
            } catch (RuntimeException | LinkageError failure) {
                rollbackPlacementBestEffort(
                        level,
                        fallbackPlacement,
                        pendingSite,
                        "failed lower-tier pending-site construction"
                );
                rollbackReservationBestEffort(
                        fallbackDepositReservation,
                        pendingSite.site().pos(),
                        "failed to create lower quality pending site"
                );
                rollbackPetroleumReservationBestEffort(
                        fallbackPetroleumReservation,
                        pendingSite.site().pos(),
                        "failed to create lower quality pending site"
                );
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Failed to stage the {} lower quality plan at {}; trying the next lower tier",
                        fallbackPlan.quality(),
                        pendingSite.site().pos(),
                        failure
                );
            }
        }
        return null;
    }

    @FunctionalInterface
    interface DepositReservationFactory {
        Optional<IoeMotherDepositReservation> reserve(
                ServerLevel level,
                IoeExcavatorDepositRules.MotherDepositRequest request
        );
    }

    private static IoePetroleumReservoirReservation prepareFallbackPetroleumReservation(
            ServerLevel level,
            PendingSite pendingSite,
            SiteQuality fallbackQuality
    ) {
        rollbackPetroleumReservationBestEffort(pendingSite, "direct quality downgrade requalification");
        if (!ModList.get().isLoaded(IoePetroleumReservoirRules.MOD_ID)) {
            return null;
        }
        BiomeMineResourceProfile resourceProfile = pendingSite.resourceProfile();
        ProvinceId province = ProvinceBindingResolver.fromConfig().resolve(resourceProfile.biomeId());
        Optional<IoePetroleumReservoirRules.PetroleumReservoirRequest> request =
                IoePetroleumReservoirRules.request(
                        level,
                        pendingSite.site().pos(),
                        fallbackQuality,
                        province,
                        resourceProfile
                );
        if (request.isEmpty()) {
            return null;
        }
        try {
            return IoePetroleumReservoirBridge.reserveReservoir(level, request.orElseThrow()).orElse(null);
        } catch (RuntimeException | LinkageError failure) {
            IoePetroleumReservoirRules.recordReservationFailed();
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to requalify the optional Immersive Petroleum reservoir at {} after site downgrade",
                    pendingSite.site().pos(),
                    failure
            );
            return null;
        }
    }

    static void clear() {
        PENDING.forEach((dimension, byChunk) -> {
            byChunk.values().forEach(sites -> sites.forEach(site -> {
                rollbackReservationBestEffort(site, "pending state cleared");
            }));
        });
        PENDING.clear();
    }

    /**
     * Treats the node's generation chunk as its deterministic IOE region cell. The accepted IE vein keeps its
     * own radius, so the deposit may extend beyond that cell without making admission depend on neighboring
     * chunk generation order.
     */
    static List<ExcavatorRegion> excavatorRegions(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(chunkPos, "chunkPos");
        ConcurrentHashMap<Long, List<PendingSite>> byChunk = PENDING.get(dimension);
        if (byChunk == null) {
            return List.of();
        }
        List<PendingSite> pendingSites = byChunk.get(chunkPos.toLong());
        if (pendingSites == null) {
            return List.of();
        }
        return pendingSites.stream()
                .filter(pendingSite -> pendingSite.site().quality().isPresent())
                .filter(pendingSite -> pendingSite.site().provinceId().isPresent())
                .map(IoePendingExpeditionSites::toExcavatorRegion)
                .toList();
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % PRUNE_INTERVAL_TICKS != 0 || PENDING.isEmpty()) {
            return;
        }
        for (ResourceKey<Level> dimension : List.copyOf(PENDING.keySet())) {
            ServerLevel level = event.getServer().getLevel(dimension);
            if (level == null) {
                ConcurrentHashMap<Long, List<PendingSite>> removed = PENDING.remove(dimension);
                if (removed != null) {
                    removed.values().forEach(sites -> sites.forEach(
                            site -> rollbackReservationBestEffort(site, "dimension unavailable")
                    ));
                }
                IoeOrePlacementAuthorization.releaseDimension(dimension);
            } else {
                prune(level);
            }
        }
    }

    private static PendingSite pendingSite(
            ServerLevel level,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation,
            IoePetroleumReservoirReservation petroleumReservoirReservation,
            List<ExpeditionSiteBlockPlan> fallbackPlans,
            ChunkPos chunkPos
    ) {
        Objects.requireNonNull(resourceProfile, "resourceProfile");
        ResourceLocation biomeId = resourceProfile.biomeId();
        ProvinceBindingResolver bindingResolver = ProvinceBindingResolver.fromConfig();
        ProvinceId province = bindingResolver.resolve(biomeId);
        ExpeditionSite site = ExpeditionSite.anchor(
                level.dimension(),
                plan.anchorPos(),
                plan.requestedFeatureId(),
                province.id(),
                plan.quality(),
                "natural_connected_expedition_site",
                ExpeditionSitePlacementState.PROVEN,
                null
        );
        int connectedBiomeChunks = resourceProfile.sampledConnectedChunks();
        return new PendingSite(
                site,
                plan,
                SiteSummary.from(plan),
                biomeId,
                connectedBiomeChunks,
                resourceProfile,
                PlanSignature.from(plan, chunkPos, motherDepositReservation),
                motherDepositReservation,
                petroleumReservoirReservation,
                fallbackPlans,
                NEXT_SEQUENCE.incrementAndGet(),
                System.nanoTime()
        );
    }

    private static List<PendingSite> appendDistinct(
            List<PendingSite> existing,
            PendingSite pendingSite,
            AtomicBoolean accepted
    ) {
        if (existing == null) {
            accepted.set(true);
            return List.of(pendingSite);
        }
        if (existing.stream().anyMatch(candidate -> candidate.site().pos().equals(pendingSite.site().pos()))) {
            rollbackReservationBestEffort(pendingSite, "duplicate pending site");
            return existing;
        }
        ArrayList<PendingSite> updated = new ArrayList<>(existing);
        updated.add(pendingSite);
        accepted.set(true);
        return List.copyOf(updated);
    }

    private static ExcavatorRegion toExcavatorRegion(PendingSite pendingSite) {
        return new ExcavatorRegion(
                pendingSite.site().pos(),
                pendingSite.site().quality().orElseThrow(),
                pendingSite.site().provinceId().orElseThrow(),
                pendingSite.resourceProfile(),
                pendingSite.motherDepositReservation() != null
        );
    }

    private static void prune(ServerLevel level) {
        ConcurrentHashMap<Long, List<PendingSite>> byChunk = PENDING.get(level.dimension());
        if (byChunk == null) {
            return;
        }
        long cutoff = System.nanoTime() - MAX_PENDING_AGE_NANOS;
        for (Map.Entry<Long, List<PendingSite>> entry : byChunk.entrySet()) {
            if (entry.getValue().stream().allMatch(site -> site.createdNanos() < cutoff)
                    && byChunk.remove(entry.getKey(), entry.getValue())) {
                entry.getValue().forEach(site -> {
                    rollbackReservationBestEffort(site, "pending site expired");
                });
                IoeOrePlacementAuthorization.releaseChunk(level.dimension(), new ChunkPos(entry.getKey()));
            }
        }
        while (byChunk.size() > MAX_PENDING_CHUNKS) {
            Map.Entry<Long, List<PendingSite>> oldest = byChunk.entrySet().stream()
                    .min(java.util.Comparator.comparingLong(entry -> entry.getValue().stream()
                            .mapToLong(PendingSite::sequence)
                            .min()
                            .orElse(Long.MAX_VALUE)))
                    .orElse(null);
            if (oldest == null || !byChunk.remove(oldest.getKey(), oldest.getValue())) {
                break;
            }
            oldest.getValue().forEach(site -> {
                rollbackReservationBestEffort(site, "pending capacity eviction");
            });
            IoeOrePlacementAuthorization.releaseChunk(level.dimension(), new ChunkPos(oldest.getKey()));
        }
    }

    private static void rollbackReservationBestEffort(PendingSite pendingSite, String reason) {
        rollbackMotherReservationBestEffort(pendingSite, reason);
        rollbackPetroleumReservationBestEffort(pendingSite, reason);
    }

    private static void rollbackMotherReservationBestEffort(PendingSite pendingSite, String reason) {
        rollbackReservationBestEffort(
                pendingSite.motherDepositReservation(),
                pendingSite.site().pos(),
                reason
        );
    }

    private static void rollbackReservationBestEffort(
            IoeMotherDepositReservation reservation,
            BlockPos anchorPos,
            String reason
    ) {
        if (reservation == null) {
            return;
        }
        try {
            reservation.rollback();
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to roll back IE deposit reservation at {} reason={}",
                    anchorPos,
                    reason,
                    failure
            );
        }
    }

    private static void rollbackPetroleumReservationBestEffort(PendingSite pendingSite, String reason) {
        rollbackPetroleumReservationBestEffort(
                pendingSite.petroleumReservoirReservation(),
                pendingSite.site().pos(),
                reason
        );
    }

    private static void rollbackPetroleumReservationBestEffort(
            IoePetroleumReservoirReservation reservation,
            BlockPos anchorPos,
            String reason
    ) {
        if (reservation == null) {
            return;
        }
        try {
            reservation.rollback();
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to roll back Immersive Petroleum reservoir reservation at {} reason={}",
                    anchorPos,
                    reason,
                    failure
            );
        }
    }

    private static boolean rollbackPlacementBestEffort(
            ServerLevel level,
            IoeExpeditionPlanPlacement.AppliedPlan appliedPlan,
            PendingSite pendingSite,
            String reason
    ) {
        try {
            boolean restored = appliedPlan.rollback(level);
            if (!restored) {
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Could not fully compensate IOE site blocks at {} reason={}",
                        pendingSite.site().pos(),
                        reason
                );
            }
            return restored;
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to compensate IOE site blocks at {} reason={}",
                    pendingSite.site().pos(),
                    reason,
                    failure
            );
            return false;
        }
    }

    private static void logConfirmedSite(PendingSite pendingSite) {
        if (!IoeWorldgenConfig.runtimePlacementDiagnostics()) {
            return;
        }
        SiteSummary summary = pendingSite.summary();
        IoeExpeditionWorldgenMod.LOGGER.info(
                "Generated connected IOE expedition site type={} anchor={} chamber={} rooms={} biome={} connectedBiomeChunks={} quality={} ore={} oreHeart={} oreNodes={} oreHearts={} oreBlocks={} blocks={}",
                summary.requestedFeatureId(),
                pendingSite.site().pos(),
                summary.chamberCenter(),
                summary.roomCount(),
                pendingSite.biomeId(),
                pendingSite.connectedBiomeChunks(),
                pendingSite.site().quality().orElse(null),
                summary.oreBlockId(),
                summary.oreNodeHeartBlockId(),
                summary.oreNodeCount(),
                summary.oreNodeHeartCount(),
                summary.oreBlockCount(),
                summary.blockCount()
        );
    }

    record Confirmation(int confirmedSites, int rejectedSites, List<BlockPos> rejectedResourcePositions) {
        private static final Confirmation NONE = new Confirmation(0, 0, List.of());

        Confirmation {
            if (confirmedSites < 0 || rejectedSites < 0) {
                throw new IllegalArgumentException("Confirmation counts cannot be negative");
            }
            rejectedResourcePositions = List.copyOf(Objects.requireNonNull(
                    rejectedResourcePositions,
                    "rejectedResourcePositions"
            ));
        }
    }

    private record PendingSite(
            ExpeditionSite site,
            ExpeditionSiteBlockPlan plan,
            SiteSummary summary,
            ResourceLocation biomeId,
            int connectedBiomeChunks,
            BiomeMineResourceProfile resourceProfile,
            PlanSignature signature,
            IoeMotherDepositReservation motherDepositReservation,
            IoePetroleumReservoirReservation petroleumReservoirReservation,
            List<ExpeditionSiteBlockPlan> fallbackPlans,
            long sequence,
            long createdNanos
    ) {
        private PendingSite {
            Objects.requireNonNull(site, "site");
            Objects.requireNonNull(plan, "plan");
            Objects.requireNonNull(summary, "summary");
            Objects.requireNonNull(resourceProfile, "resourceProfile");
            Objects.requireNonNull(signature, "signature");
            fallbackPlans = List.copyOf(Objects.requireNonNull(fallbackPlans, "fallbackPlans"));
        }
    }

    private record FallbackApplication(
            PendingSite site,
            IoeExpeditionPlanPlacement.AppliedPlan placement
    ) {
        private FallbackApplication {
            Objects.requireNonNull(site, "site");
            Objects.requireNonNull(placement, "placement");
        }
    }

    record ExcavatorRegion(
            BlockPos anchorPos,
            SiteQuality quality,
            ResourceLocation provinceId,
            BiomeMineResourceProfile resourceProfile,
            boolean motherDepositReserved
    ) {
        ExcavatorRegion {
            Objects.requireNonNull(anchorPos, "anchorPos");
            Objects.requireNonNull(quality, "quality");
            Objects.requireNonNull(provinceId, "provinceId");
            Objects.requireNonNull(resourceProfile, "resourceProfile");
        }

    }

    private record SiteSummary(
            ResourceLocation requestedFeatureId,
            BlockPos chamberCenter,
            ResourceLocation oreBlockId,
            ResourceLocation oreNodeHeartBlockId,
            int oreNodeCount,
            long oreNodeHeartCount,
            long oreBlockCount,
            int roomCount,
            int blockCount
    ) {
        private static SiteSummary from(ExpeditionSiteBlockPlan plan) {
            return new SiteSummary(
                    plan.requestedFeatureId(),
                    plan.chamberCenter(),
                    plan.oreBlockId(),
                    plan.oreNodeHeartBlockId(),
                    plan.oreNodeCount(),
                    plan.oreNodeHeartCount(),
                    plan.oreBlockCount(),
                    plan.roomCenters().size(),
                    plan.blocks().size()
            );
        }
    }

    private static final class PlanSignature {
        private final long[] resourcePositions;

        private PlanSignature(long[] resourcePositions) {
            this.resourcePositions = resourcePositions;
        }

        private static PlanSignature from(
                ExpeditionSiteBlockPlan plan,
                ChunkPos anchorChunk,
                IoeMotherDepositReservation motherDepositReservation
        ) {
            int size = plan.blocks().size();
            long[] resources = new long[size];
            int structureBlockCount = 0;
            int resourceCount = 0;
            for (Map.Entry<BlockPos, BlockState> placement : plan.blocks().entrySet()) {
                BlockPos pos = placement.getKey();
                if (!new ChunkPos(pos).equals(anchorChunk)) {
                    throw new IllegalStateException("Connected expedition plans must remain inside the anchor chunk");
                }
                BlockState expectedState = placement.getValue();
                // Later worldgen steps may legitimately decorate carved air. Durability is proven by the
                // structure and resource blocks, not by requiring every tunnel air cell to remain untouched.
                if (!expectedState.isAir()) {
                    structureBlockCount++;
                }
                if (IoeNewChunkOreGuard.isCandidate(expectedState)) {
                    resources[resourceCount++] = pos.asLong();
                }
            }
            boolean depositBackedStructure = motherDepositReservation != null
                    && motherDepositReservation.requiredForSiteQuality();
            if (structureBlockCount == 0
                    || plan.quality().isProductive() && resourceCount == 0 && !depositBackedStructure) {
                throw new IllegalStateException(
                        "Productive connected plans require structure blocks and either embedded resources or an IE deposit"
                );
            }
            return new PlanSignature(Arrays.copyOf(resources, resourceCount));
        }

        private void appendResourcePositions(List<BlockPos> target) {
            for (long resourcePosition : resourcePositions) {
                target.add(BlockPos.of(resourcePosition));
            }
        }

        private int resourcePositionCount() {
            return resourcePositions.length;
        }
    }
}
