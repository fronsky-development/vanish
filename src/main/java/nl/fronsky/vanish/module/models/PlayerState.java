/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.models;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Snapshot of the state that is changed while a vanished player opens a container in spectator mode.
 */
public record PlayerState(GameMode gameMode, boolean allowFlight, boolean flying, boolean sneaking, Location location) {

    public static PlayerState capture(Player player) {
        return new PlayerState(player.getGameMode(), player.getAllowFlight(), player.isFlying(),
                player.isSneaking(), player.getLocation());
    }

    public void restore(Player player) {
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
        player.setSneaking(sneaking);
        player.teleport(location);
    }
}
