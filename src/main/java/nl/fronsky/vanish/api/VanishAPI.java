/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.api;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public API for interacting with the Vanish plugin.
 * This class provides methods for third-party plugins to check and modify player vanish states.
 */
public class VanishAPI {

    /**
     * Checks if a player is currently vanished.
     *
     * @param player the player to check
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    public static boolean isVanished(Player player) {
        if (player == null) return false;
        Data data = VanishModule.getData();
        if (data == null) return false;
        return MetaData.getVanishState(player, data).equals(State.HIDDEN);
    }

    /**
     * Checks if a player is currently vanished by UUID.
     *
     * @param uuid the UUID of the player to check
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    public static boolean isVanished(UUID uuid) {
        Data data = VanishModule.getData();
        if (data == null) return false;
        return data.getVanishedPlayers().containsKey(uuid);
    }

    /**
     * Gets a collection of all currently vanished players.
     *
     * @return a collection of vanished {@code VanishPlayer} objects
     */
    public static Collection<VanishPlayer> getVanishedPlayers() {
        Data data = VanishModule.getData();
        if (data == null) return java.util.Collections.emptyList();
        return data.getVanishedPlayers().values();
    }

    /**
     * Gets the UUIDs of all currently vanished players.
     *
     * @return a collection of UUIDs of vanished players
     */
    public static Collection<UUID> getVanishedPlayerUUIDs() {
        return getVanishedPlayers().stream()
                .map(VanishPlayer::getUuid)
                .collect(Collectors.toList());
    }

    /**
     * Gets the number of currently vanished players.
     *
     * @return the count of vanished players
     */
    public static int getVanishedPlayerCount() {
        Data data = VanishModule.getData();
        if (data == null) return 0;
        return data.getVanishedPlayers().size();
    }

    /**
     * Vanishes a player programmatically.
     *
     * @param player the player to vanish
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean vanishPlayer(Player player) {
        if (player == null) return false;
        try {
            VanishPlayer vanishPlayer = new VanishPlayer(player);
            if (!isVanished(player)) {
                vanishPlayer.hide(false);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Unvanishes a player programmatically.
     *
     * @param player the player to unvanish
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean unvanishPlayer(Player player) {
        if (player == null) return false;
        try {
            VanishPlayer vanishPlayer = new VanishPlayer(player);
            if (isVanished(player)) {
                vanishPlayer.show(false);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
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
        } else {
            vanishPlayer(player);
            return true;
        }
    }

    /**
     * Checks if a player can see vanished players.
     *
     * @param player the player to check
     * @return {@code true} if the player has permission to see vanished players
     */
    public static boolean canSeeVanished(Player player) {
        if (player == null) return false;
        return player.hasPermission("vanish.see") || player.hasPermission("vanish.*");
    }

    /**
     * Checks if the Vanish plugin is currently loaded and available.
     *
     * @return {@code true} if the plugin is available, {@code false} otherwise
     */
    public static boolean isAvailable() {
        return VanishModule.getData() != null;
    }
}

