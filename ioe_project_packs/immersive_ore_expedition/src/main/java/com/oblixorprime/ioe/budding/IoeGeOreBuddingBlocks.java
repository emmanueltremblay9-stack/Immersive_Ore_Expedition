package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AmethystClusterBlock;
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
import java.util.function.Predicate;

/** Pre-register stable identities by loaded dependencies; activate only after validating real registry blocks. */
public final class IoeGeOreBuddingBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersiveOreExpeditionMod.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveOreExpeditionMod.MODID);
    private static final Map<BuddingResourceFamily, Map<BuddingRank, DeferredBlock<GeOreBuddingBlock>>> FAMILIES = new EnumMap<>(BuddingResourceFamily.class);

    private IoeGeOreBuddingBlocks() { }

    public static void register(IEventBus bus) {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (!family.dependenciesPresent(ModList.get()::isLoaded)) continue;
            Map<BuddingRank, DeferredBlock<GeOreBuddingBlock>> ranks = new EnumMap<>(BuddingRank.class);
            for (BuddingRank rank : BuddingRank.values()) {
                String name = rank.path() + "_budding_" + family.key();
                DeferredBlock<GeOreBuddingBlock> block = BLOCKS.register(name, () -> {
                    var properties = BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST).pushReaction(PushReaction.DESTROY);
                    return family == BuddingResourceFamily.GEORE_IRON ? new IronBuddingBlock(rank, properties)
                            : new GeOreBuddingBlock(family, rank, properties);
                });
                ranks.put(rank, block);
                ITEMS.registerSimpleBlockItem(name, block);
            }
            FAMILIES.put(family, ranks);
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static boolean resourcesPresent(BuddingResourceFamily family, Predicate<String> loaded,
                                            Predicate<ResourceLocation> present, Predicate<ResourceLocation> growthCompatible) {
        return family.dependenciesPresent(loaded) && present.test(family.storageBlockId())
                && present.test(family.pocketBlockId()) && family.growthProductIds().stream().allMatch(growthCompatible);
    }

    public static boolean available(BuddingResourceFamily family) {
        return FAMILIES.containsKey(family) && resourcesPresent(family, ModList.get()::isLoaded,
                BuiltInRegistries.BLOCK::containsKey,
                id -> BuiltInRegistries.BLOCK.getOptional(id).filter(block ->
                        block.defaultBlockState().hasProperty(AmethystClusterBlock.FACING)
                                && block.defaultBlockState().hasProperty(AmethystClusterBlock.WATERLOGGED)).isPresent());
    }

    public static Block block(BuddingResourceFamily family, BuddingRank rank) {
        var ranks = FAMILIES.get(family);
        if (ranks == null) throw new IllegalStateException("Budding dependencies absent for " + family);
        return ranks.get(rank).get();
    }
}
