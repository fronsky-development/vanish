/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.models;

import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.enums.State;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A player as seen by Vanish. The instance of an online player is cached by {@link VanishManager} and reused,
 * so it must not be kept after the player quits.
 */
public final class VanishPlayer {
    private final VanishManager vanishManager;
    private final Player player;

    public VanishPlayer(VanishManager vanishManager, Player player) {
        this.vanishManager = vanishManager;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUuid() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

    public String getDisplayName() {
        return player.getDisplayName();
    }

    public boolean isVanished() {
        return vanishManager.isVanished(player);
    }

    public State getState() {
        return isVanished() ? State.HIDDEN : State.VISIBLE;
    }

    /**
     * Sends a message with {@code &} color codes to the player. Empty messages are not sent.
     *
     * @param message the message to send to the player
     */
    public void sendMessage(String message) {
        if (!message.isEmpty()) {
            player.sendMessage(ColorUtil.colorize(message));
        }
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    /**
     * Vanishes the player.
     *
     * @param join {@code true} if the player is vanished because they joined
     */
    public void hide(boolean join) {
        vanishManager.vanish(this, join);
    }

    /**
     * Makes the player visible again.
     *
     * @param quit {@code true} if the player is leaving the server
     */
    public void show(boolean quit) {
        vanishManager.unvanish(this, quit);
    }
}
