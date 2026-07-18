package com.oblixorprime.ioe.expeditioncompass;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.concurrent.atomic.AtomicBoolean;

final class IoeVillageExpeditionTrades {
    static final int CARTOGRAPHER_LEVEL = 3;
    static final int EMERALD_COST = 12;
    static final int MAX_USES = 2;
    static final int VILLAGER_XP = 15;
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private IoeVillageExpeditionTrades() {
    }

    static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(IoeVillageExpeditionTrades::addTrades);
        }
    }

    static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.CARTOGRAPHER) {
            return;
        }
        event.getTrades().get(CARTOGRAPHER_LEVEL).add(new BasicItemListing(
                new ItemStack(Items.COMPASS),
                new ItemStack(Items.EMERALD, EMERALD_COST),
                new ItemStack(IoeItems.EXPEDITION_COMPASS.get()),
                MAX_USES,
                VILLAGER_XP,
                0.2F
        ));
    }
}
