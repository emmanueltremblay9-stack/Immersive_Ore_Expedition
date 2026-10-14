package com.oblixorprime.ioe.discovery;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Correlation token and page offset only. No player identity or world coordinates. */
public record JournalRequest(long token, int offset) implements CustomPacketPayload {
    public static final Type<JournalRequest> TYPE = new Type<>(ResourceLocation.parse("immersive_ore_expedition:journal/request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JournalRequest> CODEC =
            CustomPacketPayload.codec(JournalRequest::write, JournalRequest::new);
    public JournalRequest {
        if (offset < 0) throw new IllegalArgumentException("Negative journal offset");
    }
    private JournalRequest(RegistryFriendlyByteBuf buf) { this(buf.readLong(), buf.readVarInt()); }
    private void write(RegistryFriendlyByteBuf buf) { buf.writeLong(token); buf.writeVarInt(offset); }
    @Override public Type<JournalRequest> type() { return TYPE; }
}
