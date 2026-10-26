package com.oblixorprime.ioe.discovery;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.Objects;

/** Trusted server integration input, not a client packet or proof validator. */
public record DiscoveryEvidence(DiscoveryStage stage, BlockPos location,
                                ResourceLocation identifier, SiteQuality quality) {
    public DiscoveryEvidence {
        Objects.requireNonNull(stage);
        if (location != null) location = location.immutable();
        boolean valid = switch (stage) {
            case EVIDENCE_DISCOVERED -> location != null && identifier != null && quality == null;
            case SITE_LOCATED -> location != null && identifier == null && quality == null;
            case RESOURCE_IDENTIFIED -> location == null && identifier != null && quality == null;
            case SITE_SURVEYED -> location == null && identifier == null && quality != null;
            case EXPEDITION_DOCUMENTED -> location == null && identifier == null && quality == null;
        };
        if (!valid) throw new IllegalArgumentException("Only stage-appropriate evidence is accepted");
    }
}
