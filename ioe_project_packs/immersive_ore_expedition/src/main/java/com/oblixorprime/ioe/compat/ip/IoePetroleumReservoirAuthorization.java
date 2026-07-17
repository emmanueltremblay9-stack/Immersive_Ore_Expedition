package com.oblixorprime.ioe.compat.ip;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Exact-object authorization used by the IP storage mixin. Native free reservoir generation never enters this
 * scope, while a confirmed IOE site authorizes one specific reservoir for one specific dimension.
 */
public final class IoePetroleumReservoirAuthorization {
    private static final ThreadLocal<Admission> ACTIVE = new ThreadLocal<>();

    private IoePetroleumReservoirAuthorization() {
    }

    public static Scope authorize(ResourceKey<Level> dimension, Object reservoir) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(reservoir, "reservoir");
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("Nested Immersive Petroleum reservoir authorization is forbidden");
        }
        ACTIVE.set(new Admission(dimension, reservoir));
        return new Scope();
    }

    public static boolean permits(ResourceKey<Level> dimension, Object reservoir) {
        Admission admission = ACTIVE.get();
        return admission != null
                && admission.dimension().equals(dimension)
                && admission.reservoir() == reservoir;
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;

        private Scope() {
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                ACTIVE.remove();
            }
        }
    }

    private record Admission(ResourceKey<Level> dimension, Object reservoir) {
    }
}
