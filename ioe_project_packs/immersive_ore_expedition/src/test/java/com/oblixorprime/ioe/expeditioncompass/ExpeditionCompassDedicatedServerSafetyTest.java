package com.oblixorprime.ioe.expeditioncompass;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExpeditionCompassDedicatedServerSafetyTest {
    private static final byte[] MINECRAFT_CLIENT_REFERENCE =
            "net/minecraft/client".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] COMPASS_CLIENT_PACKAGE_REFERENCE =
            "com/oblixorprime/ioe/expeditioncompass/client/".getBytes(StandardCharsets.ISO_8859_1);

    @Test
    void compiledCommonClassesDoNotDirectlyReferenceClientOnlyClasses() throws IOException {
        Path classesRoot = classesRoot();
        assertTrue(Files.isDirectory(classesRoot), () -> "Missing compiled classes root " + classesRoot);

        List<Path> unsafeClasses;
        try (var stream = Files.walk(classesRoot)) {
            unsafeClasses = stream
                    .filter(path -> path.getFileName().toString().endsWith(".class"))
                    .filter(path -> !isClientPackageClass(classesRoot, path))
                    .filter(ExpeditionCompassDedicatedServerSafetyTest::containsClientOnlyReference)
                    .toList();
        }

        assertTrue(
                unsafeClasses.isEmpty(),
                () -> "Common classes contain direct client-only references: " + unsafeClasses
        );
    }

    @Test
    void commonSourcesDoNotImportClientOnlyPackages() throws IOException {
        Path sourcesRoot = sourcesRoot();
        assertTrue(Files.isDirectory(sourcesRoot), () -> "Missing source root " + sourcesRoot);

        List<Path> unsafeSources;
        try (var stream = Files.walk(sourcesRoot)) {
            unsafeSources = stream
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !isClientPackageSource(sourcesRoot, path))
                    .filter(ExpeditionCompassDedicatedServerSafetyTest::importsClientOnlyPackage)
                    .toList();
        }

        assertTrue(
                unsafeSources.isEmpty(),
                () -> "Common sources import client-only packages: " + unsafeSources
        );
    }

    private static boolean containsClientOnlyReference(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return contains(bytes, MINECRAFT_CLIENT_REFERENCE) || contains(bytes, COMPASS_CLIENT_PACKAGE_REFERENCE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect compiled class " + path, exception);
        }
    }

    private static boolean importsClientOnlyPackage(Path path) {
        try {
            String source = Files.readString(path);
            return source.contains("import net.minecraft.client")
                    || source.contains("import com.oblixorprime.ioe.expeditioncompass.client");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect source " + path, exception);
        }
    }

    private static boolean contains(byte[] haystack, byte[] needle) {
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            int matchIndex = 0;
            while (matchIndex < needle.length && haystack[i + matchIndex] == needle[matchIndex]) {
                matchIndex++;
            }
            if (matchIndex == needle.length) {
                return true;
            }
        }
        return false;
    }

    private static boolean isClientPackageClass(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/').contains("/client/");
    }

    private static boolean isClientPackageSource(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/').contains("/client/");
    }

    private static Path classesRoot() {
        Path moduleRoot = Path.of("build", "classes", "java", "main");
        if (Files.isDirectory(moduleRoot)) {
            return moduleRoot;
        }
        return Path.of(
                "ioe_project_packs",
                "immersive_ore_expedition",
                "build",
                "classes",
                "java",
                "main"
        );
    }

    private static Path sourcesRoot() {
        Path moduleRoot = Path.of("src", "main", "java");
        if (Files.isDirectory(moduleRoot)) {
            return moduleRoot;
        }
        return Path.of("ioe_project_packs", "immersive_ore_expedition", "src", "main", "java");
    }
}
