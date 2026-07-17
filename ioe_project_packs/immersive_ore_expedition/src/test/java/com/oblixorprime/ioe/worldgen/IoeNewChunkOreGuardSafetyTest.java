package com.oblixorprime.ioe.worldgen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IoeNewChunkOreGuardSafetyTest {
    @Test
    void schedulerAlternatesWithoutRunningFinalAheadOfInitial() {
        assertFalse(IoeNewChunkOreGuard.shouldRunFinalSanitization(true, true, false));
        assertTrue(IoeNewChunkOreGuard.shouldRunFinalSanitization(true, true, true));
        assertTrue(IoeNewChunkOreGuard.shouldRunFinalSanitization(false, true, false));
        assertFalse(IoeNewChunkOreGuard.shouldRunFinalSanitization(true, false, true));
        assertFalse(IoeNewChunkOreGuard.isFinalSanitizationEligible(20, 20, true));
        assertTrue(IoeNewChunkOreGuard.isFinalSanitizationEligible(20, 20, false));
        assertFalse(IoeNewChunkOreGuard.isFinalSanitizationEligible(21, 20, false));
    }

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
        assertTrue(source.contains("SCHEDULED_SANITIZATIONS.contains(entry.getKey())"));
        assertTrue(source.contains("!PENDING_NEW_CHUNKS.contains(chunkKey)"));
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
