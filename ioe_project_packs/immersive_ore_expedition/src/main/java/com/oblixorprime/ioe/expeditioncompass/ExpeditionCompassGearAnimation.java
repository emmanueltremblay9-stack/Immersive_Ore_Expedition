package com.oblixorprime.ioe.expeditioncompass;

public final class ExpeditionCompassGearAnimation {
    public static final int PHASE_COUNT = 4;
    public static final int TICKS_PER_PHASE = 2;
    public static final int CYCLE_TICKS = PHASE_COUNT * TICKS_PER_PHASE;

    private ExpeditionCompassGearAnimation() {
    }

    public static int phaseIndex(long gameTime) {
        return (int) (Math.floorMod(gameTime, CYCLE_TICKS) / TICKS_PER_PHASE);
    }

    public static float propertyValue(long gameTime) {
        return phaseIndex(gameTime) / (float) PHASE_COUNT;
    }
}
