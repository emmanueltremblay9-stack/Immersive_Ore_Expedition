package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SubLavaGeodeGenerator {
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

        if (!Level.NETHER.equals(anchor.dimension())
                || !new GiantLavaLakeDetector().isValidAnchor(sample)
                || !isDepthAllowed(blocksBelowLava)
                || IoeNetherGeodesConfig.allowRandomNetherGeodes()) {
            return Optional.empty();
        }

        ResourcePolicyDecision quartzDecision = policyService.evaluate(quartzResource, scanner);
        if (!quartzDecision.shouldUse()) {
            return Optional.empty();
        }

        Optional<ResourceRef> heart = Optional.empty();
        if (IoeNetherGeodesConfig.ancientDebrisExtremeRare() && ancientDebrisResource.isPresent()) {
            ResourcePolicyDecision debrisDecision = policyService.evaluate(ancientDebrisResource.get(), scanner);
            if (debrisDecision.shouldUse()) {
                heart = ancientDebrisResource;
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
                List.of(),
                List.of()
        ));
    }

    private static boolean isDepthAllowed(int blocksBelowLava) {
        return blocksBelowLava >= IoeNetherGeodesConfig.minBlocksBelowLava()
                && blocksBelowLava <= IoeNetherGeodesConfig.maxBlocksBelowLava();
    }
}
