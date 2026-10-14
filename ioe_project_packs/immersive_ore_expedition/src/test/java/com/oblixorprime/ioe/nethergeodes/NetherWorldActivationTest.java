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
    @Test void twoNewWorldsNeverShareAnIdentity() throws Exception {
        var other=Files.createDirectory(root.resolve("other"));
        assertNotEquals(NetherWorldActivation.selectIdentity(root).id(),NetherWorldActivation.selectIdentity(other).id());
        assertFalse(NetherWorldActivation.PRODUCTION_ENABLED);
    }
}
