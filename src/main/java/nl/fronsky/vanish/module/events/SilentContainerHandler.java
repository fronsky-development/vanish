/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.models.PlayerState;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.ProtocolLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Handles silent container opening for vanished players.
 * Extracted from DisabledActions (R-12) for Single Responsibility.
 */
public class SilentContainerHandler {
    private final Data data;
    private final ProtocolLib protocolLib;
    private final HashMap<UUID, PlayerState> playerStateInfo;

    public SilentContainerHandler(Data data) {
        this.data = data;
        this.protocolLib = data.getProtocolLib();
        this.playerStateInfo = new HashMap<>();
    }

    /**
     * Handles silent opening of a container block for a vanished player.
     *
     * @param event       the interact event
     * @param vanishPlayer the vanished player
     * @param block       the clicked block
     * @param silentChest whether silent chest is enabled
     * @param silentEnderChest whether silent ender chest is enabled
     * @return {@code true} if the event was handled (cancelled), {@code false} otherwise
     */
    public boolean handleContainerInteract(PlayerInteractEvent event, VanishPlayer vanishPlayer, Block block,
                                           boolean silentChest, boolean silentEnderChest) {
        // Shulker boxes
        if (silentChest && block.getState() instanceof ShulkerBox) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return false;
            }
            beginSilentOpen(vanishPlayer, block.getLocation());
            ShulkerBox shulkerBox = (ShulkerBox) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(shulkerBox.getInventory());
            event.setCancelled(true);
            return true;
        }

        // Chests
        if (silentChest && block.getState() instanceof Chest) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return false;
            }
            beginSilentOpen(vanishPlayer, block.getLocation());
            Chest chest = (Chest) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(chest.getInventory());
            event.setCancelled(true);
            return true;
        }

        // Barrels
        if (silentChest && block.getState() instanceof Barrel) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return false;
            }
            beginSilentOpen(vanishPlayer, block.getLocation());
            Barrel barrel = (Barrel) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(barrel.getInventory());
            event.setCancelled(true);
            return true;
        }

        // Ender chests
        if (silentEnderChest && block.getState() instanceof EnderChest) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return false;
            }
            event.setCancelled(true);
            if (protocolLib != null) {
                protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), block.getLocation());
            }
            Bukkit.getScheduler().runTaskLater(data.getPlugin(), () ->
                    vanishPlayer.getPlayer().openInventory(vanishPlayer.getPlayer().getEnderChest()), 1L);
            return true;
        }

        return false;
    }

    /**
     * Handles inventory close for silent container mode.
     *
     * @param vanishPlayer the vanished player closing the inventory
     */
    public void handleInventoryClose(VanishPlayer vanishPlayer) {
        Bukkit.getScheduler().runTaskLater(data.getPlugin(), () -> {
            if (protocolLib != null) {
                protocolLib.endSilentContainer(vanishPlayer.getPlayer());
            } else {
                restoreState(vanishPlayer);
            }
        }, 1L);
    }

    /**
     * Begins silent container opening using ProtocolLib or spectator fallback.
     */
    private void beginSilentOpen(VanishPlayer vanishPlayer, Location blockLocation) {
        if (protocolLib != null) {
            protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), blockLocation);
        } else {
            playerStateInfo.put(vanishPlayer.getUuid(), new PlayerState(vanishPlayer));
            vanishPlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    /**
     * Restores the player's state after closing a silently opened container.
     */
    private void restoreState(VanishPlayer vanishPlayer) {
        if (!playerStateInfo.containsKey(vanishPlayer.getUuid())) {
            return;
        }

        PlayerState playerState = playerStateInfo.get(vanishPlayer.getUuid());
        vanishPlayer.getPlayer().setGameMode(playerState.getGameMode());
        vanishPlayer.getPlayer().setAllowFlight(playerState.isAllowFlying());
        vanishPlayer.getPlayer().setFlying(playerState.isFlying());
        vanishPlayer.getPlayer().setSneaking(playerState.isSneaking());
        vanishPlayer.getPlayer().teleport(playerState.getLocation());
        playerStateInfo.remove(vanishPlayer.getUuid());
    }
}





