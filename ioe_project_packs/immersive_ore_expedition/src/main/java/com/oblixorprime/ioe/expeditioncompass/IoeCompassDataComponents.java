package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IoeCompassDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ImmersiveOreExpeditionMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExpeditionCompassTarget>>
            EXPEDITION_COMPASS_TARGET = COMPONENTS.registerComponentType(
                    "expedition_compass_target",
                    builder -> builder
                            .persistent(ExpeditionCompassTarget.CODEC)
                            .networkSynchronized(ExpeditionCompassTarget.STREAM_CODEC)
                            .cacheEncoding()
            );

    private IoeCompassDataComponents() {
    }

    static DataComponentType<ExpeditionCompassTarget> targetComponent() {
        return EXPEDITION_COMPASS_TARGET.get();
    }

    static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
