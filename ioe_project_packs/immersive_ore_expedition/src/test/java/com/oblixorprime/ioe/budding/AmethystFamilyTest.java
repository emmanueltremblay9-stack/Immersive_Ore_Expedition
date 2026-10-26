package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AmethystFamilyTest {
    @Test void profileAUsesVanillaProductsAndStorageWithoutAddingDependenciesOrPocketPolicy() {
        var family = BuddingResourceFamily.AMETHYST;
        assertTrue(family.dependenciesPresent(mod -> false));
        assertTrue(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> true, id -> true));
        assertFalse(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> !id.equals(family.storageBlockId()), id -> true));
        assertFalse(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> true, id -> false));
        assertEquals("minecraft:amethyst", family.identity().toString());
        assertEquals("Minecraft", family.displayFamily());
        assertEquals("minecraft:amethyst_block", family.storageBlockId().toString());
        assertEquals(List.of("minecraft:small_amethyst_bud", "minecraft:medium_amethyst_bud",
                "minecraft:large_amethyst_bud", "minecraft:amethyst_cluster"), family.growthProductIds().stream().map(Object::toString).toList());
        assertTrue(BuddingResourceFamily.fromGeOreMaterial("amethyst").isEmpty());
        assertThrows(IllegalStateException.class, family::pocketBlockId);
        for (var rank : BuddingRank.values()) assertTrue(family.observedNativeBuddingId(rank).isEmpty());
    }
}
