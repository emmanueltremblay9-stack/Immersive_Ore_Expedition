package com.oblixorprime.ioe.budding;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GlowstoneFamilyTest {
    @Test void ownedProductsUseVanillaStorageWithoutDependenciesOrGeOrePlacement() {
        var family = BuddingResourceFamily.GLOWSTONE;
        assertTrue(family.dependenciesPresent(mod -> false));
        assertTrue(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> true, id -> true));
        assertFalse(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> !id.equals(family.storageBlockId()), id -> true));
        assertFalse(IoeGeOreBuddingBlocks.resourcesPresent(family, mod -> false, id -> true, id -> false));
        assertEquals("minecraft:glowstone", family.identity().toString());
        assertEquals("Minecraft", family.displayFamily());
        assertEquals("minecraft:glowstone", family.storageBlockId().toString());
        assertEquals(List.of("immersive_ore_expedition:small_glowstone_bud", "immersive_ore_expedition:medium_glowstone_bud",
                "immersive_ore_expedition:large_glowstone_bud", "immersive_ore_expedition:glowstone_cluster"), family.growthProductIds().stream().map(Object::toString).toList());
        assertTrue(BuddingResourceFamily.fromGeOreMaterial("glowstone").isEmpty());
        assertThrows(IllegalStateException.class, family::pocketBlockId);
        for (var rank : BuddingRank.values()) assertTrue(family.observedNativeBuddingId(rank).isEmpty());
    }
}
