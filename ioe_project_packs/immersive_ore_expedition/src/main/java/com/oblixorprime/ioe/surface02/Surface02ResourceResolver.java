package com.oblixorprime.ioe.surface02;

import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.BuddingRankResolution;
import com.oblixorprime.ioe.budding.BuddingRankResolver;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * The only Surface 02 bridge to the compiled Budding foundation.
 *
 * <p>The production entry point always delegates to {@link BuddingRankResolver}; the overload is a
 * package-private unit-test seam for exercising composer behavior without booting an FML runtime.</p>
 */
public final class Surface02ResourceResolver {
    private Surface02ResourceResolver() {
    }

    public static Resolution resolve(BuddingResourceFamily family, BuddingRank rank) {
        return resolve(
                family,
                rank,
                (requestedFamily, requestedRank) -> FoundationResult.from(
                        BuddingRankResolver.resolve(requestedFamily, requestedRank)
                )
        );
    }

    static Resolution resolve(
            BuddingResourceFamily family,
            BuddingRank rank,
            BiFunction<BuddingResourceFamily, BuddingRank, FoundationResult> foundationResolver
    ) {
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(foundationResolver, "foundationResolver");

        FoundationResult foundation = foundationResolver.apply(family, rank);
        if (!foundation.supported()) {
            return Resolution.unsupported(family, rank, foundation.detail());
        }

        ResourceLocation buddingId = foundation.buddingBlockId().orElseThrow();
        if (family == BuddingResourceFamily.CERTUS_QUARTZ) {
            ResourceLocation requiredNativeId = ResourceLocation.fromNamespaceAndPath(
                    "ae2",
                    rank.path() + "_budding_quartz"
            );
            if (!requiredNativeId.equals(buddingId)) {
                return Resolution.unsupported(
                        family,
                        rank,
                        "Certus rank did not resolve to the exact native AE2 block: " + requiredNativeId
                );
            }
        }
        return Resolution.supported(new ResolvedResource(
                family,
                rank,
                buddingId,
                foundation.mineralBlockId()
        ));
    }

    record FoundationResult(
            boolean supported,
            Optional<ResourceLocation> buddingBlockId,
            ResourceLocation mineralBlockId,
            String detail
    ) {
        FoundationResult {
            Objects.requireNonNull(buddingBlockId, "buddingBlockId");
            Objects.requireNonNull(mineralBlockId, "mineralBlockId");
            Objects.requireNonNull(detail, "detail");
            if (supported != buddingBlockId.isPresent()) {
                throw new IllegalArgumentException("Supported foundation results require one Budding block id");
            }
        }

        static FoundationResult from(BuddingRankResolution resolution) {
            Objects.requireNonNull(resolution, "resolution");
            return new FoundationResult(
                    resolution.supported(),
                    resolution.supported() ? resolution.buddingBlockId() : Optional.empty(),
                    resolution.storageBlockId(),
                    resolution.detail()
            );
        }
    }

    public record ResolvedResource(
            BuddingResourceFamily family,
            BuddingRank rank,
            ResourceLocation buddingBlockId,
            ResourceLocation mineralBlockId
    ) {
        public ResolvedResource {
            Objects.requireNonNull(family, "family");
            Objects.requireNonNull(rank, "rank");
            Objects.requireNonNull(buddingBlockId, "buddingBlockId");
            Objects.requireNonNull(mineralBlockId, "mineralBlockId");
            if (family.kind() == BuddingResourceFamily.Kind.EXTENDED_AE) {
                throw new IllegalArgumentException("ExtendedAE cannot be a supported Surface 02 resource");
            }
            if (!mineralBlockId.equals(family.storageBlockId())) {
                throw new IllegalArgumentException("Surface 02 mineral id must come from the resolved family");
            }
            ResourceLocation expectedBuddingId = switch (family.kind()) {
                case GEORE -> ResourceLocation.fromNamespaceAndPath(
                        "immersive_ore_expedition",
                        rank.path() + "_budding_" + family.key()
                );
                case CERTUS -> family.observedNativeBuddingId(rank).orElseThrow();
                case EXTENDED_AE -> throw new IllegalStateException("ExtendedAE is rejected above");
            };
            if (!buddingBlockId.equals(expectedBuddingId)) {
                throw new IllegalArgumentException(
                        "Surface 02 Budding id must be the exact canonical family/rank id: " + expectedBuddingId
                );
            }
        }
    }

    public record Resolution(
            Status status,
            Optional<ResolvedResource> resource,
            BuddingResourceFamily requestedFamily,
            BuddingRank requestedRank,
            String detail
    ) {
        public Resolution {
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(requestedFamily, "requestedFamily");
            Objects.requireNonNull(requestedRank, "requestedRank");
            Objects.requireNonNull(detail, "detail");
            if (resource.isPresent() != (status == Status.SUPPORTED)) {
                throw new IllegalArgumentException("Supported Surface 02 resources require exactly one result");
            }
            if (resource.isPresent()
                    && (resource.orElseThrow().family() != requestedFamily
                    || resource.orElseThrow().rank() != requestedRank)) {
                throw new IllegalArgumentException(
                        "Surface 02 resource result must match its requested family and rank"
                );
            }
        }

        private static Resolution supported(ResolvedResource resource) {
            return new Resolution(
                    Status.SUPPORTED,
                    Optional.of(resource),
                    resource.family(),
                    resource.rank(),
                    "Exact Budding foundation rank resolved"
            );
        }

        private static Resolution unsupported(
                BuddingResourceFamily family,
                BuddingRank rank,
                String detail
        ) {
            return new Resolution(Status.RANK_UNSUPPORTED, Optional.empty(), family, rank, detail);
        }
    }

    public enum Status {
        SUPPORTED,
        RANK_UNSUPPORTED
    }
}
