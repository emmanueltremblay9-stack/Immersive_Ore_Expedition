package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlawlessPaymentTest {
    @Test void partialPaymentResumesWithoutChargingAgain() {
        var payment = new FlawlessPayment("certus@position", 1_000_000, 0);
        payment.credit(400_000);
        var resumed = new FlawlessPayment(payment.identity(), payment.cost(), payment.paid());
        assertEquals(600_000, resumed.request(1_000_000));
        resumed.credit(600_000);
        assertTrue(resumed.complete());
        assertEquals(0, resumed.request(1_000_000));
        assertEquals(1_000_000, resumed.paid());
    }
    @Test void noPowerPausesAndFinalDebitIsCapped() {
        var payment = new FlawlessPayment("attempt", 1000, 990);
        payment.credit(0);
        assertFalse(payment.complete());
        assertEquals(10, payment.request(150));
        payment.credit(10);
        assertTrue(payment.complete());
        // A failed roll is still a consumed attempt, so its replacement starts unpaid.
        assertFalse(new FlawlessPayment("attempt", payment.cost(), 0).complete());
    }
    @Test void invalidPersistenceAndDebitsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlawlessPayment("x", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new FlawlessPayment("x", 100, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new FlawlessPayment("x", 100, 101));
        var p = new FlawlessPayment("x", 100, 10);
        assertThrows(IllegalArgumentException.class, () -> p.credit(-1));
        assertThrows(IllegalArgumentException.class, () -> p.credit(91));
        assertEquals(10, p.paid());
    }
    @Test void costSnapshotAndFractionalConversionRemainStable() {
        var p = new FlawlessPayment("x", 100, 0);
        p.credit(99.5);
        var restored = new FlawlessPayment(p.identity(), p.cost(), p.paid());
        assertEquals(.5, restored.request(100));
        restored.credit(.5);
        assertTrue(restored.complete());
        assertEquals(100, restored.cost());
    }
}
