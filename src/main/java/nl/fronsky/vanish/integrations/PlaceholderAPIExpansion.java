/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.storage.PlayerDataStore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * PlaceholderAPI expansion providing the {@code %vanish_...%} placeholders. Requests can arrive on any thread,
 * so only thread-safe lookups are used.
 */
public final class PlaceholderAPIExpansion extends PlaceholderExpansion {
    private final JavaPlugin plugin;
    private final VanishConfig config;
    private final VanishManager vanishManager;
    private final PlayerDataStore playerData;

    public PlaceholderAPIExpansion(JavaPlugin plugin, VanishConfig config, VanishManager vanishManager,
                                   PlayerDataStore playerData) {
        this.plugin = plugin;
        this.config = config;
        this.vanishManager = vanishManager;
        this.playerData = playerData;
    }

    @Override
    public String getIdentifier() {
        return "vanish";
    }

    @Override
    public String getAuthor() {
        return "Fronsky";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return switch (params.toLowerCase(Locale.ROOT)) {
            case "isvanished" -> String.valueOf(isVanished(player));
            case "status" -> isVanished(player) ? "Vanished" : "Visible";
            case "count" -> String.valueOf(vanishManager.getVanishedCount());
            case "cansee" -> String.valueOf(canSeeVanished(player));
            case "total_online" ->
                    String.valueOf(Math.max(0, Bukkit.getOnlinePlayers().size() - vanishManager.getVanishedCount()));
            case "color" -> config.getPluginColorName();
            case "silent" -> String.valueOf(player != null && playerData.isSilentJoin(player.getUniqueId()));
            default -> null;
        };
    }

    private boolean isVanished(OfflinePlayer player) {
        return player != null && vanishManager.isVanished(player.getUniqueId());
    }

    private static boolean canSeeVanished(OfflinePlayer player) {
        Player online = player != null ? player.getPlayer() : null;
        return online != null && VanishManager.canSeeVanished(online);
    }
}
