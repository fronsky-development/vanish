/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.utils;

import lombok.Getter;
import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.logic.file.YmlFile;
import nl.fronsky.vanish.logic.file.interfaces.IFile;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.module.events.DisabledActions;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Data {
    private final Plugin plugin;
    private final IFile<FileConfiguration> config, messages, players;
    private final Map<UUID, VanishPlayer> vanishedPlayers;
    private final Map<UUID, Boolean> advancementAnnouncementRules;
    private final boolean advancementMessageApiAvailable;
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
        advancementAnnouncementRules = new ConcurrentHashMap<>();
        advancementMessageApiAvailable = detectAdvancementMessageApi();

        // Load configuration and validate
        validateConfiguration();

        // Enable debug mode if configured
        boolean debugMode = config.get().getBoolean("debug-mode", false);
        Logger.setDebugEnabled(debugMode);
        if (debugMode) {
            Logger.info("Debug mode enabled!");
        }

        BarColor barColor = getBarColor(config.get().getString("plugin-color"));
        BarStyle barStyle = getBarStyle(config.get().getString("bossbar.style"));
        String barTitle = ColorUtil.colorize(config.get().getString("bossbar.title", "Vanish"));
        vanishedBossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);

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

        if (useAdvancementGameruleFallback()) {
            Logger.debug("Advancement suppression: using global gamerule fallback (no Paper message API or ProtocolLib detected).");
        } else {
            Logger.debug("Advancement suppression: using per-player mode.");
        }
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
        if (!cfg.contains("bossbar.enabled")) {
            cfg.set("bossbar.enabled", true);
            modified = true;
        }
        if (!cfg.contains("bossbar.title")) {
            cfg.set("bossbar.title", "&bYou are currently vanished");
            modified = true;
        }
        if (!cfg.contains("bossbar.style")) {
            cfg.set("bossbar.style", "SOLID");
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
        if (!cfg.contains("disabled-actions.advancements")) {
            cfg.set("disabled-actions.advancements", true);
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
        if (!cfg.contains("vanish-effects.night-vision")) {
            cfg.set("vanish-effects.night-vision", true);
            modified = true;
        }
        if (!cfg.contains("vanish-effects.allow-flight")) {
            cfg.set("vanish-effects.allow-flight", true);
            modified = true;
        }
        if (!cfg.contains("notifications.actionbar")) {
            cfg.set("notifications.actionbar", true);
            modified = true;
        }
        if (!cfg.contains("notifications.title")) {
            cfg.set("notifications.title", true);
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

        updateBossBar();

        // Refresh cached disabled-action settings (BUG-1 fix)
        if (disabledActions != null) {
            disabledActions.reloadConfig();
        }
        updateAdvancementAnnouncements();

        Logger.info("Configurations reloaded successfully!");
    }

    /**
     * Applies the advancement-announcement suppression strategy.
     * <p>
     * When a per-player mechanism is available (Paper's advancement message API or ProtocolLib)
     * this is a no-op, since suppression then happens per vanished player. Only on a plain Spigot
     * server without ProtocolLib do we fall back to toggling the world {@code announceAdvancements}
     * gamerule, which reliably suppresses vanilla and datapack advancements for everyone while at
     * least one player is vanished.
     */
    public void updateAdvancementAnnouncements() {
        if (!useAdvancementGameruleFallback()) {
            return;
        }

        if (!config.get().getBoolean("disabled-actions.advancements", true) || vanishedPlayers.isEmpty()) {
            restoreAdvancementAnnouncements();
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            advancementAnnouncementRules.putIfAbsent(world.getUID(),
                    Boolean.TRUE.equals(world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS)));
            if (Boolean.TRUE.equals(world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS))) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            }
        }
    }

    /**
     * Restores any world advancement-announcement gamerules changed by
     * {@link #updateAdvancementAnnouncements()}.
     */
    public void restoreAdvancementAnnouncements() {
        for (Map.Entry<UUID, Boolean> entry : advancementAnnouncementRules.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world != null) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, entry.getValue());
            }
        }
        advancementAnnouncementRules.clear();
    }

    /**
     * @return {@code true} when neither Paper's advancement message API nor ProtocolLib is
     * available, so the global gamerule is the only reliable way to suppress advancement
     * announcements for vanished players.
     */
    public boolean useAdvancementGameruleFallback() {
        return !advancementMessageApiAvailable && protocolLib == null;
    }

    private static boolean detectAdvancementMessageApi() {
        for (Method method : PlayerAdvancementDoneEvent.class.getMethods()) {
            if (method.getName().equals("message") && method.getParameterCount() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the color of the vanishedBossBar.
     */
    public void updateBarColor() {
        vanishedBossBar.setColor(getBarColor(config.get().getString("plugin-color")));
    }

    /**
     * Applies the configured title, color, style and enabled state to the vanish boss bar.
     */
    public void updateBossBar() {
        FileConfiguration cfg = config.get();
        vanishedBossBar.setColor(getBarColor(cfg.getString("plugin-color")));
        vanishedBossBar.setStyle(getBarStyle(cfg.getString("bossbar.style")));
        vanishedBossBar.setTitle(ColorUtil.colorize(cfg.getString("bossbar.title", "Vanish")));

        if (isBossBarEnabled()) {
            for (VanishPlayer vanishPlayer : vanishedPlayers.values()) {
                vanishedBossBar.addPlayer(vanishPlayer.getPlayer());
            }
        } else {
            vanishedBossBar.removeAll();
        }
    }

    /**
     * Returns whether the vanish boss bar is enabled.
     *
     * @return {@code true} if the boss bar should be shown to vanished players
     */
    public boolean isBossBarEnabled() {
        return config.get().getBoolean("bossbar.enabled", true);
    }

    /**
     * Returns whether the vanish sound effect is enabled.
     *
     * @return {@code true} if the sound is enabled
     */
    public boolean isSoundEnabled() {
        return config.get().getBoolean("sound-enable");
    }

    /**
     * Returns the configured vanish sound effect.
     *
     * @return the configured {@link org.bukkit.Sound}, or {@code AMBIENT_CAVE} as fallback
     */
    public org.bukkit.Sound getSound() {
        String soundName = config.get().getString("sound");
        if (soundName != null) {
            try {
                return org.bukkit.Sound.valueOf(soundName);
            } catch (IllegalArgumentException e) {
                Logger.warning("Invalid sound '" + soundName + "', using AMBIENT_CAVE as default.");
            }
        }
        return org.bukkit.Sound.AMBIENT_CAVE;
    }

    /**
     * Returns the configured plugin accent color as a {@link org.bukkit.ChatColor}.
     * Handles PINK→LIGHT_PURPLE and PURPLE→DARK_PURPLE mapping.
     *
     * @return the configured chat color, or {@code BLUE} as fallback
     */
    public org.bukkit.ChatColor getPluginChatColor() {
        return ColorUtil.parsePluginColor(config.get().getString("plugin-color"));
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
     * Retrieves a BarStyle based on the provided key.
     *
     * @param key the key to look up the BarStyle
     * @return the corresponding BarStyle, or SOLID if the key is invalid or not provided
     */
    private BarStyle getBarStyle(String key) {
        BarStyle barStyle = BarStyle.SOLID;
        if (key != null && !key.isEmpty()) {
            try {
                barStyle = BarStyle.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException exception) {
                Logger.warning("Invalid bar style '" + key + "', using SOLID as default.");
                Logger.debug(exception.getMessage());
            }
        }
        return barStyle;
    }

    /**
     * Cleans up resources when the plugin is disabled.
     */
    public void cleanup() {
        restoreAdvancementAnnouncements();
        if (protocolLib != null) {
            protocolLib.cleanup();
        }
    }
}
