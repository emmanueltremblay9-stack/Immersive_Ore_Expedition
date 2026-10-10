package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

final class FirstLoadAdmissionsTest {
    @Test
    void reloadResumesOnlyWithinOriginalWindow() {
        var removed = new ArrayList<String>();
        var admissions = new FirstLoadAdmissions<String>(4, 100, removed::add);
        assertFalse(admissions.admit("old", false, 0));
        assertTrue(admissions.admit("new", true, 10));
        // A missing chunk consumes no permission; a later disk load is still the same admission.
        assertTrue(admissions.admit("new", false, 109));
        assertTrue(admissions.admit("new", true, 109)); // duplicate new event cannot renew it either
        assertFalse(admissions.contains("new", 110));
        assertFalse(admissions.admit("new", false, 110));
        assertEquals(java.util.List.of("new"), removed);
        assertEquals(0, admissions.size());
    }

    @Test
    void saturationEvictsOldestAdmissionNotLeastRecentlyLoaded() {
        var removed = new ArrayList<String>();
        var admissions = new FirstLoadAdmissions<String>(2, 100, removed::add);
        admissions.admit("overworld:A", true, 0);
        admissions.admit("nether:B", true, 1);
        admissions.admit("overworld:A", false, 2);
        admissions.admit("end:C", true, 3);
        assertEquals(java.util.List.of("overworld:A"), removed);
        assertFalse(admissions.admit("overworld:A", false, 4));
        assertTrue(admissions.contains("nether:B", 4));
        assertTrue(admissions.contains("end:C", 4));
        assertEquals(2, admissions.size());
    }

    @Test
    void repeatedAdmissionsBoundOwnedStateAndSessionClearRevokesEverything() {
        var ownedState = new HashSet<Integer>();
        var admissions = new FirstLoadAdmissions<Integer>(64, 12000, ownedState::remove);
        for (int i = 0; i < 10000; i++) {
            assertTrue(admissions.admit(i, true, i / 100));
            ownedState.add(i);
            assertTrue(admissions.size() <= 64);
            assertEquals(admissions.size(), ownedState.size());
        }
        admissions.clear();
        assertTrue(ownedState.isEmpty());
        for (int i = 0; i < 10000; i++) assertFalse(admissions.admit(i, false, 0));
        assertEquals(0, admissions.size());
    }

    @Test
    void expirationWithoutReloadReleasesAllOwnedStateOnce() {
        var removed = new ArrayList<Integer>();
        var admissions = new FirstLoadAdmissions<Integer>(4, 20, removed::add);
        admissions.admit(1, true, 0);
        admissions.admit(2, true, 1);
        admissions.admit(3, true, 2);
        admissions.remove(2); // successful completion uses the same cleanup
        admissions.expire(20);
        assertEquals(java.util.List.of(2, 1), removed);
        assertTrue(admissions.contains(3, 21));
        admissions.expire(22);
        admissions.clear();
        assertEquals(java.util.List.of(2, 1, 3), removed);
        assertEquals(0, admissions.size());
    }
}
