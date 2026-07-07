package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.Objects;

public record ServerboundExpeditionCompassClearPayload(
        InteractionHand hand
) implements CustomPacketPayload {
    public static final Type<ServerboundExpeditionCompassClearPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "expedition_compass/clear")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundExpeditionCompassClearPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundExpeditionCompassClearPayload::write, ServerboundExpeditionCompassClearPayload::new);

    public ServerboundExpeditionCompassClearPayload {
        Objects.requireNonNull(hand, "hand");
    }

    private ServerboundExpeditionCompassClearPayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
    }

    @Override
    public Type<ServerboundExpeditionCompassClearPayload> type() {
        return TYPE;
    }
}
