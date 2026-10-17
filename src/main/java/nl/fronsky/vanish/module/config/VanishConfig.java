/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.config;

import nl.fronsky.vanish.logic.utils.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * Typed view of {@code config.yml}. Every value is read once and cached until the next {@link #reload()}.
 */
public final class VanishConfig {
    private final JavaPlugin plugin;

    private boolean readable;
    private boolean debug;
    private boolean soundEnabled;
    private Sound sound;
    private boolean damageDisabled;
    private boolean hungerDisabled;
    private boolean mobTargetDisabled;
    private boolean silentChestEnabled;
    private boolean silentEnderChestEnabled;
    private boolean pressurePlatesDisabled;
    private boolean deathMessagesDisabled;
    private boolean advancementsDisabled;
    private boolean playerPushDisabled;
    private boolean itemPickupDisabled;
    private boolean nightVisionEnabled;
    private boolean flightEnabled;
    private boolean actionBarNotificationEnabled;
    private boolean titleNotificationEnabled;
    private String pluginColorName;
    private BarColor barColor;
    private ChatColor accentColor;
    private boolean bossBarEnabled;
    private String bossBarTitle;
    private BarStyle bossBarStyle;

    public VanishConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    /**
     * Re-reads {@code config.yml} from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();
        // Bukkit returns an empty configuration when config.yml cannot be parsed; never write that back.
        readable = !config.getKeys(false).isEmpty();
        if (readable) {
            addMissingDefaults(config);
        } else {
            plugin.getLogger().warning("config.yml is empty or invalid; using the default settings.");
        }

        debug = config.getBoolean("debug-mode");
        soundEnabled = config.getBoolean("sound-enable");
        sound = parseSound(config.getString("sound"));
        damageDisabled = config.getBoolean("disabled-actions.damage");
        hungerDisabled = config.getBoolean("disabled-actions.hunger");
        mobTargetDisabled = config.getBoolean("disabled-actions.mob-target");
        silentChestEnabled = config.getBoolean("disabled-actions.silent-chest");
        silentEnderChestEnabled = config.getBoolean("disabled-actions.silent-ender-chest");
        pressurePlatesDisabled = config.getBoolean("disabled-actions.pressure-plates");
        deathMessagesDisabled = config.getBoolean("disabled-actions.death-messages");
        advancementsDisabled = config.getBoolean("disabled-actions.advancements");
        playerPushDisabled = config.getBoolean("disabled-actions.player-push");
        itemPickupDisabled = config.getBoolean("disabled-actions.pickup-items");
        nightVisionEnabled = config.getBoolean("vanish-effects.night-vision");
        flightEnabled = config.getBoolean("vanish-effects.allow-flight");
        actionBarNotificationEnabled = config.getBoolean("notifications.actionbar");
        titleNotificationEnabled = config.getBoolean("notifications.title");
        applyPluginColor(config.getString("plugin-color", BarColor.BLUE.name()));
        bossBarEnabled = config.getBoolean("bossbar.enabled");
        bossBarTitle = ColorUtil.colorize(config.getString("bossbar.title", "Vanish"));
        bossBarStyle = parseBossBarStyle(config.getString("bossbar.style"));
    }

    /**
     * Writes settings that are missing from the user's file (e.g. added by an update) back to {@code config.yml}.
     */
    private void addMissingDefaults(FileConfiguration config) {
        Configuration defaults = config.getDefaults();
        if (defaults == null || config.getKeys(true).containsAll(defaults.getKeys(true))) {
            return;
        }
        config.options().copyDefaults(true);
        plugin.saveConfig();
        plugin.getLogger().info("Added missing settings to config.yml.");
    }

    private void applyPluginColor(String name) {
        pluginColorName = name;
        accentColor = ColorUtil.parsePluginColor(name);
        try {
            barColor = BarColor.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid plugin-color '" + name + "', the boss bar uses BLUE instead.");
            barColor = BarColor.BLUE;
        }
    }

    private Sound parseSound(String name) {
        if (name != null && !name.isEmpty()) {
            try {
                return Sound.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound '" + name + "', using AMBIENT_CAVE instead.");
            }
        }
        return Sound.AMBIENT_CAVE;
    }

    private BarStyle parseBossBarStyle(String name) {
        if (name != null && !name.isEmpty()) {
            try {
                return BarStyle.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid bossbar.style '" + name + "', using SOLID instead.");
            }
        }
        return BarStyle.SOLID;
    }

    private void save(String path, Object value) {
        plugin.getConfig().set(path, value);
        if (readable) {
            plugin.saveConfig();
        } else {
            plugin.getLogger().warning("Not saving '" + path + "' because config.yml could not be read; "
                    + "the change lasts until the next restart.");
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        save("sound-enable", soundEnabled);
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
        save("sound", sound.name());
    }

    public boolean isDamageDisabled() {
        return damageDisabled;
    }

    public boolean isHungerDisabled() {
        return hungerDisabled;
    }

    public boolean isMobTargetDisabled() {
        return mobTargetDisabled;
    }

    public boolean isSilentChestEnabled() {
        return silentChestEnabled;
    }

    public boolean isSilentEnderChestEnabled() {
        return silentEnderChestEnabled;
    }

    public boolean isPressurePlatesDisabled() {
        return pressurePlatesDisabled;
    }

    public boolean isDeathMessagesDisabled() {
        return deathMessagesDisabled;
    }

    public boolean isAdvancementsDisabled() {
        return advancementsDisabled;
    }

    public boolean isPlayerPushDisabled() {
        return playerPushDisabled;
    }

    public boolean isItemPickupDisabled() {
        return itemPickupDisabled;
    }

    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    public boolean isFlightEnabled() {
        return flightEnabled;
    }

    public boolean isActionBarNotificationEnabled() {
        return actionBarNotificationEnabled;
    }

    public boolean isTitleNotificationEnabled() {
        return titleNotificationEnabled;
    }

    /**
     * @return the {@code plugin-color} value exactly as configured
     */
    public String getPluginColorName() {
        return pluginColorName;
    }

    public void setPluginColor(BarColor color) {
        applyPluginColor(color.name());
        save("plugin-color", color.name());
    }

    public BarColor getBarColor() {
        return barColor;
    }

    /**
     * @return the chat color used for headers, menu titles and other accents
     */
    public ChatColor getAccentColor() {
        return accentColor;
    }

    public boolean isBossBarEnabled() {
        return bossBarEnabled;
    }

    public String getBossBarTitle() {
        return bossBarTitle;
    }

    public BarStyle getBossBarStyle() {
        return bossBarStyle;
    }
}
