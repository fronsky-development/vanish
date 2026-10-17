/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Opens {@link Menu}s and routes their clicks. Players without an open menu return after a single map lookup.
 */
public final class MenuListener implements Listener {
    private final Plugin plugin;
    // Tracked per viewer instead of via InventoryHolder: getHolder() copies block inventories on every call.
    private final Map<UUID, Menu> openMenus = new HashMap<>();

    public MenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Menu menu) {
        if (player.openInventory(menu.getInventory()) != null) {
            openMenus.put(player.getUniqueId(), menu);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Menu menu = findOpenMenu(event.getWhoClicked().getUniqueId(), event.getInventory());
        if (menu == null) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !menu.getInventory().equals(event.getClickedInventory())) {
            return;
        }

        int slot = event.getSlot();
        ClickType click = event.getClick();
        // Inventories must not be opened or closed while a click event is being handled.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (openMenus.get(player.getUniqueId()) == menu) {
                menu.onClick(player, slot, click);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (findOpenMenu(event.getWhoClicked().getUniqueId(), event.getInventory()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        UUID viewer = event.getPlayer().getUniqueId();
        if (findOpenMenu(viewer, event.getInventory()) != null) {
            openMenus.remove(viewer);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        openMenus.remove(event.getPlayer().getUniqueId());
    }

    private Menu findOpenMenu(UUID viewer, Inventory topInventory) {
        if (openMenus.isEmpty()) {
            return null;
        }
        Menu menu = openMenus.get(viewer);
        return menu != null && menu.getInventory().equals(topInventory) ? menu : null;
    }
}
