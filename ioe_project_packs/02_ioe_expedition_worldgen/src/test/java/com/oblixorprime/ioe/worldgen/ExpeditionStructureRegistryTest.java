package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionStructureRegistryTest {
    @Test
    void catalogsExpectedAlphaStructureIds() {
        assertEquals(6, ExpeditionStructureRegistry.enabledStructureIds().size());
        assertTrue(ExpeditionStructureRegistry.enabledStructureIds().contains(ExpeditionStructureRegistry.TINY_VERTICAL_MINE_ENTRANCE));
        assertTrue(ExpeditionStructureRegistry.enabledStructureIds().contains(ExpeditionStructureRegistry.ORE_LOAD_CHAMBER));
        assertTrue(ExpeditionStructureRegistry.isEnabledStructureId("tiny_vertical_mine_entrance"));
        assertTrue(ExpeditionStructureRegistry.isEnabledStructureId(ExpeditionStructureRegistry.TINY_VERTICAL_MINE_ENTRANCE.toString()));
    }
}
