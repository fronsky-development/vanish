/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.api;

import nl.fronsky.vanish.VanishPlugin;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Public API for interacting with the Vanish plugin.
 * This class provides methods for third-party plugins to check and modify player vanish states.
 * <p>
 * Read methods are thread-safe. Methods that change a vanish state must be called on the main server thread.
 */
public class VanishAPI {

    /**
     * Checks if a player is currently vanished.
     *
     * @param player the player to check
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    public static boolean isVanished(Player player) {
        VanishManager manager = manager();
        return manager != null && manager.isVanished(player);
    }

    /**
     * Checks if a player is currently vanished by UUID.
     *
     * @param uuid the UUID of the player to check
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    public static boolean isVanished(UUID uuid) {
        VanishManager manager = manager();
        return manager != null && manager.isVanished(uuid);
    }

    /**
     * Gets all currently vanished players.
     *
     * @return a snapshot of the vanished {@code VanishPlayer} objects
     */
    public static Collection<VanishPlayer> getVanishedPlayers() {
        VanishManager manager = manager();
        return manager != null ? manager.getVanishedPlayers() : Collections.emptyList();
    }

    /**
     * Gets the UUIDs of all currently vanished players.
     *
     * @return a snapshot of the UUIDs of vanished players
     */
    public static Collection<UUID> getVanishedPlayerUUIDs() {
        VanishManager manager = manager();
        return manager != null ? new ArrayList<>(manager.getVanishedIds()) : Collections.emptyList();
    }

    /**
     * Gets the number of currently vanished players.
     *
     * @return the count of vanished players
     */
    public static int getVanishedPlayerCount() {
        VanishManager manager = manager();
        return manager != null ? manager.getVanishedCount() : 0;
    }

    /**
     * Vanishes a player programmatically.
     *
     * @param player the player to vanish
     * @return {@code true} if the player was vanished, {@code false} if already vanished or unavailable
     */
    public static boolean vanishPlayer(Player player) {
        VanishManager manager = manager();
        if (manager == null || player == null || manager.isVanished(player)) {
            return false;
        }
        manager.vanish(manager.getPlayer(player), false);
        return true;
    }

    /**
     * Unvanishes a player programmatically.
     *
     * @param player the player to unvanish
     * @return {@code true} if the player was made visible, {@code false} if not vanished or unavailable
     */
    public static boolean unvanishPlayer(Player player) {
        VanishManager manager = manager();
        if (manager == null || player == null || !manager.isVanished(player)) {
            return false;
        }
        manager.unvanish(manager.getPlayer(player), false);
        return true;
    }

    /**
     * Toggles a player's vanish state.
     *
     * @param player the player to toggle
     * @return the new state ({@code true} if now vanished, {@code false} if now visible)
     */
    public static boolean toggleVanish(Player player) {
        if (isVanished(player)) {
            unvanishPlayer(player);
            return false;
        }
        return vanishPlayer(player);
    }

    /**
     * Checks if a player can see vanished players.
     *
     * @param player the player to check
     * @return {@code true} if the player has permission to see vanished players
     */
    public static boolean canSeeVanished(Player player) {
        return player != null && VanishManager.canSeeVanished(player);
    }

    /**
     * Checks if the Vanish plugin is currently loaded and available.
     *
     * @return {@code true} if the plugin is available, {@code false} otherwise
     */
    public static boolean isAvailable() {
        return manager() != null;
    }

    private static VanishManager manager() {
        try {
            VanishPlugin plugin = JavaPlugin.getPlugin(VanishPlugin.class);
            return plugin.isEnabled() ? plugin.getVanishManager() : null;
        } catch (IllegalArgumentException | IllegalStateException e) {
            // The plugin class was not loaded by Bukkit, e.g. because the API was shaded into another plugin.
            return null;
        }
    }
}

