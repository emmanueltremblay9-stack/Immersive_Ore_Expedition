package com.oblixorprime.ioe.nethergeodes;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

final class NetherWorldActivationTest {
    @TempDir Path root;
    @Test void newWorldIdentityIsPersistentAndNeverRecreatedOnReopen() throws Exception {
        var first=NetherWorldActivation.selectIdentity(root);
        byte[] original=Files.readAllBytes(root.resolve(NetherWorldActivation.MANIFEST));
        Files.writeString(root.resolve("level.dat"),"existing world");
        assertEquals(first,NetherWorldActivation.selectIdentity(root));
        assertArrayEquals(original,Files.readAllBytes(root.resolve(NetherWorldActivation.MANIFEST)));
        assertEquals(32,first.depth());assertEquals(1,first.schema());
    }
    @Test void oldSaveAndOrphanChunksNeverGainIdentity() throws Exception {
        for(String file:new String[]{"level.dat","level.dat_old","DIM-1/region/r.0.0.mca"}) {
            var world=Files.createTempDirectory(root,"old");
            var path=world.resolve(file);Files.createDirectories(path.getParent());Files.writeString(path,"old");
            assertNull(NetherWorldActivation.selectIdentity(world));
            assertFalse(Files.exists(world.resolve(NetherWorldActivation.MANIFEST)));
        }
    }
    @Test void malformedOrUnknownIdentityFailsClosedWithoutReplacement() throws Exception {
        var marker=root.resolve(NetherWorldActivation.MANIFEST);
        for(String value:new String[]{"broken", "{\"schema\":2}", "{}"}) {
            Files.writeString(marker,value);
            assertThrows(java.io.IOException.class,()->NetherWorldActivation.selectIdentity(root));
            assertEquals(value,Files.readString(marker));
        }
    }
    @Test void boundOldSaveCannotSelectPackEvenWithInitModeOrEditedPackList() throws Exception {
        Files.writeString(root.resolve("level.dat"),"old save");
        String previous=System.getProperty("ioe.depth32Prototype");
        try {
            System.setProperty("ioe.depth32Prototype","true");
            var repository=new net.minecraft.server.packs.repository.PackRepository();
            NetherWorldActivation.bind(repository,root);
            NetherWorldActivation.beforeRegistries(repository,net.minecraft.world.level.WorldDataConfiguration.DEFAULT,true,false);
            assertTrue(repository.getAvailableIds().isEmpty());
            assertFalse(Files.exists(root.resolve(NetherWorldActivation.MANIFEST)));
            var selected=new net.minecraft.world.level.WorldDataConfiguration(
                    new net.minecraft.world.level.DataPackConfig(java.util.List.of(NetherWorldActivation.PACK_ID),java.util.List.of()),
                    net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
            assertThrows(IllegalStateException.class,()->NetherWorldActivation.beforeRegistries(repository,selected,true,false));
            assertThrows(IllegalStateException.class,()->NetherWorldActivation.beforeRegistries(repository,selected,false,false));
        } finally {
            if(previous==null)System.clearProperty("ioe.depth32Prototype");else System.setProperty("ioe.depth32Prototype",previous);
        }
    }
    @Test void twoNewWorldsNeverShareAnIdentity() throws Exception {
        var other=Files.createDirectory(root.resolve("other"));
        assertNotEquals(NetherWorldActivation.selectIdentity(root).id(),NetherWorldActivation.selectIdentity(other).id());
        assertFalse(NetherWorldActivation.PRODUCTION_ENABLED);
    }
}
