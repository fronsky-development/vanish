/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.integrations.ProtocolLibHook;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.models.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lets vanished players open chests, barrels, shulker boxes and ender chests without animation or sound.
 * <p>
 * With ProtocolLib the container packets are suppressed. Without it, the player is briefly put in spectator
 * mode (spectators open containers silently) and restored when the inventory closes.
 */
public final class SilentContainerListener implements Listener {
    private final Plugin plugin;
    private final VanishConfig config;
    private final VanishManager vanishManager;
    private final ProtocolLibHook protocolLib;
    private final Map<UUID, PlayerState> spectatorSessions = new HashMap<>();

    /**
     * @param protocolLib the ProtocolLib hook, or {@code null} to use the spectator mode fallback
     */
    public SilentContainerListener(Plugin plugin, VanishConfig config, VanishManager vanishManager,
                                   ProtocolLibHook protocolLib) {
        this.plugin = plugin;
        this.config = config;
        this.vanishManager = vanishManager;
        this.protocolLib = protocolLib;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        // The off-hand event follows the cancelled main-hand event and would open the container a second time.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || block == null
                || !vanishManager.isVanished(player)) {
            return;
        }

        if (block.getType() == Material.ENDER_CHEST) {
            if (config.isSilentEnderChestEnabled()) {
                openEnderChest(event, player, block);
            }
            return;
        }
        if (!config.isSilentChestEnabled()) {
            return;
        }
        BlockState state = block.getState();
        if (state instanceof Chest || state instanceof Barrel || state instanceof ShulkerBox) {
            openContainer(event, player, block, ((Container) state).getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !hasSession(player.getUniqueId())) {
            return;
        }
        // Restore one tick later so the closing animation and sound are suppressed as well.
        Bukkit.getScheduler().runTaskLater(plugin, () -> endSession(player), 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Restore right away, so the original game mode is what gets saved with the player data.
        endSession(event.getPlayer());
    }

    /**
     * Restores every player that is still in a silent container session. Used when the plugin is disabled.
     */
    public void restoreAll() {
        for (UUID uuid : new ArrayList<>(spectatorSessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                endSession(player);
            }
        }
        spectatorSessions.clear();
    }

    private void openContainer(PlayerInteractEvent event, Player player, Block block, Inventory inventory) {
        event.setCancelled(true);
        if (protocolLib != null) {
            protocolLib.beginSilentContainer(player, block.getLocation());
        } else {
            spectatorSessions.putIfAbsent(player.getUniqueId(), PlayerState.capture(player));
            player.setGameMode(GameMode.SPECTATOR);
        }
        player.openInventory(inventory);
    }

    private void openEnderChest(PlayerInteractEvent event, Player player, Block block) {
        event.setCancelled(true);
        if (protocolLib != null) {
            protocolLib.beginSilentContainer(player, block.getLocation());
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.openInventory(player.getEnderChest());
            }
        });
    }

    private boolean hasSession(UUID uuid) {
        return spectatorSessions.containsKey(uuid) || (protocolLib != null && protocolLib.isSilentContainer(uuid));
    }

    private void endSession(Player player) {
        if (protocolLib != null) {
            protocolLib.endSilentContainer(player.getUniqueId());
        }
        PlayerState state = spectatorSessions.remove(player.getUniqueId());
        if (state != null) {
            state.restore(player);
        }
    }
}
