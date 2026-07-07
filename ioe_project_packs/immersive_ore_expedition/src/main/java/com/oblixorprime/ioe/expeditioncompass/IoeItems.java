package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IoeItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveOreExpeditionMod.MODID);

    public static final DeferredItem<ExpeditionCompassItem> EXPEDITION_COMPASS = ITEMS.register(
            "expedition_compass",
            () -> new ExpeditionCompassItem(new Item.Properties().stacksTo(1))
    );

    private IoeItems() {
    }

    static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
