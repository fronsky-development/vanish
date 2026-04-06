/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Dynmap {
    /**
     * Hides the player from Dynmap if installed.
     *
     * @param player the player to hide from Dynmap
     */
    public static void hide(Player player) {
        if (isDynmapInstalled()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "dynmap hide " + player.getName());
        }
    }

    /**
     * Shows the player on Dynmap if installed.
     *
     * @param player the player to show on Dynmap
     */
    public static void show(Player player) {
        if (isDynmapInstalled()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "dynmap show " + player.getName());
        }
    }


    /**
     * Checks if Dynmap plugin is installed.
     *
     * @return {@code true} if Dynmap is installed, otherwise {@code false}
     */
    private static boolean isDynmapInstalled() {
        return Bukkit.getPluginManager().getPlugin("Dynmap") != null;
    }
}
