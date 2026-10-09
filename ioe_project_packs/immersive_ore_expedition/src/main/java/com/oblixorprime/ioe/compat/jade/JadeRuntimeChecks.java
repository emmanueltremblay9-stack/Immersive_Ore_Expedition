package com.oblixorprime.ioe.compat.jade;

import com.oblixorprime.ioe.budding.BuddingNodeInfo;
import com.oblixorprime.ioe.budding.BuddingRank;
import com.oblixorprime.ioe.budding.IoeGeOreBuddingBlocks;
import com.oblixorprime.ioe.budding.BuddingResourceFamily;
import com.oblixorprime.ioe.core.SiteQuality;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Loaded by the GameTest only when the optional Jade runtime is present. */
public final class JadeRuntimeChecks {
    public static void run(GameTestHelper helper) throws ReflectiveOperationException, java.io.IOException {
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (IoeGeOreBuddingBlocks.available(family)) checkFamily(helper, family);
        }
        helper.succeed();
    }

    private static void checkFamily(GameTestHelper helper, BuddingResourceFamily family)
            throws ReflectiveOperationException, java.io.IOException {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
        level.setBlockAndUpdate(pos, IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED).defaultBlockState());
        var registration = Class.forName("snownee.jade.impl.WailaCommonRegistration");
        var providers = (List<?>) registration.getMethod("getBlockNBTProviders", net.minecraft.world.level.block.Block.class,
                net.minecraft.world.level.block.entity.BlockEntity.class).invoke(registration.getMethod("instance").invoke(null),
                IoeGeOreBuddingBlocks.block(family, BuddingRank.FLAWED), null);
        helper.assertTrue(providers.contains(IoeBuddingProvider.INSTANCE), "Jade did not discover/register the IOE server provider");
        var node = new BuddingNodeInfo(pos, 3, 7, 7, family.identity());
        var site = ExpeditionSite.anchor(level.dimension(), pos.above(24), ResourceLocation.parse("immersive_ore_expedition:miner_camp"),
                null, SiteQuality.MOTHERLODE, "test").withBuddingNodes(List.of(node));
        ExpeditionLocatorService.record(level, site);
        com.oblixorprime.ioe.expeditionlocator.BuddingPersistenceRuntimeChecks.reloadFromDisk(level);
        CompoundTag serverData = new CompoundTag();
        BlockAccessor accessor = (BlockAccessor) Proxy.newProxyInstance(BlockAccessor.class.getClassLoader(),
                new Class<?>[]{BlockAccessor.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getLevel" -> level;
                    case "getBlock" -> level.getBlockState(pos).getBlock();
                    case "getPosition" -> pos;
                    case "getServerData" -> serverData;
                    default -> throw new AssertionError("Unexpected Jade accessor call " + method.getName());
                });
        List<Component> lines = new ArrayList<>();
        ITooltip tooltip = (ITooltip) Proxy.newProxyInstance(ITooltip.class.getClassLoader(), new Class<?>[]{ITooltip.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("add") && args[0] instanceof Component component) {
                        lines.add(component);
                        return null;
                    }
                    throw new AssertionError("Unexpected Jade tooltip call " + method.getName());
                });
        for (BuddingRank rank : List.of(BuddingRank.FLAWED, BuddingRank.CHIPPED, BuddingRank.FLAWLESS)) {
            level.setBlockAndUpdate(pos, IoeGeOreBuddingBlocks.block(family, rank).defaultBlockState());
            IoeBuddingProvider.INSTANCE.appendServerData(serverData, accessor);
            lines.clear();
            IoeBuddingProvider.INSTANCE.appendTooltip(tooltip, accessor, null);
            var rankLine = line(lines, "rank");
            helper.assertTrue(rankLine.getArgs()[0].equals(Component.translatable("budding.ioe.rank." + rank.path())),
                    "Jade confused site quality with the current degraded rank");
            helper.assertTrue(line(lines, "quality").getArgs()[0].equals(Component.translatable("budding.ioe.quality.motherlode")),
                    "Jade lost the original site quality");
            helper.assertTrue(java.util.Arrays.equals(line(lines, "node").getArgs(), new Object[]{3, 7}), "Wrong node index/count");
            helper.assertTrue(line(lines, "ore").getArgs()[0].equals(7), "Wrong original ore count");
            helper.assertTrue(line(lines, "family").getArgs()[0].equals("GeOre"), "Wrong family");
            helper.assertTrue(lines.size() == (rank == BuddingRank.FLAWLESS ? 6 : 5), "Wrong Flawless site indicator");
        }
        ExpeditionLocatorService.removeBuddingNode(level, pos);
        com.oblixorprime.ioe.expeditionlocator.BuddingPersistenceRuntimeChecks.reloadFromDisk(level);
        serverData.remove("immersive_ore_expedition:budding");
        IoeBuddingProvider.INSTANCE.appendServerData(serverData, accessor);
        lines.clear();
        IoeBuddingProvider.INSTANCE.appendTooltip(tooltip, accessor, null);
        helper.assertTrue(lines.size() == 2, "Unproven/player-replaced blocks must not inherit site metadata");
    }

    private static TranslatableContents line(List<Component> lines, String key) {
        return lines.stream().map(Component::getContents).filter(TranslatableContents.class::isInstance)
                .map(TranslatableContents.class::cast).filter(value -> value.getKey().equals("tooltip.ioe.budding." + key))
                .findFirst().orElseThrow();
    }
}
