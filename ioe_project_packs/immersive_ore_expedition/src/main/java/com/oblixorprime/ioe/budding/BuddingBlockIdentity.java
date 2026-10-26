package com.oblixorprime.ioe.budding;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

/** Only authorized GeOre and native Certus families participate in IOE nodes. */
public record BuddingBlockIdentity(ResourceLocation family, BuddingRank rank, String displayFamily) {
    public static Optional<BuddingBlockIdentity> of(Block block) {
        if (block instanceof GeOreBuddingBlock geore) {
            return Optional.of(new BuddingBlockIdentity(geore.family().identity(), geore.rank(), "GeOre"));
        }
        return NativeCertusBudding.rank(block).map(rank ->
                new BuddingBlockIdentity(NativeCertusBudding.FAMILY, rank, "AE2"));
    }

    public static boolean isCanonical(Block block) {
        return of(block).isPresent();
    }
}
