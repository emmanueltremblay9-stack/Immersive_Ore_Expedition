package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.util.RandomSource;

import java.util.Objects;

/** Independent of residual ore: exactly one neutral seed on one of ten DRY outcomes. */
public final class DrySiteReward {
    private DrySiteReward() {
    }

    public static boolean roll(SiteQuality quality, RandomSource random) {
        Objects.requireNonNull(quality, "quality");
        Objects.requireNonNull(random, "random");
        return quality == SiteQuality.DRY && random.nextInt(10) == 0;
    }
}
