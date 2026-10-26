package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionLocatorService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Player replacement must not inherit the generation metadata of an old node. */
public final class BuddingMetadataEvents {
    private BuddingMetadataEvents() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, BuddingMetadataEvents::onBreak);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, BuddingMetadataEvents::onPlace);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) ExpeditionLocatorService.removeBuddingNode(level, event.getPos());
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player && event.getLevel() instanceof ServerLevel level) {
            ExpeditionLocatorService.removeBuddingNode(level, event.getPos());
        }
    }
}
