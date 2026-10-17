/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataStoreTest {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    @TempDir
    Path directory;

    private File file;
    private final UUID player = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        LOGGER.setUseParentHandlers(false);
        file = directory.resolve("players.yml").toFile();
    }

    @Test
    void defaultsToSilentJoinWithoutCreatingTheFile() {
        PlayerDataStore store = new PlayerDataStore(file, LOGGER);

        assertTrue(store.isSilentJoin(player));
        store.shutdown();
        assertFalse(file.exists());
    }

    @Test
    void appliesChangesImmediatelyAndPersistsThem() {
        PlayerDataStore store = new PlayerDataStore(file, LOGGER);
        store.setSilentJoin(player, false);

        assertFalse(store.isSilentJoin(player));
        store.shutdown();
        assertFalse(new PlayerDataStore(file, LOGGER).isSilentJoin(player));
    }

    @Test
    void keepsUnrelatedEntriesWhenSaving() throws IOException {
        Files.writeString(file.toPath(), "someSetting: 1\n" + player + ":\n  silent: true\n  note: keep\n");

        PlayerDataStore store = new PlayerDataStore(file, LOGGER);
        store.setSilentJoin(player, false);
        store.shutdown();

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("someSetting: 1"));
        assertTrue(content.contains("note: keep"));
        assertTrue(content.contains("silent: false"));
    }

    @Test
    void reloadPicksUpChangesMadeOutsideThePlugin() throws IOException {
        UUID other = UUID.randomUUID();
        Files.writeString(file.toPath(), player + ":\n  silent: false\n" + other + ":\n  silent: false\n");
        PlayerDataStore store = new PlayerDataStore(file, LOGGER);

        Files.writeString(file.toPath(), player + ":\n  silent: true\n");
        store.reload();
        store.shutdown();

        assertTrue(store.isSilentJoin(player));
        assertTrue(store.isSilentJoin(other));
    }

    @Test
    void reloadQueuedBeforeAChangeDoesNotUndoIt() {
        PlayerDataStore store = new PlayerDataStore(file, LOGGER);

        store.reload();
        store.setSilentJoin(player, false);
        store.shutdown();

        assertFalse(store.isSilentJoin(player));
        assertFalse(new PlayerDataStore(file, LOGGER).isSilentJoin(player));
    }

    @Test
    void neverOverwritesAFileItCannotRead() throws IOException {
        String invalid = "this: is: not: valid: yaml: [\n";
        Files.writeString(file.toPath(), invalid);

        PlayerDataStore store = new PlayerDataStore(file, LOGGER);
        store.setSilentJoin(player, false);
        store.shutdown();

        assertFalse(store.isSilentJoin(player));
        assertEquals(invalid, Files.readString(file.toPath()));
    }
}
