/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.utils;

import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class MetaData {

    /**
     * Retrieves a collection of VanishPlayer objects for all online players.
     *
     * @return a collection of {@code VanishPlayer} objects representing all online players
     */
    public static Collection<VanishPlayer> getOnlinePlayers() {
        Collection<VanishPlayer> vanishPlayers = new HashSet<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            vanishPlayers.add(new VanishPlayer(onlinePlayer));
        }
        return vanishPlayers;
    }

    /**
     * Retrieves the number of vanished players currently online.
     *
     * @return the number of vanished players currently online
     */
    public static int getVanishedPlayersAmount(Data data) {
        if (data == null) return 0;
        return data.getVanishedPlayers().size();
    }

    /**
     * Retrieves the vanish state of the specified player.
     *
     * @param player the player whose vanish state is being checked
     * @param data   the data containing information about vanished players
     * @return the vanish state of the player
     */
    public static State getVanishState(Player player, Data data) {
        if (player == null || data == null) {
            return State.VISIBLE;
        }

        if (player.hasMetadata("fronsky_vanish") && data.getVanishedPlayers().containsKey(player.getUniqueId())) {
            return State.HIDDEN;
        }

        return State.VISIBLE;
    }

    /**
     * Clears the player state cache. (No-op, kept for API compatibility.)
     */
    public static void clearCache() {
        // No-op: dead cache removed in R-13
    }

    /**
     * Invalidates cache for a specific player. (No-op, kept for API compatibility.)
     *
     * @param uuid the UUID of the player to invalidate
     */
    public static void invalidateCache(UUID uuid) {
        // No-op: dead cache removed in R-13
    }
}
