package com.oblixorprime.ioe.budding;

import net.minecraft.util.RandomSource;
import java.util.Objects;

/** One independent, reproducible draw for the whole pocket, never one draw per node. */
public final class DryPocketRoll {
    private DryPocketRoll() { }
    public static int roll(RandomSource random) { return Objects.requireNonNull(random, "random").nextInt(6); }
    public static int forSite(long siteSeed) { return roll(RandomSource.create(siteSeed ^ 0x4452594f52454cL)); }
}
