package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.Objects;

public record ServerboundExpeditionCompassRefreshPayload(
        InteractionHand hand
) implements CustomPacketPayload {
    public static final Type<ServerboundExpeditionCompassRefreshPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "expedition_compass/refresh")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundExpeditionCompassRefreshPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundExpeditionCompassRefreshPayload::write, ServerboundExpeditionCompassRefreshPayload::new);

    public ServerboundExpeditionCompassRefreshPayload {
        Objects.requireNonNull(hand, "hand");
    }

    private ServerboundExpeditionCompassRefreshPayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
    }

    @Override
    public Type<ServerboundExpeditionCompassRefreshPayload> type() {
        return TYPE;
    }
}
