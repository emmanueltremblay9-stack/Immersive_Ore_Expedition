package com.oblixorprime.ioe.expeditioncompass;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record ExpeditionCompassMenuEntry(
        ExpeditionCompassTarget target,
        long distanceBlocks
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ExpeditionCompassMenuEntry> STREAM_CODEC =
            StreamCodec.ofMember(ExpeditionCompassMenuEntry::write, ExpeditionCompassMenuEntry::new);

    public ExpeditionCompassMenuEntry {
        Objects.requireNonNull(target, "target");
        if (distanceBlocks < 0L) {
            throw new IllegalArgumentException("distanceBlocks must be non-negative");
        }
    }

    private ExpeditionCompassMenuEntry(RegistryFriendlyByteBuf buffer) {
        this(
                ExpeditionCompassTarget.STREAM_CODEC.decode(buffer),
                buffer.readVarLong()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        ExpeditionCompassTarget.STREAM_CODEC.encode(buffer, target);
        buffer.writeVarLong(distanceBlocks);
    }
}
