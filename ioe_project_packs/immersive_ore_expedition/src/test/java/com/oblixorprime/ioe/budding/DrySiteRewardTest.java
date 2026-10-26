package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class DrySiteRewardTest {
    @Test
    void exactlyOneOfTenDryOutcomesAwardsTheNeutralSeed() {
        int awards = 0;
        for (int outcome = 0; outcome < 10; outcome++) {
            int value = outcome;
            int[] draws = {0};
            var random = new LegacyRandomSource(0) {
                @Override
                public int nextInt(int bound) {
                    assertEquals(10, bound);
                    draws[0]++;
                    return value;
                }
            };
            if (DrySiteReward.roll(SiteQuality.DRY, random)) awards++;
            assertEquals(1, draws[0]);
        }
        assertEquals(1, awards);
    }

    @Test
    void productiveSitesNeverDrawOrAwardADrySeed() {
        var noDraw = new LegacyRandomSource(0) {
            @Override
            public int nextInt(int bound) {
                throw new AssertionError("Productive site consumed a DRY reward draw");
            }
        };
        for (SiteQuality quality : SiteQuality.values()) {
            if (quality.isProductive()) assertFalse(DrySiteReward.roll(quality, noDraw));
        }
    }
}
