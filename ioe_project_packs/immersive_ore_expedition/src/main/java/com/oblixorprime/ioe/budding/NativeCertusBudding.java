package com.oblixorprime.ioe.budding;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/** Registry-only adapter. AE2 retains ownership of growth, degradation, repair and loot. */
public final class NativeCertusBudding {
    public static final ResourceLocation FAMILY = BuddingResourceFamily.CERTUS_QUARTZ.identity();
    public static final ResourceLocation QUARTZ = ResourceLocation.parse("ae2:quartz_block");

    private NativeCertusBudding() { }

    public static ResourceLocation id(BuddingRank rank) {
        return BuddingResourceFamily.CERTUS_QUARTZ.observedNativeBuddingId(rank).orElseThrow();
    }

    public static Optional<BuddingRank> rank(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return Arrays.stream(BuddingRank.values()).filter(rank -> id(rank).equals(id)).findFirst();
    }

    public static Block block(BuddingRank rank) {
        return BuiltInRegistries.BLOCK.getOptional(id(rank)).orElseThrow(() ->
                new IllegalStateException("Missing native Certus rank " + rank));
    }

    public static boolean available() {
        return available(ModList.get()::isLoaded, BuiltInRegistries.BLOCK::containsKey);
    }

    public static boolean available(Predicate<String> loaded, Predicate<ResourceLocation> present) {
        return loaded.test("ae2") && loaded.test("ae2cs") && present.test(QUARTZ)
                && Arrays.stream(BuddingRank.values()).allMatch(rank -> present.test(id(rank)))
                && BuddingResourceFamily.CERTUS_QUARTZ.growthProductIds().stream().allMatch(present);
    }
}
