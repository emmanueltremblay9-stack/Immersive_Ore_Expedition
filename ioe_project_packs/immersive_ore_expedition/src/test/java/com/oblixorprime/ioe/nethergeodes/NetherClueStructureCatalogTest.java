package com.oblixorprime.ioe.nethergeodes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NetherClueStructureCatalogTest {
    @Test
    void catalogsExpectedLavaShoreClueIds() {
        assertEquals(4, NetherClueStructureCatalog.enabledClueIds().size());
        assertTrue(NetherClueStructureCatalog.enabledClueIds().contains(NetherClueStructureCatalog.LAVA_SHORE_MARKER));
        assertTrue(NetherClueStructureCatalog.enabledClueIds().contains(NetherClueStructureCatalog.ASHEN_QUARTZ_RUBBLE));
    }
}
