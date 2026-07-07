package com.oblixorprime.ioe.expeditioncompass;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ClientboundExpeditionCompassMenuPayload(
        ExpeditionCompassMenuSnapshot snapshot
) implements CustomPacketPayload {
    public static final Type<ClientboundExpeditionCompassMenuPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImmersiveOreExpeditionMod.MODID, "expedition_compass/menu")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundExpeditionCompassMenuPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundExpeditionCompassMenuPayload::write, ClientboundExpeditionCompassMenuPayload::new);

    public ClientboundExpeditionCompassMenuPayload {
        Objects.requireNonNull(snapshot, "snapshot");
    }

    private ClientboundExpeditionCompassMenuPayload(RegistryFriendlyByteBuf buffer) {
        this(ExpeditionCompassMenuSnapshot.STREAM_CODEC.decode(buffer));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        ExpeditionCompassMenuSnapshot.STREAM_CODEC.encode(buffer, snapshot);
    }

    @Override
    public Type<ClientboundExpeditionCompassMenuPayload> type() {
        return TYPE;
    }
}
