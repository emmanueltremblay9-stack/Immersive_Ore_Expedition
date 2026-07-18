package com.oblixorprime.ioe.expeditioncompass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ExpeditionCompassGearAnimationTest {
    @Test
    void advancesFourSynchronizedPhasesEveryTwoTicks() {
        assertEquals(0.0F, ExpeditionCompassGearAnimation.propertyValue(0L));
        assertEquals(0.0F, ExpeditionCompassGearAnimation.propertyValue(1L));
        assertEquals(0.25F, ExpeditionCompassGearAnimation.propertyValue(2L));
        assertEquals(0.25F, ExpeditionCompassGearAnimation.propertyValue(3L));
        assertEquals(0.5F, ExpeditionCompassGearAnimation.propertyValue(4L));
        assertEquals(0.5F, ExpeditionCompassGearAnimation.propertyValue(5L));
        assertEquals(0.75F, ExpeditionCompassGearAnimation.propertyValue(6L));
        assertEquals(0.75F, ExpeditionCompassGearAnimation.propertyValue(7L));
        assertEquals(0.0F, ExpeditionCompassGearAnimation.propertyValue(8L));
    }

    @Test
    void wrapsNegativeGameTimesWithFloorMod() {
        assertEquals(3, ExpeditionCompassGearAnimation.phaseIndex(-1L));
        assertEquals(0, ExpeditionCompassGearAnimation.phaseIndex(-8L));
    }
}
