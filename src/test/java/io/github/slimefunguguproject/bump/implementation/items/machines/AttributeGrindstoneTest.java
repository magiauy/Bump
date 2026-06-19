package io.github.slimefunguguproject.bump.implementation.items.machines;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AttributeGrindstoneTest {

    @Test
    void legacyModifierRemovalRemovesAllModifiers() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        int versionCheck = source.indexOf("if (version == 1)");
        int legacyRemoval = source.indexOf("removeAllModifiers(meta)");

        assertTrue(legacyRemoval >= 0);
        assertTrue(legacyRemoval > versionCheck);
    }

    @Test
    void legacyModifierRemovalStillRemovesNamedAppraisalModifiers() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        int versionCheck = source.indexOf("if (version == 1)");
        int namedRemoval = source.indexOf("removeAppraisalModifiers(meta)");

        assertTrue(namedRemoval >= 0);
        assertTrue(namedRemoval < versionCheck);
    }

    @Test
    void currentModifierRemovalRemovesUuidNamedModifiers() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        int elseBranch = source.indexOf("} else {", source.indexOf("if (version == 1)"));
        int uuidRemoval = source.indexOf("removeUuidNamedModifiers(meta)");

        assertTrue(uuidRemoval >= 0);
        assertTrue(uuidRemoval > elseBranch);
    }

    @Test
    void currentModifierRemovalPrefersStoredModifierIds() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        int markedRemoval = source.indexOf("removeMarkedModifiers(meta, modifierIds)");
        int uuidFallback = source.indexOf("removeUuidNamedModifiers(meta)");

        assertTrue(markedRemoval >= 0);
        assertTrue(uuidFallback >= 0);
        assertTrue(markedRemoval < uuidFallback);
    }

    @Test
    void grindstoneClearsStoredModifierIds() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        assertTrue(source.contains("PersistentDataAPI.remove(meta, Keys.APPRAISE_MODIFIERS)"));
    }

    @Test
    void grindstoneRestoresDefaultAttributesWhenCustomModifiersAreGone() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        assertTrue(source.contains("restoreDefaultAttributesIfEmpty(meta)"));
        assertTrue(source.contains("meta.setAttributeModifiers(null)"));
    }
}
