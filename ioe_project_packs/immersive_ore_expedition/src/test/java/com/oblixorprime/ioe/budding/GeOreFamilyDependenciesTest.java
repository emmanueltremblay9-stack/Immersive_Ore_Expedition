package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class GeOreFamilyDependenciesTest {
    @Test
    void ordinaryStorageMappingUsesApprovedNamespacesAndNames() {
        Set<String> ie = Set.of("aluminum", "lead", "nickel", "silver", "uranium");
        int count = 0;
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) continue;
            count++;
            assertEquals(ie.contains(family.key()), family.requiresImmersiveEngineering());
            assertEquals(ie.contains(family.key()) ? "immersiveengineering:storage_" + family.key()
                    : "minecraft:" + family.key() + "_block", family.storageBlockId().toString());
            assertEquals("geore:" + family.key() + "_block", family.pocketBlockId().toString());
            assertEquals("geore:" + family.key(), family.identity().toString());
            assertEquals(4, family.growthProductIds().stream().distinct().count());
        }
        assertEquals(13, count);
    }

    @Test
    void missingIeDisablesOnlyItsFiveFamiliesWithoutFallback() {
        Set<String> base = Set.of("ae2", "geore");
        int enabled = 0;
        for (BuddingResourceFamily family : BuddingResourceFamily.values()) {
            if (family.kind() != BuddingResourceFamily.Kind.GEORE) continue;
            assertEquals(!family.requiresImmersiveEngineering(), family.dependenciesPresent(base::contains));
            if (family.dependenciesPresent(base::contains)) enabled++;
            assertTrue(family.dependenciesPresent(mod -> true));
            assertFalse(family.dependenciesPresent(mod -> !mod.equals("ae2")));
            assertFalse(family.dependenciesPresent(mod -> !mod.equals("geore")));
            assertFalse(family.dependenciesPresent(mod -> false));
        }
        assertEquals(8, enabled);
    }

    @Test
    void nativeAe2FamiliesCannotEnterGeOreRegistration() {
        for (BuddingResourceFamily family : Set.of(BuddingResourceFamily.CERTUS_QUARTZ,
                BuddingResourceFamily.ENTROIZED_FLUIX)) {
            assertFalse(family.dependenciesPresent(mod -> true));
            assertThrows(IllegalStateException.class, family::pocketBlockId);
        }
    }
}
