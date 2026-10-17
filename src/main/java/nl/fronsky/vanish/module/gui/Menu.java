/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * A chest menu opened through {@link MenuListener}. Every click while the menu is open is cancelled.
 */
public abstract class Menu {
    private final Inventory inventory;

    protected Menu(int size, String title) {
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Handles a click on one of this menu's slots. Runs one tick after the click, so opening or closing
     * inventories here is safe.
     */
    protected abstract void onClick(Player viewer, int slot, ClickType click);

    protected static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
