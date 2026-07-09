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

    public synchronized void record(ExpeditionSite site) {
        Objects.requireNonNull(site, "site");
        sites.put(SiteKey.from(site), site);
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
