package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/** Stable Iron ids; no optional-mod classes are linked into IOE. */
public final class IoeIronBuddingBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersiveOreExpeditionMod.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveOreExpeditionMod.MODID);
    private static final Map<BuddingRank, DeferredBlock<IronBuddingBlock>> IRON = new EnumMap<>(BuddingRank.class);

    private IoeIronBuddingBlocks() {
    }

    public static void register(IEventBus bus) {
        if (!ModList.get().isLoaded("geore") || !ModList.get().isLoaded("ae2")) {
            return;
        }
        for (BuddingRank rank : BuddingRank.values()) {
            String name = rank.path() + "_budding_iron";
            DeferredBlock<IronBuddingBlock> block = BLOCKS.register(name, () -> new IronBuddingBlock(rank,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST)
                            .pushReaction(PushReaction.DESTROY)));
            IRON.put(rank, block);
            ITEMS.registerSimpleBlockItem(name, block);
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static Block block(BuddingRank rank) {
        DeferredBlock<IronBuddingBlock> block = IRON.get(rank);
        if (block == null) {
            throw new IllegalStateException("Iron Budding requires loaded AE2 and GeOre");
        }
        return block.get();
    }
}
