package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.Objects;

public record ServerboundExpeditionCompassSelectPayload(
        InteractionHand hand,
        ExpeditionCompassTarget target
) implements CustomPacketPayload {
    public static final Type<ServerboundExpeditionCompassSelectPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "expedition_compass/select")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundExpeditionCompassSelectPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundExpeditionCompassSelectPayload::write, ServerboundExpeditionCompassSelectPayload::new);

    public ServerboundExpeditionCompassSelectPayload {
        Objects.requireNonNull(hand, "hand");
        Objects.requireNonNull(target, "target");
    }

    private ServerboundExpeditionCompassSelectPayload(RegistryFriendlyByteBuf buffer) {
        this(
                buffer.readEnum(InteractionHand.class),
                ExpeditionCompassTarget.STREAM_CODEC.decode(buffer)
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        ExpeditionCompassTarget.STREAM_CODEC.encode(buffer, target);
    }

    @Override
    public Type<ServerboundExpeditionCompassSelectPayload> type() {
        return TYPE;
    }
}
