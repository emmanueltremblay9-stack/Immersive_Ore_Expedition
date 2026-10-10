package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.expeditionlocator.ExpeditionSite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.Objects;

/** Server-only identity, deliberately independent of mutable quality/source/node metadata. */
public record DiscoverySiteKey(ResourceLocation dimension, BlockPos anchor) {
    public DiscoverySiteKey {
        Objects.requireNonNull(dimension);
        anchor = Objects.requireNonNull(anchor).immutable();
    }
    public static DiscoverySiteKey from(ExpeditionSite site) {
        return new DiscoverySiteKey(site.dimension().location(), site.pos());
    }
}
