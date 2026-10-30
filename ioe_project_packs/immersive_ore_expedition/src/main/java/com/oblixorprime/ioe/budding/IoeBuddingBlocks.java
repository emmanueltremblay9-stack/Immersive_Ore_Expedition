package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class IoeBuddingBlocks {
    public static final List<String> GEORE_MATERIALS = List.of(
            "aluminum",
            "coal",
            "copper",
            "diamond",
            "emerald",
            "gold",
            "iron",
            "lapis",
            "lead",
            "nickel",
            "redstone",
            "silver",
            "uranium"
    );

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ImmersiveOreExpeditionMod.MODID);
    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ImmersiveOreExpeditionMod.MODID);
    private static final Map<String, EnumMap<BuddingRank, DeferredBlock<GeOreRankedBuddingBlock>>>
            GEORE_RANK_BLOCKS = new LinkedHashMap<>();
    private static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = new ArrayList<>();

    static {
        for (String material : GEORE_MATERIALS) {
            EnumMap<BuddingRank, DeferredBlock<GeOreRankedBuddingBlock>> ranks =
                    new EnumMap<>(BuddingRank.class);
            for (BuddingRank rank : BuddingRank.values()) {
                String name = blockPath(material, rank);
                DeferredBlock<GeOreRankedBuddingBlock> block = BLOCKS.register(
                        name,
                        ignored -> new GeOreRankedBuddingBlock(properties(), material, rank)
                );
                DeferredItem<BlockItem> item = ITEMS.register(
                        name,
                        () -> new BlockItem(block.get(), new Item.Properties())
                );
                ranks.put(rank, block);
                BLOCK_ITEMS.add(item);
            }
            GEORE_RANK_BLOCKS.put(material, ranks);
        }
    }

    private IoeBuddingBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    public static Optional<DeferredBlock<GeOreRankedBuddingBlock>> registration(
            String material,
            BuddingRank rank
    ) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(rank, "rank");
        EnumMap<BuddingRank, DeferredBlock<GeOreRankedBuddingBlock>> ranks =
                GEORE_RANK_BLOCKS.get(material);
        return ranks == null ? Optional.empty() : Optional.ofNullable(ranks.get(rank));
    }

    public static List<DeferredItem<BlockItem>> blockItems() {
        return List.copyOf(BLOCK_ITEMS);
    }

    public static ResourceLocation blockId(String material, BuddingRank rank) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(rank, "rank");
        if (!GEORE_MATERIALS.contains(material)) {
            throw new IllegalArgumentException("Unsupported GeOre material: " + material);
        }
        return ResourceLocation.fromNamespaceAndPath(
                ImmersiveOreExpeditionMod.MODID,
                blockPath(material, rank)
        );
    }

    private static String blockPath(String material, BuddingRank rank) {
        return rank.path() + "_budding_" + material;
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST)
                .randomTicks()
                .strength(1.5F)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops();
    }
}
