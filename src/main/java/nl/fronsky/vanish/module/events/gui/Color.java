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
    private final String KEY = "fronsky_vanish_color_selection";
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
        try {
            if (event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
        } catch (NullPointerException exception) {
            return;
        }

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        if (event.getCurrentItem() != null && event.getCurrentItem().getType().name().substring(event.getCurrentItem().getType().name().indexOf("_")).equals("_STAINED_GLASS_PANE") && event.getCurrentItem().hasItemMeta()) {
            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            handleColorSelection(player, item);
        }
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
        if (!name.substring(name.indexOf("_")).equals("_STAINED_GLASS_PANE")) {
            return;
        }

        BarColor color = BarColor.valueOf(name.substring(0, name.indexOf("_")));
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
