package io.github.slimefunguguproject.bump.build;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BuildArtifactTest {

    @Test
    void shadowJarIsTheDefaultServerArtifact() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(buildScript.contains("archiveClassifier = ''"));
        assertTrue(buildScript.contains("archiveClassifier = 'plain'"));
    }
}
