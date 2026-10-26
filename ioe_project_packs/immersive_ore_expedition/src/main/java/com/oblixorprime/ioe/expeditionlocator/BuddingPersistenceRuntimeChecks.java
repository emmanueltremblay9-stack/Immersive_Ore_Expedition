package com.oblixorprime.ioe.expeditionlocator;

import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;

/** GameTest support: exercise the real world data file and rebuild its in-memory index. */
public final class BuddingPersistenceRuntimeChecks {
    private BuddingPersistenceRuntimeChecks() {
    }

    public static void reloadFromDisk(ServerLevel level) throws IOException {
        var storage = level.getServer().overworld().getDataStorage();
        var before = ExpeditionLocatorService.index(level);
        storage.save();
        net.neoforged.neoforge.common.IOUtilities.waitUntilIOWorkerComplete();
        var saved = storage.readTagFromDisk(ExpeditionLocatorSavedData.STORAGE_NAME, null,
                SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var loaded = ExpeditionLocatorSavedData.FACTORY.deserializer()
                .apply(saved.getCompound("data"), level.registryAccess());
        if (loaded.index() == before || !loaded.index().diagnosticSites().equals(before.diagnosticSites())) {
            throw new AssertionError("Budding site data changed across the world-data disk round trip");
        }
        storage.set(ExpeditionLocatorSavedData.STORAGE_NAME, loaded);
    }
}
