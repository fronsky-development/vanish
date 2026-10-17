/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hides vanished players from the Dynmap web map through Dynmap's own commands.
 */
public final class DynmapHook {
    // Plugin names are case-sensitive on Spigot and Dynmap registers itself as "dynmap".
    public static final String PLUGIN_NAME = "dynmap";

    private final Logger logger;

    public DynmapHook(Logger logger) {
        this.logger = logger;
    }

    public void hide(Player player) {
        dispatch("dynmap hide " + player.getName());
    }

    public void show(Player player) {
        dispatch("dynmap show " + player.getName());
    }

    private void dispatch(String command) {
        // Checked on every call because Dynmap can be disabled after startup.
        if (!Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
            return;
        }
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (CommandException e) {
            logger.log(Level.WARNING, "Dynmap command '" + command + "' failed.", e);
        }
    }
}
