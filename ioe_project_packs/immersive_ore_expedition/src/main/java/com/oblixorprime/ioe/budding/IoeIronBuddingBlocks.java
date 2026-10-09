package com.oblixorprime.ioe.budding;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;

/** Compatibility facade for the established Iron integration. */
public final class IoeIronBuddingBlocks {
    private IoeIronBuddingBlocks() { }
    public static void register(IEventBus bus) { IoeGeOreBuddingBlocks.register(bus); }
    public static Block block(BuddingRank rank) { return IoeGeOreBuddingBlocks.block(BuddingResourceFamily.GEORE_IRON, rank); }
}
