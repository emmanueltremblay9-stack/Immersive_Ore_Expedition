package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.worldgen.ExpeditionAnchorPlacementPlan;
import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ExpeditionLocatorIndex {
    public static final String RUNTIME_PLACEMENT_PROOF_SOURCE = "runtime_worldgen_placement_proof";

    private final LinkedHashMap<SiteKey, ExpeditionSite> sites = new LinkedHashMap<>();
    private final java.util.Map<NodeKey, NodeContext> nodes = new java.util.HashMap<>();

    private final java.util.Map<NodeKey, ExpeditionSite> naturalAnchors = new java.util.HashMap<>();
    private final java.util.Map<ChunkKey, LinkedHashMap<NodeKey, ExpeditionSite>> discoveryChunks = new java.util.HashMap<>();
    private record ChunkKey(ResourceKey<Level> dimension, int x, int z) { }

    public record NodeContext(ExpeditionSite site, com.oblixorprime.ioe.budding.BuddingNodeInfo node) { }
    private record NodeKey(ResourceKey<Level> dimension, BlockPos pos) { }

    public synchronized Optional<NodeContext> buddingNodeAt(ResourceKey<Level> dimension, BlockPos pos) {
        return Optional.ofNullable(nodes.get(new NodeKey(dimension, pos)));
    }

    public synchronized boolean removeBuddingNode(ResourceKey<Level> dimension, BlockPos pos) {
        NodeContext previous = nodes.get(new NodeKey(dimension, pos));
        if (previous == null) return false;
        record(previous.site().withBuddingNodes(previous.site().buddingNodes().stream()
                .filter(node -> !node.pos().equals(pos)).toList()));
        return true;
    }

    public synchronized void record(ExpeditionSite site) {
        Objects.requireNonNull(site, "site");
        if (naturalDiscoveryAnchor(site)) {
            NodeKey key = new NodeKey(site.dimension(), site.pos());
            naturalAnchors.put(key, site);
            discoveryChunks.computeIfAbsent(new ChunkKey(site.dimension(), site.pos().getX() >> 4,
                    site.pos().getZ() >> 4), ignored -> new LinkedHashMap<>()).put(key, site);
        }
        ExpeditionSite previous = sites.put(SiteKey.from(site), site);
        if (previous != null) previous.buddingNodes().forEach(node -> nodes.remove(new NodeKey(previous.dimension(), node.pos()), new NodeContext(previous, node)));
        if (site.playable() && site.quality().isPresent()) {
            site.buddingNodes().forEach(node -> nodes.put(new NodeKey(site.dimension(), node.pos()), new NodeContext(site, node)));
        }
    }

    public synchronized void recordPlacedProof(
            ResourceKey<Level> dimension,
            RuntimeWorldgenPlacementProofResult result
    ) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(result, "result");
        if (!result.blockPlaced()
                || result.anchorType() == null
                || result.origin() == null
                || result.siteQuality() == null) {
            return;
        }

        ExpeditionAnchorPlacementPlan anchorPlan = result.anchorPlan().orElse(null);
        ResourceLocation provinceId = anchorPlan == null ? null : anchorPlan.provinceId().orElse(null);
        ExpeditionSite anchorSite = ExpeditionSite.anchor(
                dimension,
                result.origin(),
                result.anchorType(),
                provinceId,
                result.siteQuality(),
                RUNTIME_PLACEMENT_PROOF_SOURCE,
                ExpeditionSitePlacementState.PROVEN,
                null
        );
        record(anchorSite);

        if (provinceId != null) {
            record(ExpeditionSite.province(
                    dimension,
                    result.origin(),
                    result.anchorType(),
                    provinceId,
                    result.siteQuality(),
                    RUNTIME_PLACEMENT_PROOF_SOURCE,
                    ExpeditionSitePlacementState.PROVEN,
                    null
            ));
        }
    }

    public synchronized ExpeditionLocatorResult nearestAny(ResourceKey<Level> dimension, BlockPos origin) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(origin, "origin");
        return nearestFrom(dimension, origin, sites.values());
    }

    public synchronized ExpeditionLocatorResult nearest(
            ResourceKey<Level> dimension,
            BlockPos origin,
            ExpeditionSiteKind kind
    ) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(origin, "origin");
        List<ExpeditionSite> candidates = sites.values().stream()
                .filter(ExpeditionSite::playable)
                .filter(site -> site.kind() == kind)
                .toList();
        return nearestFrom(dimension, origin, candidates);
    }

    public synchronized boolean hasPlayableAnchorWithinHorizontalDistance(
            ResourceKey<Level> dimension,
            BlockPos origin,
            int minimumDistanceBlocks
    ) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(origin, "origin");
        if (minimumDistanceBlocks <= 0) {
            throw new IllegalArgumentException("Minimum anchor distance must be positive");
        }
        long minimumDistanceSquared = (long) minimumDistanceBlocks * minimumDistanceBlocks;
        return sites.values().stream()
                .filter(ExpeditionSite::playable)
                .filter(site -> site.kind() == ExpeditionSiteKind.ANCHOR)
                .filter(site -> site.dimension().equals(dimension))
                .anyMatch(site -> {
                    long dx = (long) origin.getX() - site.pos().getX();
                    long dz = (long) origin.getZ() - site.pos().getZ();
                    return Math.abs(dx) < minimumDistanceBlocks
                            && Math.abs(dz) < minimumDistanceBlocks
                            && dx * dx + dz * dz < minimumDistanceSquared;
                });
    }

    private static boolean naturalDiscoveryAnchor(ExpeditionSite site) {
        return site.kind() == ExpeditionSiteKind.ANCHOR && site.playable()
                && site.source().filter("natural_connected_expedition_site"::equals).isPresent();
    }

    public synchronized boolean hasNaturalDiscoveryAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        return naturalAnchors.containsKey(new NodeKey(dimension, pos));
    }

    /** Fixed nine buckets, at most sixteen anchors per bucket; no global index copy or terrain access. */
    public synchronized List<ExpeditionSite> nearbyDiscoveryAnchors(ResourceKey<Level> dimension, BlockPos pos) {
        var result = new java.util.ArrayList<ExpeditionSite>();
        int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
        for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) {
            var bucket = discoveryChunks.get(new ChunkKey(dimension, x, z));
            if (bucket == null) continue;
            int inspected = 0;
            for (ExpeditionSite site : bucket.values()) {
                if (inspected++ == 16) break;
                result.add(site);
            }
        }
        return List.copyOf(result);
    }

    public synchronized List<ExpeditionSite> sites() {
        return gameplaySites();
    }

    public synchronized List<ExpeditionSite> diagnosticSites() {
        return List.copyOf(sites.values());
    }

    public synchronized int size() {
        return gameplaySites().size();
    }

    public synchronized void clear() {
        sites.clear();
        nodes.clear();
        naturalAnchors.clear();
        discoveryChunks.clear();
    }

    public static long distanceSquared(BlockPos first, BlockPos second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        long dx = (long) first.getX() - second.getX();
        long dy = (long) first.getY() - second.getY();
        long dz = (long) first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static ExpeditionLocatorResult nearestFrom(
            ResourceKey<Level> dimension,
            BlockPos origin,
            Collection<ExpeditionSite> candidates
    ) {
        return candidates.stream()
                .filter(ExpeditionSite::playable)
                .filter(site -> site.dimension().equals(dimension))
                .min(nearestComparator(origin))
                .map(site -> ExpeditionLocatorResult.found(site, distanceSquared(origin, site.pos())))
                .orElseGet(ExpeditionLocatorResult::noIndexedSites);
    }

    private List<ExpeditionSite> gameplaySites() {
        return sites.values().stream()
                .filter(ExpeditionSite::playable)
                .toList();
    }

    private static Comparator<ExpeditionSite> nearestComparator(BlockPos origin) {
        return Comparator
                .comparingLong((ExpeditionSite site) -> distanceSquared(origin, site.pos()))
                .thenComparing(site -> site.dimension().location().toString())
                .thenComparingInt(site -> site.pos().getX())
                .thenComparingInt(site -> site.pos().getY())
                .thenComparingInt(site -> site.pos().getZ())
                .thenComparing(site -> site.kind().name())
                .thenComparing(site -> locationKey(site.anchorId()))
                .thenComparing(site -> locationKey(site.provinceId()))
                .thenComparing(site -> site.quality().map(Enum::name).orElse(""))
                .thenComparing(site -> site.source().orElse(""));
    }

    private static String locationKey(Optional<ResourceLocation> id) {
        return id.map(ResourceLocation::toString).orElse("");
    }

    private record SiteKey(
            ResourceKey<Level> dimension,
            BlockPos pos,
            ExpeditionSiteKind kind,
            Optional<ResourceLocation> anchorId,
            Optional<ResourceLocation> provinceId,
            Optional<String> source,
            ExpeditionSitePlacementState placementState,
            Optional<String> placementReason
    ) {
        private static SiteKey from(ExpeditionSite site) {
            return new SiteKey(
                    site.dimension(),
                    site.pos(),
                    site.kind(),
                    site.anchorId(),
                    site.provinceId(),
                    site.source(),
                    site.placementState(),
                    site.placementReason()
            );
        }
    }
}
