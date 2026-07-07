package com.oblixorprime.ioe.retrogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RetrogenStartResultTest {
    @Test
    void startedFlagMustMatchAcceptedChunkCount() {
        assertThrows(IllegalArgumentException.class, () -> new RetrogenStartResult(
                RetrogenMode.ADMIN_RADIUS,
                0,
                0,
                0,
                0,
                0,
                true,
                "inconsistent"
        ));
        assertThrows(IllegalArgumentException.class, () -> new RetrogenStartResult(
                RetrogenMode.ADMIN_RADIUS,
                1,
                0,
                0,
                0,
                0,
                false,
                "inconsistent"
        ));
    }
}
