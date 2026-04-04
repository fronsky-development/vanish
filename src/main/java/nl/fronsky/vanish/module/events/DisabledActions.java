/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.PlayerState;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import nl.fronsky.vanish.module.utils.ProtocolLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class DisabledActions implements Listener {
    private final Data data;
    private final ProtocolLib protocolLib;
    private final HashMap<UUID, PlayerState> playerStateInfo;
    private boolean disabledDamage, disabledHunger, disabledMobTarget, disabledSilentChest, disabledSilentEnderChest,
            disabledPressurePlates, disabledDeathMessages;

    public DisabledActions() {
        data = VanishModule.getData();
        protocolLib = data.getProtocolLib();
        playerStateInfo = new HashMap<>();
        reloadConfig();
    }

    /**
     * Reloads all disabled-action settings from the config.
     * Called on construction and after {@code /vanish reload}.
     */
    public void reloadConfig() {
        FileConfiguration file = data.getConfig().get();
        disabledDamage = file.getBoolean("disabled-actions.damage");
        disabledHunger = file.getBoolean("disabled-actions.hunger");
        disabledMobTarget = file.getBoolean("disabled-actions.mob-target");
        disabledSilentChest = file.getBoolean("disabled-actions.silent-chest");
        disabledSilentEnderChest = file.getBoolean("disabled-actions.silent-ender-chest");
        disabledPressurePlates = file.getBoolean("disabled-actions.pressure-plates");
        disabledDeathMessages = file.getBoolean("disabled-actions.death-messages");
    }

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        VanishPlayer vanishPlayer = new VanishPlayer((Player) event.getEntity());
        if (disabledDamage && MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void foodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        VanishPlayer vanishPlayer = new VanishPlayer((Player) event.getEntity());
        if (disabledHunger && MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }

        VanishPlayer vanishPlayer = new VanishPlayer((Player) event.getTarget());
        if (disabledMobTarget && MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        VanishPlayer vanishPlayer = new VanishPlayer(event.getEntity());
        if (disabledDeathMessages && MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            String deathMessage = event.getDeathMessage();
            event.setDeathMessage("");
            if (deathMessage != null && !deathMessage.isEmpty()) {
                vanishPlayer.sendMessage(deathMessage);
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!(block.getState() instanceof Chest)
                && !(block.getState() instanceof EnderChest)
                && !(block.getState() instanceof ShulkerBox)
                && !(block.getState() instanceof Barrel)
                && !block.getType().name().contains("PRESSURE_PLATE")) {
            return;
        }

        VanishPlayer vanishPlayer = new VanishPlayer(event.getPlayer());
        if (MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.VISIBLE)) {
            return;
        }

        // Treat shulker boxes like chests for silent container behavior.
        if (disabledSilentChest && block.getState() instanceof ShulkerBox) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (protocolLib != null) {
                protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), block.getLocation());
            } else {
                playerStateInfo.put(vanishPlayer.getUuid(), new PlayerState(vanishPlayer));
                vanishPlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            }

            ShulkerBox shulkerBox = (ShulkerBox) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(shulkerBox.getInventory());
            event.setCancelled(true);
            return;
        }

        if (disabledSilentChest && block.getState() instanceof Chest) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            // Enable per-player silent container mode (packet-level)
            if (protocolLib != null) {
                protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), block.getLocation());
            } else {
                // Fallback without ProtocolLib: spectator trick
                playerStateInfo.put(vanishPlayer.getUuid(), new PlayerState(vanishPlayer));
                vanishPlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            }

            Chest chest = (Chest) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(chest.getInventory());
            event.setCancelled(true);
            return;
        }

        // Barrels (silent open)
        if (disabledSilentChest && block.getState() instanceof Barrel) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (protocolLib != null) {
                protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), block.getLocation());
            } else {
                playerStateInfo.put(vanishPlayer.getUuid(), new PlayerState(vanishPlayer));
                vanishPlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            }

            Barrel barrel = (Barrel) event.getClickedBlock().getState();
            vanishPlayer.getPlayer().openInventory(barrel.getInventory());
            event.setCancelled(true);
            return;
        }

        if (disabledSilentEnderChest && block.getState() instanceof EnderChest) {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            event.setCancelled(true);
            if (protocolLib != null) {
                protocolLib.beginSilentContainer(vanishPlayer.getPlayer(), block.getLocation());
            }
            Bukkit.getScheduler().runTaskLater(data.getPlugin(), () -> vanishPlayer.getPlayer().openInventory(vanishPlayer.getPlayer().getEnderChest()), 1L);
            return;
        }

        if (disabledPressurePlates && block.getType().name().contains("PRESSURE_PLATE")) {
            event.setCancelled(event.getAction().equals(Action.PHYSICAL));
        }
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        VanishPlayer vanishPlayer = new VanishPlayer((Player) event.getPlayer());

        Bukkit.getScheduler().runTaskLater(data.getPlugin(), () -> {
            if (protocolLib != null) {
                protocolLib.endSilentContainer(vanishPlayer.getPlayer());
            } else {
                restoreState(vanishPlayer);
            }
        }, 1L);
    }

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
