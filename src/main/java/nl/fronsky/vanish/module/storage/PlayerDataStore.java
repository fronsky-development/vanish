/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Per-player settings backed by {@code players.yml}.
 * <p>
 * Values are cached in memory, so lookups never touch the disk and are safe from any thread
 * (event handlers, packet handlers, PlaceholderAPI). Writes and reloads run in submission order
 * on a single background thread, so the main thread never blocks on file I/O.
 */
public final class PlayerDataStore {
    private static final String SILENT_KEY = "silent";
    private static final boolean DEFAULT_SILENT_JOIN = true;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final File file;
    private final Logger logger;
    private final Map<UUID, Boolean> silentJoin = new ConcurrentHashMap<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Vanish-PlayerData");
        thread.setDaemon(true);
        return thread;
    });

    public PlayerDataStore(File file, Logger logger) {
        this.file = file;
        this.logger = logger;

        // Read once while the plugin enables, before any player can join.
        YamlConfiguration yaml = read();
        if (yaml != null) {
            silentJoin.putAll(parse(yaml));
        }
    }

    /**
     * Returns whether the player joins silently when auto-vanished. Never blocks.
     *
     * @param uuid the UUID of the player
     * @return the stored preference, or {@code true} when none is stored
     */
    public boolean isSilentJoin(UUID uuid) {
        return silentJoin.getOrDefault(uuid, DEFAULT_SILENT_JOIN);
    }

    /**
     * Updates the silent join preference in memory immediately and persists it asynchronously.
     *
     * @param uuid   the UUID of the player
     * @param silent the new preference
     */
    public void setSilentJoin(UUID uuid, boolean silent) {
        silentJoin.put(uuid, silent);
        ioExecutor.execute(() -> {
            // A reload queued before this write may have replaced the cached value.
            silentJoin.put(uuid, silent);

            YamlConfiguration yaml = read();
            if (yaml == null) {
                return;
            }
            yaml.set(uuid + "." + SILENT_KEY, silent);
            try {
                yaml.save(file);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save " + file.getName(), e);
            }
        });
    }

    /**
     * Re-reads the file in the background and refreshes the cache.
     */
    public void reload() {
        ioExecutor.execute(() -> {
            YamlConfiguration yaml = read();
            if (yaml == null) {
                return;
            }
            Map<UUID, Boolean> loaded = parse(yaml);
            silentJoin.keySet().retainAll(loaded.keySet());
            silentJoin.putAll(loaded);
        });
    }

    /**
     * Lets pending writes finish and stops the background thread. Call once when the plugin is disabled.
     */
    public void shutdown() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warning("Timed out while saving " + file.getName()
                        + "; recent changes may not have been written.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Reads the backing file.
     *
     * @return the parsed file, an empty configuration if the file does not exist, or {@code null}
     * if it could not be read (so callers never overwrite a file they failed to parse)
     */
    private YamlConfiguration read() {
        YamlConfiguration yaml = new YamlConfiguration();
        if (!file.exists()) {
            return yaml;
        }

        try {
            yaml.load(file);
            return yaml;
        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to read " + file.getName(), e);
            return null;
        }
    }

    private static Map<UUID, Boolean> parse(YamlConfiguration yaml) {
        Map<UUID, Boolean> values = new HashMap<>();
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null || !section.isBoolean(SILENT_KEY)) {
                continue;
            }

            try {
                values.put(UUID.fromString(key), section.getBoolean(SILENT_KEY));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return values;
    }
}
