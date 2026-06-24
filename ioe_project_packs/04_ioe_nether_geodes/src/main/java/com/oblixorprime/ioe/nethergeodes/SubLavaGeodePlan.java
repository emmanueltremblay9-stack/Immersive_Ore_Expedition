package com.oblixorprime.ioe.nethergeodes;

import com.oblixorprime.ioe.core.ExpeditionAnchorRef;
import com.oblixorprime.ioe.core.ResourcePolicyDecision;
import com.oblixorprime.ioe.core.ResourceRef;

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
    public SubLavaGeodePlan {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(lavaLake, "lavaLake");
        Objects.requireNonNull(quartzResource, "quartzResource");
        ancientDebrisHeart = Objects.requireNonNull(ancientDebrisHeart, "ancientDebrisHeart");
        skippedResources = List.copyOf(Objects.requireNonNull(skippedResources, "skippedResources"));
        rejectedResources = List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
        if (blocksBelowLava < 0) {
            throw new IllegalArgumentException("blocksBelowLava must not be negative");
        }
        if (randomNetherGeode) {
            throw new IllegalArgumentException("Nether geodes must not be random netherrack geodes");
        }
        if (ancientDebrisMotherlodeChance < 0.0D || ancientDebrisMotherlodeChance > 0.05D) {
            throw new IllegalArgumentException("ancientDebrisMotherlodeChance must stay extremely rare");
        }
    }

    public boolean hasAncientDebrisHeart() {
        return ancientDebrisHeart.isPresent();
    }
}
