package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeNewChunkOreGuardSafetyTest {
    @Test
    void chunkLoadCallbackDefersSanitizationUntilServerTicks() throws IOException {
        String source = Files.readString(sourceFile(
                "src/main/java/com/oblixorprime/ioe/worldgen/IoeNewChunkOreGuard.java"
        ));
        String chunkLoadCallback = source.substring(
                source.indexOf("private static void onChunkLoad"),
                source.indexOf("private static void onServerTick")
        );

        assertFalse(chunkLoadCallback.contains("getServer().execute"));
        assertFalse(chunkLoadCallback.contains("sanitizeLoadedChunk("));
        assertTrue(chunkLoadCallback.contains("INITIAL_SANITIZATION_QUEUE.add(chunkKey)"));
        assertTrue(source.contains("SANITIZATIONS_PER_TICK = 1"));
        assertTrue(source.contains("ServerTickEvent.Post"));
        assertTrue(source.contains("INITIAL_SANITIZATION_QUEUE.clear()"));
    }

    private static Path sourceFile(String relativePath) {
        Path current = Path.of("").toAbsolutePath();
        for (int depth = 0; depth < 8 && current != null; depth++) {
            Path candidate = current.resolve(relativePath);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate " + relativePath);
    }
}
