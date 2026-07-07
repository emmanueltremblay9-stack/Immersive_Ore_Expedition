package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IoeCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersiveOreExpeditionMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> IMMERSIVE_ORE_EXPEDITION =
            CREATIVE_MODE_TABS.register("immersive_ore_expedition", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.immersive_ore_expedition"))
                    .icon(() -> new ItemStack(IoeItems.EXPEDITION_COMPASS.get()))
                    .displayItems((parameters, output) -> output.accept(IoeItems.EXPEDITION_COMPASS.get()))
                    .build());

    private IoeCreativeModeTabs() {
    }

    static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
