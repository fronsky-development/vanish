/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.utils;

import lombok.Getter;
import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.logic.file.YmlFile;
import nl.fronsky.vanish.logic.file.interfaces.IFile;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.events.DisabledActions;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Data {
    private final Plugin plugin;
    private final IFile<FileConfiguration> config, messages, players;
    private final Map<UUID, VanishPlayer> vanishedPlayers;
    private final BossBar vanishedBossBar;
    private final ProtocolLib protocolLib;

    @lombok.Setter
    private DisabledActions disabledActions;

    public Data() {
        plugin = Main.getInstance();
        config = new YmlFile("config");
        messages = new YmlFile("messages");
        players = new YmlFile("players");
        vanishedPlayers = new ConcurrentHashMap<>();

        // Load configuration and validate
        validateConfiguration();

        // Enable debug mode if configured
        boolean debugMode = config.get().getBoolean("debug-mode", false);
        Logger.setDebugEnabled(debugMode);
        if (debugMode) {
            Logger.info("Debug mode enabled!");
        }

        BarColor barColor = getBarColor(config.get().getString("plugin-color"));
        vanishedBossBar = Bukkit.createBossBar("Vanish", barColor, BarStyle.SOLID);

        ProtocolLib protocolLib = null;
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                protocolLib = new ProtocolLib();
                protocolLib.enable(this);
                Logger.info("ProtocolLib integration enabled (v5.4.0)");
            } catch (Exception e) {
                Logger.exception("Failed to initialize ProtocolLib integration", e);
            }
        }
        this.protocolLib = protocolLib;
    }

    /**
     * Validates the configuration file and sets defaults for missing values.
     */
    private void validateConfiguration() {
        FileConfiguration cfg = config.get();
        boolean modified = false;

        // Validate and set defaults
        if (!cfg.contains("debug-mode")) {
            cfg.set("debug-mode", false);
            modified = true;
        }
        if (!cfg.contains("sound-enable")) {
            cfg.set("sound-enable", true);
            modified = true;
        }
        if (!cfg.contains("sound")) {
            cfg.set("sound", "AMBIENT_CAVE");
            modified = true;
        }
        if (!cfg.contains("plugin-color")) {
            cfg.set("plugin-color", "BLUE");
            modified = true;
        }
        if (!cfg.contains("disabled-actions.damage")) {
            cfg.set("disabled-actions.damage", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.hunger")) {
            cfg.set("disabled-actions.hunger", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.mob-target")) {
            cfg.set("disabled-actions.mob-target", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.silent-chest")) {
            cfg.set("disabled-actions.silent-chest", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.silent-ender-chest")) {
            cfg.set("disabled-actions.silent-ender-chest", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.pressure-plates")) {
            cfg.set("disabled-actions.pressure-plates", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.death-messages")) {
            cfg.set("disabled-actions.death-messages", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.player-push")) {
            cfg.set("disabled-actions.player-push", true);
            modified = true;
        }
        if (!cfg.contains("disabled-actions.pickup-items")) {
            cfg.set("disabled-actions.pickup-items", true);
            modified = true;
        }

        if (modified) {
            config.save();
            Logger.info("Configuration file updated with missing defaults.");
        }
    }

    /**
     * Reloads all configuration files.
     */
    public void reloadConfigurations() {
        config.reload();
        messages.reload();
        players.reload();
        validateConfiguration();

        boolean debugMode = config.get().getBoolean("debug-mode", false);
        Logger.setDebugEnabled(debugMode);

        updateBarColor();

        // Refresh cached disabled-action settings (BUG-1 fix)
        if (disabledActions != null) {
            disabledActions.reloadConfig();
        }

        Logger.info("Configurations reloaded successfully!");
    }

    /**
     * Updates the color of the vanishedBossBar.
     */
    public void updateBarColor() {
        vanishedBossBar.setColor(getBarColor(config.get().getString("plugin-color")));
    }

    /**
     * Retrieves a BarColor based on the provided key.
     *
     * @param key the key to look up the BarColor
     * @return the corresponding BarColor, or BLUE if the key is invalid or not provided
     */
    private BarColor getBarColor(String key) {
        BarColor barColor = BarColor.BLUE;
        if (key != null && !key.isEmpty()) {
            try {
                barColor = BarColor.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException exception) {
                Logger.warning("Invalid bar color '" + key + "', using BLUE as default.");
                Logger.debug(exception.getMessage());
            }
        }
        return barColor;
    }

    /**
     * Cleans up resources when the plugin is disabled.
     */
    public void cleanup() {
        if (protocolLib != null) {
            protocolLib.cleanup();
        }
        MetaData.clearCache();
    }
}
