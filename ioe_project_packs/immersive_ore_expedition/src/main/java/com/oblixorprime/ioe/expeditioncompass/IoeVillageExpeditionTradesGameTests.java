package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(ImmersiveOreExpeditionMod.MODID)
@PrefixGameTestTemplate(false)
public final class IoeVillageExpeditionTradesGameTests {
    private static final String TEMPLATE = "expedition_worldgen_empty";

    private IoeVillageExpeditionTradesGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void cartographerOffersExpeditionCompassUpgrade(GameTestHelper helper) {
        Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>> cartographerTrades =
                emptyTradeTable();
        IoeVillageExpeditionTrades.addTrades(new VillagerTradesEvent(
                cartographerTrades,
                VillagerProfession.CARTOGRAPHER,
                RegistryAccess.EMPTY
        ));
        helper.assertTrue(
                cartographerTrades.get(IoeVillageExpeditionTrades.CARTOGRAPHER_LEVEL).size() == 1,
                "The Cartographer did not receive exactly one Expedition Compass upgrade trade"
        );
        var offer = cartographerTrades.get(IoeVillageExpeditionTrades.CARTOGRAPHER_LEVEL)
                .getFirst()
                .getOffer(null, RandomSource.create(41L));
        helper.assertTrue(offer != null, "The Expedition Compass trade did not create a merchant offer");
        helper.assertTrue(offer.getBaseCostA().is(Items.COMPASS)
                        && offer.getBaseCostA().getCount() == 1,
                "The Expedition Compass trade does not require one normal Compass");
        helper.assertTrue(offer.getCostB().is(Items.EMERALD)
                        && offer.getCostB().getCount() == IoeVillageExpeditionTrades.EMERALD_COST,
                "The Expedition Compass trade has the wrong emerald cost");
        helper.assertTrue(offer.getResult().is(IoeItems.EXPEDITION_COMPASS.get()),
                "The Cartographer trade does not produce an Expedition Compass");
        helper.assertTrue(offer.getMaxUses() == IoeVillageExpeditionTrades.MAX_USES
                        && offer.getXp() == IoeVillageExpeditionTrades.VILLAGER_XP,
                "The Expedition Compass trade has the wrong use or XP limits");

        Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>> armorerTrades =
                emptyTradeTable();
        IoeVillageExpeditionTrades.addTrades(new VillagerTradesEvent(
                armorerTrades,
                VillagerProfession.ARMORER,
                RegistryAccess.EMPTY
        ));
        helper.assertTrue(
                armorerTrades.get(IoeVillageExpeditionTrades.CARTOGRAPHER_LEVEL).isEmpty(),
                "The Expedition Compass trade leaked into another villager profession"
        );
        helper.succeed();
    }

    private static Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>> emptyTradeTable() {
        Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>> trades = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            trades.put(level, new ArrayList<>());
        }
        return trades;
    }
}
