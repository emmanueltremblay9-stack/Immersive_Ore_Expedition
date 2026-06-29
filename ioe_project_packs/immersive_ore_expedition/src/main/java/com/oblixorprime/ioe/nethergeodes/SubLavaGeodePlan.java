package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SubLavaGeodePlan(
        ExpeditionAnchorRef anchor,
        LavaLakeAnchorSample lavaLake,
        ResourceRef quartzResource,
        Optional<ResourceRef> ancientDebrisHeart,
        int blocksBelowLava,
        boolean requireSafeCrust,
        boolean randomNetherGeode,
        double ancientDebrisMotherlodeChance,
        List<ResourcePolicyDecision> skippedResources,
        List<ResourcePolicyDecision> rejectedResources
) {
    private static final ResourceRef NETHER_QUARTZ_ORE = ResourceRef.block("minecraft", "nether_quartz_ore");
    private static final ResourceRef ANCIENT_DEBRIS = ResourceRef.block("minecraft", "ancient_debris");

    public SubLavaGeodePlan {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(lavaLake, "lavaLake");
        Objects.requireNonNull(quartzResource, "quartzResource");
        ancientDebrisHeart = Objects.requireNonNull(ancientDebrisHeart, "ancientDebrisHeart");
        skippedResources = List.copyOf(Objects.requireNonNull(skippedResources, "skippedResources"));
        rejectedResources = List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
        if (!Level.NETHER.equals(anchor.dimension()) || !Level.NETHER.equals(lavaLake.dimension())) {
            throw new IllegalArgumentException("Sub-lava geode plans must stay in the Nether");
        }
        if (!new GiantLavaLakeDetector().isValidAnchor(lavaLake)) {
            throw new IllegalArgumentException("Sub-lava geode plans require a valid giant Nether lava lake");
        }
        if (!NETHER_QUARTZ_ORE.equals(quartzResource)) {
            throw new IllegalArgumentException("Sub-lava geode plans require minecraft:nether_quartz_ore");
        }
        if (ancientDebrisHeart.filter(ANCIENT_DEBRIS::equals).isEmpty() && ancientDebrisHeart.isPresent()) {
            throw new IllegalArgumentException("Ancient Debris hearts require minecraft:ancient_debris");
        }
        if (blocksBelowLava < IoeNetherGeodesConfig.minBlocksBelowLava()
                || blocksBelowLava > IoeNetherGeodesConfig.maxBlocksBelowLava()) {
            throw new IllegalArgumentException("blocksBelowLava must stay inside the configured sub-lava depth band");
        }
        if (randomNetherGeode) {
            throw new IllegalArgumentException("Nether geodes must not be random netherrack geodes");
        }
        if (!Double.isFinite(ancientDebrisMotherlodeChance)
                || ancientDebrisMotherlodeChance < 0.0D
                || ancientDebrisMotherlodeChance > 0.05D) {
            throw new IllegalArgumentException("ancientDebrisMotherlodeChance must stay extremely rare");
        }
    }

    public boolean hasAncientDebrisHeart() {
        return ancientDebrisHeart.isPresent();
    }
}
