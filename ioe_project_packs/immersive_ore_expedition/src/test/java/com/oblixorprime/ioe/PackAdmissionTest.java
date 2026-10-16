package com.oblixorprime.ioe;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/** Exercise the Maven version semantics used by NeoForge, including gaps between qualified versions. */
class PackAdmissionTest {
    @Test
    void admitsQualifiedVersionsAndRejectsUnqualifiedGaps() throws Exception {
        var properties = new Properties();
        try (var input = Files.newBufferedReader(Path.of("gradle.properties"))) {
            properties.load(input);
        }
        check(properties, "ae2cs_version_range", new String[]{"1.1.12", "1.2.1"},
                new String[]{"1.1.11", "1.2.0", "1.2.2", "2.0.0"});
        check(properties, "domum_version_range", new String[]{"1.0.231", "1.0.236-snapshot"},
                new String[]{"1.0.230", "1.0.232", "1.0.234-snapshot", "1.0.235-snapshot", "1.0.237-snapshot"});
        check(properties, "extendedae_version_range", new String[]{"1.21-2.2.33-neoforge", "1.21-2.2.41-neoforge"},
                new String[]{"1.21-2.2.32-neoforge", "1.21-2.2.34-neoforge", "1.21-2.2.40-neoforge", "1.21-2.2.42-neoforge"});
        check(properties, "jade_version_range", new String[]{"15.10.6"}, new String[]{"15.10.5", "16"});
        check(properties, "ae2_version_range", new String[]{"19.2.17", "19.2.18"}, new String[]{"19.2.16", "20"});
        check(properties, "geore_version_range", new String[]{"6.2.2", "6.2.4"}, new String[]{"6.2.1", "7"});
    }

    private static void check(Properties properties, String key, String[] admitted, String[] rejected) throws Exception {
        String spec = properties.getProperty(key);
        assertNotNull(spec, key);
        var range = VersionRange.createFromVersionSpec(spec);
        for (String version : admitted)
            assertTrue(range.containsVersion(new DefaultArtifactVersion(version)), key + " must admit " + version);
        for (String version : rejected)
            assertFalse(range.containsVersion(new DefaultArtifactVersion(version)), key + " must reject " + version);
    }
}
