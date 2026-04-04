/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.api.VanishAPI;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI expansion for Vanish.
 * Identifier: %vanish_...%
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public PlaceholderAPIExpansion(Main plugin) {
        this.plugin = plugin;
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
        Data data = VanishModule.getData();
        if (data == null) {
            return "";
        }

        if (params.equalsIgnoreCase("isvanished")) {
            if (player == null || !player.isOnline()) {
                return "false";
            }
            Player p = player.getPlayer();
            return String.valueOf(VanishAPI.isVanished(p));
        }

        if (params.equalsIgnoreCase("status")) {
            if (player == null || !player.isOnline()) {
                return "Visible";
            }
            Player p = player.getPlayer();
            return (VanishAPI.isVanished(p)) ? "Vanished" : "Visible";
        }

        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(VanishAPI.getVanishedPlayerCount());
        }

        if (params.equalsIgnoreCase("cansee")) {
            if (player == null || !player.isOnline()) {
                return "false";
            }
            Player p = player.getPlayer();
            return String.valueOf(VanishAPI.canSeeVanished(p));
        }

        if (params.equalsIgnoreCase("total_online")) {
            int online = plugin.getServer().getOnlinePlayers().size();
            int visible = Math.max(0, online - VanishAPI.getVanishedPlayerCount());
            return String.valueOf(visible);
        }

        if (params.equalsIgnoreCase("color")) {
            return data.getConfig().get().getString("plugin-color", "BLUE");
        }

        if (params.equalsIgnoreCase("silent")) {
            if (player == null) {
                return "false";
            }
            return String.valueOf(data.getPlayers().get().getBoolean(player.getUniqueId() + ".silent", true));
        }

        return null;
    }
}
