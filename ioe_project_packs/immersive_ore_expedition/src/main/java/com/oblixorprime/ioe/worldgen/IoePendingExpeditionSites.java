package com.oblixorprime.ioe.worldgen;

import com.oblixorprime.ioe.compat.ie.IoeExcavatorMotherDepositBridge;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;
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
 * Defers locator entries until the new-chunk guard has sanitized and verified the complete mine plan.
 * Worldgen can run in temporary dependency chunks that never become durable; those plans must never become
 * compass targets or retain unbounded authorization state.
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

    static void stage(
            WorldGenLevel worldGenLevel,
            ExpeditionSiteBlockPlan plan,
            BiomeMineResourceProfile resourceProfile
    ) {
        Objects.requireNonNull(worldGenLevel, "worldGenLevel");
        ServerLevel level = Objects.requireNonNull(worldGenLevel.getLevel(), "level");
        Objects.requireNonNull(plan, "plan");
        ChunkPos chunkPos = new ChunkPos(plan.anchorPos());
        PendingSite pendingSite = pendingSite(level, plan, resourceProfile, chunkPos);
        ConcurrentHashMap<Long, List<PendingSite>> byChunk = PENDING.computeIfAbsent(
                level.dimension(),
                ignored -> new ConcurrentHashMap<>()
        );
        byChunk.compute(chunkPos.toLong(), (ignored, existing) -> appendDistinct(existing, pendingSite));
        level.getServer().execute(() -> prune(level));
    }

    /**
     * Runs only from the new-chunk guard after its final sanitization pass. Rejected resource positions are
     * returned so the guard can revoke their authorization and remove any surviving ore or budding blocks.
     */
    static Confirmation confirmLoadedChunk(ServerLevel level, ChunkPos chunkPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) {
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
            if (!pendingSite.signature().matches(chunk)) {
                rejectedSites++;
                pendingSite.signature().appendResourcePositions(rejectedResourcePositions);
                IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                        IoeWorldgenRuntimeDiagnostics.SiteSkipReason.UNSAFE_WRITE
                );
                IoeExpeditionWorldgenMod.LOGGER.warn(
                        "Discarded non-durable IOE expedition locator entry type={} anchor={} chunk={}",
                        pendingSite.summary().requestedFeatureId(),
                        pendingSite.site().pos(),
                        chunkPos
                );
                continue;
            }
            if (!guaranteeMotherDepositIfApplicable(level, pendingSite)) {
                rejectedSites++;
                pendingSite.signature().appendResourcePositions(rejectedResourcePositions);
                IoeWorldgenRuntimeDiagnostics.recordSiteSkip(
                        IoeWorldgenRuntimeDiagnostics.SiteSkipReason.IE_DEPOSIT_MISSING
                );
                IoeExpeditionWorldgenMod.LOGGER.error(
                        "Discarded IOE Mother Node without a guaranteed IE Excavator deposit anchor={} chunk={}",
                        pendingSite.site().pos(),
                        chunkPos
                );
                continue;
            }
            ExpeditionLocatorService.record(level, pendingSite.site());
            IoeWorldgenRuntimeDiagnostics.recordSitePlaced();
            confirmedSites++;
            logConfirmedSite(pendingSite);
        }
        return new Confirmation(confirmedSites, rejectedSites, List.copyOf(rejectedResourcePositions));
    }

    private static boolean guaranteeMotherDepositIfApplicable(ServerLevel level, PendingSite pendingSite) {
        SiteQuality quality = pendingSite.site().quality().orElse(null);
        ResourceLocation provinceId = pendingSite.site().provinceId().orElse(null);
        if (quality == null || provinceId == null) {
            return true;
        }
        var request = IoeExcavatorDepositRules.guaranteedMotherRequest(
                pendingSite.site().pos(),
                quality,
                provinceId,
                pendingSite.resourceProfile()
        );
        if (request.isEmpty()) {
            return true;
        }
        if (!ModList.get().isLoaded("immersiveengineering")) {
            IoeExcavatorDepositRules.recordGuaranteedMotherIeAbsent();
            return true;
        }
        try {
            return IoeExcavatorMotherDepositBridge.ensureGuaranteedDeposit(level, request.orElseThrow());
        } catch (RuntimeException | LinkageError failure) {
            IoeExcavatorDepositRules.recordGuaranteedMotherFailed();
            IoeExpeditionWorldgenMod.LOGGER.error(
                    "Failed to guarantee the Immersive Engineering deposit for Mother Node {} at {} in {}; rejecting the site",
                    pendingSite.site().siteId(),
                    pendingSite.site().pos(),
                    level.dimension().location(),
                    failure
            );
            return false;
        }
    }

    static void clear() {
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
                PENDING.remove(dimension);
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
                SiteSummary.from(plan),
                biomeId,
                connectedBiomeChunks,
                resourceProfile,
                PlanSignature.from(plan, chunkPos),
                NEXT_SEQUENCE.incrementAndGet(),
                System.nanoTime()
        );
    }

    private static List<PendingSite> appendDistinct(List<PendingSite> existing, PendingSite pendingSite) {
        if (existing == null) {
            return List.of(pendingSite);
        }
        if (existing.stream().anyMatch(candidate -> candidate.site().pos().equals(pendingSite.site().pos()))) {
            return existing;
        }
        ArrayList<PendingSite> updated = new ArrayList<>(existing);
        updated.add(pendingSite);
        return List.copyOf(updated);
    }

    private static ExcavatorRegion toExcavatorRegion(PendingSite pendingSite) {
        return new ExcavatorRegion(
                pendingSite.site().pos(),
                pendingSite.site().quality().orElseThrow(),
                pendingSite.site().provinceId().orElseThrow(),
                pendingSite.resourceProfile()
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
            IoeOrePlacementAuthorization.releaseChunk(level.dimension(), new ChunkPos(oldest.getKey()));
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
            SiteSummary summary,
            ResourceLocation biomeId,
            int connectedBiomeChunks,
            BiomeMineResourceProfile resourceProfile,
            PlanSignature signature,
            long sequence,
            long createdNanos
    ) {
        private PendingSite {
            Objects.requireNonNull(site, "site");
            Objects.requireNonNull(summary, "summary");
            Objects.requireNonNull(resourceProfile, "resourceProfile");
            Objects.requireNonNull(signature, "signature");
        }
    }

    record ExcavatorRegion(
            BlockPos anchorPos,
            SiteQuality quality,
            ResourceLocation provinceId,
            BiomeMineResourceProfile resourceProfile
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
        private final long[] positions;
        private final Block[] expectedBlocks;
        private final long[] resourcePositions;

        private PlanSignature(long[] positions, Block[] expectedBlocks, long[] resourcePositions) {
            this.positions = positions;
            this.expectedBlocks = expectedBlocks;
            this.resourcePositions = resourcePositions;
        }

        private static PlanSignature from(ExpeditionSiteBlockPlan plan, ChunkPos anchorChunk) {
            int size = plan.blocks().size();
            long[] positions = new long[size];
            Block[] expectedBlocks = new Block[size];
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
                    positions[structureBlockCount] = pos.asLong();
                    expectedBlocks[structureBlockCount] = expectedState.getBlock();
                    structureBlockCount++;
                }
                if (IoeNewChunkOreGuard.isCandidate(expectedState)) {
                    resources[resourceCount++] = pos.asLong();
                }
            }
            if (structureBlockCount == 0 || resourceCount == 0) {
                throw new IllegalStateException("Connected expedition plans require structure and resource blocks");
            }
            return new PlanSignature(
                    Arrays.copyOf(positions, structureBlockCount),
                    Arrays.copyOf(expectedBlocks, structureBlockCount),
                    Arrays.copyOf(resources, resourceCount)
            );
        }

        private boolean matches(LevelChunk chunk) {
            for (int index = 0; index < positions.length; index++) {
                if (chunk.getBlockState(BlockPos.of(positions[index])).getBlock() != expectedBlocks[index]) {
                    return false;
                }
            }
            return true;
        }

        private void appendResourcePositions(List<BlockPos> target) {
            for (long resourcePosition : resourcePositions) {
                target.add(BlockPos.of(resourcePosition));
            }
        }
    }
}
