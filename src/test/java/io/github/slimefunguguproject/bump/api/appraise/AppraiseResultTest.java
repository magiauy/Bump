package io.github.slimefunguguproject.bump.api.appraise;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AppraiseResultTest {

    @Test
    void applyStoresAddedModifierIds() throws IOException {
        String source = Files.readString(Path.of("src/main/java/io/github/slimefunguguproject/bump/api/appraise/AppraiseResult.java"));

        assertTrue(source.contains("List<String> modifierIds = new ArrayList<>()"));
        assertTrue(source.contains("modifierIds.add(uuid.toString())"));
        assertTrue(source.contains("PersistentDataAPI.setString(meta, Keys.APPRAISE_MODIFIERS, String.join(\",\", modifierIds))"));
    }
}
