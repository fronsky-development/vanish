/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.models;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.UUID;

@Getter
public class PlayerState {
    private final UUID uuid;
    private final GameMode gameMode;
    private final boolean allowFlying, isFlying, isSneaking;
    private final Location location;

    public PlayerState(VanishPlayer vanishPlayer) {
        uuid = vanishPlayer.getUuid();
        gameMode = vanishPlayer.getPlayer().getGameMode();
        allowFlying = vanishPlayer.getPlayer().getAllowFlight();
        isFlying = vanishPlayer.getPlayer().isFlying();
        isSneaking = vanishPlayer.getPlayer().isSneaking();
        location = vanishPlayer.getPlayer().getLocation();
    }
}
