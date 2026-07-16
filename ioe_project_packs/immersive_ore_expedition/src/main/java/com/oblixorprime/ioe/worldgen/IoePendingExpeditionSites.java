package com.oblixorprime.ioe.worldgen;

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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return stage(worldGenLevel, plan, resourceProfile, null, null);
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation
    ) {
        return stage(worldGenLevel, plan, resourceProfile, motherDepositReservation, null);
    }

    static boolean stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile,
            IoeMotherDepositReservation motherDepositReservation,
            ExpeditionSiteBlockPlan fallbackPlan
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
                fallbackPlan,
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
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");
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
            IoeExpeditionPlanPlacement.AppliedPlan effectivePlacement =
                    IoeExpeditionPlanPlacement.apply(level, pendingSite.plan()).orElse(null);
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
            boolean depositCommitted = false;
            IoeMotherDepositReservation reservation = pendingSite.motherDepositReservation();
            if (reservation != null) {
                try {
                    reservation.commit();
                    depositCommitted = true;
                } catch (RuntimeException | LinkageError failure) {
                    rollbackReservationBestEffort(pendingSite, "commit failure");
                    IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                            IoeWorldgenRuntimeDiagnostics.SiteSkipReason.IE_DEPOSIT_MISSING
                    );
                    IoeExpeditionWorldgenMod.LOGGER.error(
                            "Failed to commit the IE deposit at {}; applying the direct lower quality pipeline",
                            pendingSite.site().pos(),
                            failure
                    );
                    FallbackApplication fallback = applyDirectLowerFallback(
                            level,
                            pendingSite,
                            effectivePlacement,
                            chunkPos
                    );
                    if (fallback == null) {
                        rejectedSites++;
                        pendingSite.signature().appendResourcePositions(rejectedResourcePositions);
                        continue;
                    }
                    effectiveSite = fallback.site();
                    effectivePlacement = fallback.placement();
                }
            }

            try {
                ExpeditionLocatorService.record(level, effectiveSite.site());
            } catch (RuntimeException | LinkageError failure) {
                rejectedSites++;
                if (depositCommitted) {
                    rollbackReservationBestEffort(pendingSite, "locator failure after commit");
                }
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

    private static FallbackApplication applyDirectLowerFallback(
            ServerLevel level,
            PendingSite pendingSite,
            IoeExpeditionPlanPlacement.AppliedPlan initialPlacement,
            ChunkPos chunkPos
    ) {
        ExpeditionSiteBlockPlan fallbackPlan = pendingSite.fallbackPlan();
        if (fallbackPlan == null) {
            rollbackPlacementBestEffort(level, initialPlacement, pendingSite, "missing lower quality fallback");
            return null;
        }
        SiteQuality currentQuality = pendingSite.site().quality().orElseThrow();
        SiteQuality expectedLower = currentQuality.directLower().orElse(null);
        if (expectedLower == null || fallbackPlan.quality() != expectedLower) {
            rollbackPlacementBestEffort(level, initialPlacement, pendingSite, "invalid lower quality fallback");
            return null;
        }
        if (!rollbackPlacementBestEffort(
                level,
                initialPlacement,
                pendingSite,
                "direct quality downgrade"
        )) {
            return null;
        }
        IoeExpeditionPlanPlacement.AppliedPlan fallbackPlacement =
                IoeExpeditionPlanPlacement.apply(level, fallbackPlan).orElse(null);
        if (fallbackPlacement == null) {
            return null;
        }
        PendingSite fallbackSite;
        try {
            fallbackSite = pendingSite(
                    level,
                    fallbackPlan,
                    pendingSite.resourceProfile(),
                    null,
                    null,
                    chunkPos
            );
        } catch (RuntimeException | LinkageError failure) {
            fallbackPlacement.rollback(level);
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to stage the direct lower quality plan at {}",
                    pendingSite.site().pos(),
                    failure
            );
            return null;
        }
        IoeExpeditionWorldgenMod.LOGGER.warn(
                "Downgraded IOE site after IE deposit commit failure: anchor={} {} -> {}",
                pendingSite.site().pos(),
                currentQuality,
                fallbackPlan.quality()
        );
        return new FallbackApplication(fallbackSite, fallbackPlacement);
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
            ExpeditionSiteBlockPlan fallbackPlan,
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
                PlanSignature.from(plan, chunkPos),
                motherDepositReservation,
                fallbackPlan,
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
        IoeMotherDepositReservation reservation = pendingSite.motherDepositReservation();
        if (reservation == null) {
            return;
        }
        try {
            reservation.rollback();
        } catch (RuntimeException | LinkageError failure) {
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to roll back IE deposit reservation at {} reason={}",
                    pendingSite.site().pos(),
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
                "Generated connected IOE expedition site type={} anchor={} chamber={} biome={} connectedBiomeChunks={} quality={} ore={} oreHeart={} oreNodes={} oreHearts={} oreBlocks={} blocks={}",
                summary.requestedFeatureId(),
                pendingSite.site().pos(),
                summary.chamberCenter(),
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
            ExpeditionSiteBlockPlan fallbackPlan,
            long sequence,
            long createdNanos
    ) {
        private PendingSite {
            Objects.requireNonNull(site, "site");
            Objects.requireNonNull(plan, "plan");
            Objects.requireNonNull(summary, "summary");
            Objects.requireNonNull(resourceProfile, "resourceProfile");
            Objects.requireNonNull(signature, "signature");
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
                    plan.blocks().size()
            );
        }
    }

    private static final class PlanSignature {
        private final long[] resourcePositions;

        private PlanSignature(long[] resourcePositions) {
            this.resourcePositions = resourcePositions;
        }

        private static PlanSignature from(ExpeditionSiteBlockPlan plan, ChunkPos anchorChunk) {
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
            if (structureBlockCount == 0 || plan.quality().isProductive() && resourceCount == 0) {
                throw new IllegalStateException(
                        "Connected expedition plans require structure blocks and productive plans require resources"
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
