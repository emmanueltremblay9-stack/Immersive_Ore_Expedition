package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;
import com.oblixorprime.ioe.core.ResourceType;

import java.util.Objects;

public record ReservoirSeepPlan(
        String reservoirId,
        ResourceRef clueFluid,
        int maxSurfaceFluidBlocks,
        boolean pocketLake,
        boolean vent,
        boolean rendersFullReservoir
) {
    public ReservoirSeepPlan {
        Objects.requireNonNull(reservoirId, "reservoirId");
        Objects.requireNonNull(clueFluid, "clueFluid");
        if (reservoirId.isBlank()) {
            throw new IllegalArgumentException("reservoirId must not be blank");
        }
        if (clueFluid.type() != ResourceType.FLUID) {
            throw new IllegalArgumentException("IP seep clue resource must be a concrete fluid");
        }
        if (maxSurfaceFluidBlocks < 0) {
            throw new IllegalArgumentException("maxSurfaceFluidBlocks must not be negative");
        }
        if (rendersFullReservoir) {
            throw new IllegalArgumentException("IP seep clues must not render full underground reservoirs");
        }
    }
}
