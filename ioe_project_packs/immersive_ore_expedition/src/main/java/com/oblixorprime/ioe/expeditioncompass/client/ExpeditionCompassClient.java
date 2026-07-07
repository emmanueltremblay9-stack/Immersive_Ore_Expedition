package com.oblixorprime.ioe.expeditioncompass.client;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassAngle;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassItem;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassMenuSnapshot;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget;
import com.oblixorprime.ioe.expeditioncompass.IoeItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Objects;

public final class ExpeditionCompassClient {
    public static final ResourceLocation ANGLE_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "angle");

    private ExpeditionCompassClient() {
    }

    public static void register(IEventBus modEventBus) {
        Objects.requireNonNull(modEventBus, "modEventBus");
        modEventBus.addListener(ExpeditionCompassClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ExpeditionCompassClient::registerItemProperties);
    }

    static void registerItemProperties() {
        ItemProperties.register(IoeItems.EXPEDITION_COMPASS.get(), ANGLE_PROPERTY, ExpeditionCompassClient::angle);
    }

    public static void openMenu(ExpeditionCompassMenuSnapshot snapshot) {
        ExpeditionCompassScreen.open(snapshot);
    }

    private static float angle(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        ExpeditionCompassTarget target = ExpeditionCompassItem.target(stack).orElse(null);
        if (target == null) {
            return ExpeditionCompassAngle.UNBOUND_ANGLE;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel effectiveLevel = level == null ? minecraft.level : level;
        LivingEntity viewer = entity == null ? minecraft.player : entity;
        if (effectiveLevel == null || viewer == null) {
            return ExpeditionCompassAngle.UNBOUND_ANGLE;
        }

        return ExpeditionCompassAngle.angleToTarget(
                effectiveLevel.dimension(),
                viewer.getX(),
                viewer.getZ(),
                viewer.getYRot(),
                target
        );
    }
}
