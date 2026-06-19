package io.github.slimefunguguproject.bump.utils.constant;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class KeysTest {

    @Test
    void appraiseVersionUsesSeparatePersistentDataKey() throws IOException {
        String source = Files.readString(Path.of("src/main/java/io/github/slimefunguguproject/bump/utils/constant/Keys.java"));

        assertNotEquals(keyLiteral(source, "APPRAISE_LEVEL"), keyLiteral(source, "APPRAISE_VERSION"));
    }

    @Test
    void appraiseModifiersUsesSeparatePersistentDataKey() throws IOException {
        String source = Files.readString(Path.of("src/main/java/io/github/slimefunguguproject/bump/utils/constant/Keys.java"));

        assertNotEquals(keyLiteral(source, "APPRAISE_LEVEL"), keyLiteral(source, "APPRAISE_MODIFIERS"));
        assertNotEquals(keyLiteral(source, "APPRAISE_VERSION"), keyLiteral(source, "APPRAISE_MODIFIERS"));
    }

    private static String keyLiteral(String source, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "\\s*=\\s*Bump\\.createKey\\(\"([^\"]+)\"\\)");
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            throw new AssertionError("Could not find key literal for " + fieldName);
        }
        return matcher.group(1);
    }
}
