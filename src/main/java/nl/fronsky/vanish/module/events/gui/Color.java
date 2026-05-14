/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.gui;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class Color implements Listener {
    private static final String KEY = "fronsky_vanish_color_selection";
    private final Data data;

    public Color() {
        data = VanishModule.getData();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.hasMetadata(KEY)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().equals(Material.AIR)) {
            return;
        }

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        String typeName = current.getType().name();
        if (!typeName.endsWith("_STAINED_GLASS_PANE")) {
            return;
        }
        if (!current.hasItemMeta()) {
            return;
        }

        handleColorSelection(player, current);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.hasMetadata(KEY)) {
            player.removeMetadata(KEY, data.getPlugin());
        }
    }

    /**
     * Handles the selection of a color by the player from an inventory interface.
     *
     * @param player the player who selected the color
     * @param item   the item that the player selected
     */
    private void handleColorSelection(Player player, ItemStack item) {
        String name = item.getType().name();
        String suffix = "_STAINED_GLASS_PANE";
        if (!name.endsWith(suffix)) {
            return;
        }

        String colorName = name.substring(0, name.length() - suffix.length());
        BarColor color;
        try {
            color = BarColor.valueOf(colorName);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        executeColorCommand(player, color);
        player.removeMetadata(KEY, data.getPlugin());
        player.closeInventory();
    }

    /**
     * Executes a command to change the vanish bar color for a player.
     *
     * @param player the player who initiated the color change
     * @param color  the new {@link BarColor} to set
     */
    private void executeColorCommand(Player player, BarColor color) {
        String command = "vanish color " + color.name();
        Bukkit.dispatchCommand(player, command);
    }
}
