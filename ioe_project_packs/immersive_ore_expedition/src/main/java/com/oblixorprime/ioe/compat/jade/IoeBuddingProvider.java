package com.oblixorprime.ioe.compat.jade;

import com.oblixorprime.ioe.budding.BuddingNodeInfo;
import com.oblixorprime.ioe.budding.BuddingBlockIdentity;
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
        data.remove(DATA_KEY);
        if (!(accessor.getLevel() instanceof ServerLevel level)) return;
        var identity = BuddingBlockIdentity.of(accessor.getBlock());
        if (identity.isEmpty()) return;
        var block = identity.orElseThrow();
        ExpeditionLocatorService.index(level).buddingNodeAt(level.dimension(), accessor.getPosition())
                .filter(context -> context.node().family().equals(block.family()))
                .ifPresent(context -> {
                    CompoundTag payload = context.node().save();
                    context.site().quality().ifPresent(quality -> payload.putString("quality", quality.name()));
                    data.put(DATA_KEY, payload);
                });
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var identity = BuddingBlockIdentity.of(accessor.getBlock());
        if (identity.isEmpty()) return;
        var block = identity.orElseThrow();
        tooltip.add(Component.translatable("tooltip.ioe.budding.rank",
                Component.translatable("budding.ioe.rank." + block.rank().path())));
        tooltip.add(Component.translatable("tooltip.ioe.budding.family", block.displayFamily()));
        CompoundTag payload = accessor.getServerData().getCompound(DATA_KEY);
        var info = BuddingNodeInfo.load(payload).filter(node -> node.pos().equals(accessor.getPosition())
                && node.family().equals(block.family()));
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
