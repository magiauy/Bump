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
        int legacyRemoval = source.indexOf("removeAllModifiers(meta)", versionCheck);

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

    @Test
    void grindstoneHasConfigGatedFullClearPlayers() throws IOException {
        String config = Files.readString(Path.of("src/main/resources/config.yml"));
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        assertTrue(config.contains("attribute-grindstone:"));
        assertTrue(config.contains("full-clear-players:"));
        assertTrue(config.contains("- Magiauy_VN"));
        assertTrue(source.contains("\"attribute-grindstone.full-clear-players\""));
    }

    @Test
    void shiftClickFullClearIsOnlyAvailableForConfiguredPlayers() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        int shiftCheck = source.indexOf("action.isShiftClicked()");
        int permissionCheck = source.indexOf("canFullClear(p)", shiftCheck);
        int fullClear = source.indexOf("clearAttributes(output, clearAll)", permissionCheck);

        assertTrue(shiftCheck >= 0);
        assertTrue(permissionCheck > shiftCheck);
        assertTrue(fullClear > permissionCheck);
    }

    @Test
    void normalClickKeepsCurrentModifierRemoval() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));

        assertTrue(source.contains("action.isShiftClicked() && canFullClear(p)"));
        assertTrue(source.contains("if (clearAll)"));
        assertTrue(source.contains("removeModifiers(meta, version)"));
    }

    @Test
    void grindButtonLoreMentionsShiftClickOnlyThroughDynamicItem() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));
        String lang = Files.readString(Path.of("src/main/resources/lang/en-US.yml"));

        assertTrue(source.contains("getOperationSlotItem(@Nonnull Player p)"));
        assertTrue(source.contains("gui.grind.full-clear-lore"));
        assertTrue(lang.contains("full-clear-lore:"));
    }

    @Test
    void fullClearUsesSeparateSuccessMessage() throws IOException {
        String source = Files.readString(Path.of(
            "src/main/java/io/github/slimefunguguproject/bump/implementation/items/machines/AttributeGrindstone.java"
        ));
        String lang = Files.readString(Path.of("src/main/resources/lang/en-US.yml"));

        assertTrue(source.contains("machine.attribute-grindstone.success-full-clear"));
        assertTrue(source.contains("Bump.getLocalization().sendMessage(p, clearAll"));
        assertTrue(lang.contains("success-full-clear:"));
    }
}
