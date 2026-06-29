package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SubLavaGeodeGenerator {
    private static final ResourceRef NETHER_QUARTZ_ORE = ResourceRef.block("minecraft", "nether_quartz_ore");
    private static final ResourceRef ANCIENT_DEBRIS = ResourceRef.block("minecraft", "ancient_debris");

    public boolean generateBelowLake(WorldGenLevel level, BlockPos lavaAnchor) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(lavaAnchor, "lavaAnchor");
        IoeNetherGeodesMod.LOGGER.debug("Skipping direct Nether geode placement at {}; alpha planning is enabled but configured-feature placement is not registered.", lavaAnchor);
        return false;
    }

    public Optional<SubLavaGeodePlan> planBelowLake(
            ExpeditionAnchorRef anchor,
            LavaLakeAnchorSample sample,
            ResourceRef quartzResource,
            Optional<ResourceRef> ancientDebrisResource,
            int blocksBelowLava,
            LoadedResourceScanner scanner,
            ResourcePolicyService policyService
    ) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(sample, "sample");
        Objects.requireNonNull(quartzResource, "quartzResource");
        Objects.requireNonNull(ancientDebrisResource, "ancientDebrisResource");
        Objects.requireNonNull(scanner, "scanner");
        Objects.requireNonNull(policyService, "policyService");

        if (!canPlanAnchoredGeode(
                anchor.dimension(),
                new GiantLavaLakeDetector().isValidAnchor(sample),
                isDepthAllowed(blocksBelowLava),
                IoeNetherGeodesConfig.allowRandomNetherGeodes()
        )) {
            return Optional.empty();
        }

        ResourcePolicyDecision quartzDecision = policyService.evaluate(quartzResource, scanner);
        if (!canUseQuartzResource(quartzResource, quartzDecision, IoeNetherGeodesConfig.netherQuartz())) {
            return Optional.empty();
        }

        List<ResourcePolicyDecision> skipped = new ArrayList<>();
        List<ResourcePolicyDecision> rejected = new ArrayList<>();
        Optional<ResourceRef> heart = Optional.empty();
        if (IoeNetherGeodesConfig.ancientDebrisExtremeRare() && ancientDebrisResource.isPresent()) {
            ResourceRef candidateHeart = ancientDebrisResource.get();
            if (isAncientDebrisResource(candidateHeart)) {
                ResourcePolicyDecision debrisDecision = policyService.evaluate(candidateHeart, scanner);
                if (debrisDecision.shouldUse()) {
                    heart = ancientDebrisResource;
                } else if (debrisDecision.shouldSkip()) {
                    skipped.add(debrisDecision);
                } else {
                    rejected.add(debrisDecision);
                }
            } else {
                rejected.add(ResourcePolicyDecision.reject("Ancient Debris hearts require minecraft:ancient_debris: " + candidateHeart.id()));
            }
        }

        return Optional.of(new SubLavaGeodePlan(
                anchor,
                sample,
                quartzResource,
                heart,
                blocksBelowLava,
                IoeNetherGeodesConfig.requireSafeCrust(),
                false,
                IoeNetherGeodesConfig.ancientDebrisMotherlodeChance(),
                skipped,
                rejected
        ));
    }

    private static boolean isDepthAllowed(int blocksBelowLava) {
        return blocksBelowLava >= IoeNetherGeodesConfig.minBlocksBelowLava()
                && blocksBelowLava <= IoeNetherGeodesConfig.maxBlocksBelowLava();
    }

    static boolean canPlanAnchoredGeode(
            net.minecraft.resources.ResourceKey<Level> anchorDimension,
            boolean validLavaLakeAnchor,
            boolean depthAllowed,
            boolean allowRandomNetherGeodes
    ) {
        // Random geode allowance applies only to unanchored generation; this alpha path is anchored-only.
        return Level.NETHER.equals(anchorDimension) && validLavaLakeAnchor && depthAllowed;
    }

    static boolean canUseQuartzResource(ResourceRef quartzResource, ResourcePolicyDecision quartzDecision, boolean netherQuartzEnabled) {
        Objects.requireNonNull(quartzResource, "quartzResource");
        Objects.requireNonNull(quartzDecision, "quartzDecision");
        return netherQuartzEnabled && NETHER_QUARTZ_ORE.equals(quartzResource) && quartzDecision.shouldUse();
    }

    private static boolean isAncientDebrisResource(ResourceRef resource) {
        return ANCIENT_DEBRIS.equals(resource);
    }
}
