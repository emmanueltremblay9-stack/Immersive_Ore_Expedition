package com.oblixorprime.ioe.expeditionlocator;

import com.oblixorprime.ioe.worldgen.RuntimeWorldgenPlacementProofResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class ExpeditionLocatorService {
    private static final ExpeditionLocatorIndex RUNTIME_INDEX = new ExpeditionLocatorIndex();

    private ExpeditionLocatorService() {
    }

    public static ExpeditionLocatorIndex index() {
        return RUNTIME_INDEX;
    }

    public static void recordPlacedProof(
            ResourceKey<Level> dimension,
            RuntimeWorldgenPlacementProofResult result
    ) {
        RUNTIME_INDEX.recordPlacedProof(
                Objects.requireNonNull(dimension, "dimension"),
                Objects.requireNonNull(result, "result")
        );
    }

    public static void clearForTesting() {
        RUNTIME_INDEX.clear();
    }
}
