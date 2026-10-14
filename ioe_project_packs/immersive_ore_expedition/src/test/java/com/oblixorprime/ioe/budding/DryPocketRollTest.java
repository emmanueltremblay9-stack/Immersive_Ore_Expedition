package com.oblixorprime.ioe.budding;

import com.oblixorprime.ioe.core.SiteQuality;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class DryPocketRollTest {
    @Test
    void oneUniformDrawIncludesBothBounds() {
        for (int value = 0; value <= 5; value++) {
            int outcome = value;
            int[] calls = {0};
            assertEquals(value, DryPocketRoll.roll(new LegacyRandomSource(0) {
                @Override public int nextInt(int bound) {
                    assertEquals(6, bound);
                    assertEquals(1, ++calls[0]);
                    return outcome;
                }
            }));
            assertEquals(1, calls[0]);
        }
    }

    @Test
    void siteDrawIsRepeatableAndCannotConsumeSeedRewardRandomness() {
        boolean[][] observed = new boolean[6][2];
        for (long seed = 0; seed < 1000; seed++) {
            var rewardRandom = RandomSource.create(seed ^ 0x53454544L);
            int count = DryPocketRoll.forSite(seed);
            boolean reward = DrySiteReward.roll(SiteQuality.DRY, rewardRandom);
            assertEquals(count, DryPocketRoll.forSite(seed));
            assertTrue(count >= 0 && count <= 5);
            assertEquals(reward, DrySiteReward.roll(SiteQuality.DRY, RandomSource.create(seed ^ 0x53454544L)));
            observed[count][reward ? 1 : 0] = true;
        }
        for (boolean[] outcomes : observed) assertTrue(outcomes[0] && outcomes[1], "Every count permits either seed outcome");
    }
}
