package com.oblixorprime.ioe.compat.jade;

import com.oblixorprime.ioe.budding.BuddingNodeInfo;
import com.oblixorprime.ioe.budding.GeOreBuddingBlock;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

public enum IoeBuddingProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String DATA_KEY = "immersive_ore_expedition:budding";

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.parse(DATA_KEY);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel level) || !(accessor.getBlock() instanceof GeOreBuddingBlock block)) return;
        data.remove(DATA_KEY);
        ExpeditionLocatorService.index(level).buddingNodeAt(level.dimension(), accessor.getPosition())
                .filter(context -> context.node().family().equals(block.family().identity()))
                .ifPresent(context -> {
                    CompoundTag payload = context.node().save();
                    context.site().quality().ifPresent(quality -> payload.putString("quality", quality.name()));
                    data.put(DATA_KEY, payload);
                });
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlock() instanceof GeOreBuddingBlock block)) return;
        tooltip.add(Component.translatable("tooltip.ioe.budding.rank",
                Component.translatable("budding.ioe.rank." + block.rank().path())));
        tooltip.add(Component.translatable("tooltip.ioe.budding.family", "GeOre"));
        CompoundTag payload = accessor.getServerData().getCompound(DATA_KEY);
        var info = BuddingNodeInfo.load(payload).filter(node -> node.pos().equals(accessor.getPosition())
                && node.family().equals(block.family().identity()));
        if (info.isEmpty()) return; // Hand-placed or legacy blocks have no proven generation metadata.
        String quality = payload.getString("quality");
        if (java.util.Arrays.stream(com.oblixorprime.ioe.core.SiteQuality.values()).noneMatch(q -> q.name().equals(quality))) return;
        BuddingNodeInfo node = info.orElseThrow();
        tooltip.add(Component.translatable("tooltip.ioe.budding.quality",
                Component.translatable("budding.ioe.quality." + quality.toLowerCase(Locale.ROOT))));
        tooltip.add(Component.translatable("tooltip.ioe.budding.node", node.index(), node.count()));
        tooltip.add(Component.translatable("tooltip.ioe.budding.ore", node.initialOre()));
        if (block.rank() == com.oblixorprime.ioe.budding.BuddingRank.FLAWLESS && quality.equals("MOTHERLODE")) {
            tooltip.add(Component.translatable("tooltip.ioe.budding.flawless", 1, 1));
        }
    }
}
