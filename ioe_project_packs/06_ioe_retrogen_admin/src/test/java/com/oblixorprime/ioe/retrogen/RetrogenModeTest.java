package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetrogenModeTest {
    @Test
    void parsesConfigValuesAndFallsBackConservatively() {
        assertEquals(RetrogenMode.OFF, RetrogenMode.fromConfig("off"));
        assertEquals(RetrogenMode.ADMIN_RADIUS, RetrogenMode.fromConfig("admin-radius"));
        assertEquals(RetrogenMode.CLUE_PLUS_POCKET, RetrogenMode.fromConfig("CLUE_PLUS_POCKET"));
        assertEquals(RetrogenMode.UNEXPLORED_CHUNKS_ONLY, RetrogenMode.fromConfig("unknown"));
        assertEquals(RetrogenMode.UNEXPLORED_CHUNKS_ONLY, RetrogenMode.fromConfig(null));
    }
}
