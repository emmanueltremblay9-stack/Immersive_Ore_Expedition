package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourceRef;

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
        if (maxSurfaceFluidBlocks < 0) {
            throw new IllegalArgumentException("maxSurfaceFluidBlocks must not be negative");
        }
    }
}
