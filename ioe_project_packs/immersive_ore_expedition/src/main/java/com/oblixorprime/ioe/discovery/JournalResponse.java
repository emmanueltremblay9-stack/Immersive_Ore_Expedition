package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;

public record JournalResponse(long token, DiscoveryPage page) implements CustomPacketPayload {
    private static final int MAX_ID = 256;
    public static final Type<JournalResponse> TYPE = new Type<>(ResourceLocation.parse("immersive_ore_expedition:journal/page"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JournalResponse> CODEC =
            CustomPacketPayload.codec(JournalResponse::write, JournalResponse::read);
    private void write(RegistryFriendlyByteBuf b) {
        b.writeLong(token); b.writeVarInt(page.offset()); b.writeVarInt(page.total());
        if (page.total() == 0) return;
        DiscoveryView v = page.entry().orElseThrow();
        b.writeUUID(v.id()); b.writeEnum(v.stage());
        b.writeUtf(v.dimension().toString(), MAX_ID); b.writeUtf(v.clueType().toString(), MAX_ID); b.writeBlockPos(v.clueLocation());
        // Stage-gated serialization is independent of incidental fields in a view.
        if (v.stage().ordinal() >= 1) b.writeBlockPos(v.siteLocation().orElseThrow());
        if (v.stage().ordinal() >= 2) b.writeUtf(v.resource().orElseThrow().toString(), MAX_ID);
        if (v.stage().ordinal() >= 3) b.writeEnum(v.quality().orElseThrow());
    }
    private static JournalResponse read(RegistryFriendlyByteBuf b) {
        long token = b.readLong(); int offset = b.readVarInt(); int total = b.readVarInt();
        if (total == 0) return new JournalResponse(token, new DiscoveryPage(offset, total, Optional.empty()));
        if (total < 0 || offset < 0 || offset >= total) throw new IllegalArgumentException("Invalid journal page bounds");
        var id = b.readUUID(); var stage = b.readEnum(DiscoveryStage.class);
        var dimension = ResourceLocation.parse(b.readUtf(MAX_ID)); var type = ResourceLocation.parse(b.readUtf(MAX_ID));
        var clue = b.readBlockPos();
        var location = stage.ordinal() >= 1 ? Optional.of(b.readBlockPos()) : Optional.<net.minecraft.core.BlockPos>empty();
        var resource = stage.ordinal() >= 2 ? Optional.of(ResourceLocation.parse(b.readUtf(MAX_ID))) : Optional.<ResourceLocation>empty();
        var quality = stage.ordinal() >= 3 ? Optional.of(b.readEnum(SiteQuality.class)) : Optional.<SiteQuality>empty();
        return new JournalResponse(token, new DiscoveryPage(offset, total,
                Optional.of(new DiscoveryView(id, stage, dimension, type, clue, location, resource, quality))));
    }
    @Override public Type<JournalResponse> type() { return TYPE; }
}
