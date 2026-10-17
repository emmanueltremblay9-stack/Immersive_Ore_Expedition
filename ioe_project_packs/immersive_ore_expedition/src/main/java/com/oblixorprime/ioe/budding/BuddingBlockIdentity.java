package com.oblixorprime.ioe.budding;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

/** Recognizes IOE-owned hearts and native Certus, without adopting vanilla budding blocks. */
public record BuddingBlockIdentity(ResourceLocation family, BuddingRank rank, String displayFamily) {
    public static Optional<BuddingBlockIdentity> of(Block block) {
        if (block instanceof GeOreBuddingBlock geore) {
            return Optional.of(new BuddingBlockIdentity(geore.family().identity(), geore.rank(), geore.family().displayFamily()));
        }
        return NativeCertusBudding.rank(block).map(rank ->
                new BuddingBlockIdentity(NativeCertusBudding.FAMILY, rank, "AE2"));
    }

    public static boolean isCanonical(Block block) {
        return of(block).isPresent();
    }
}
