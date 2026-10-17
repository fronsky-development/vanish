/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.custom;

import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after a player has vanished or has become visible again.
 */
public class VisibilityChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final VanishPlayer vanishPlayer;
    private final boolean visible;
    private final boolean action;

    /**
     * @param vanishPlayer the player whose visibility changed
     * @param action       {@code true} if the change was caused by the player joining or quitting
     */
    public VisibilityChangeEvent(VanishPlayer vanishPlayer, boolean action) {
        this.vanishPlayer = vanishPlayer;
        this.visible = !vanishPlayer.isVanished();
        this.action = action;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public VanishPlayer getVanishPlayer() {
        return vanishPlayer;
    }

    /**
     * @return {@code true} if the player became visible, {@code false} if the player vanished
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return {@code true} if the change was caused by the player joining (vanish) or quitting (unvanish)
     */
    public boolean isAction() {
        return action;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
