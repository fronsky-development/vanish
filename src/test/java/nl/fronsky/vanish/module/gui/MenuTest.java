/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class MenuTest extends VanishTestBase {

    @Test
    void clickingAPlayerHeadTogglesThatPlayer() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");
        admin.performCommand("vanish gui");
        InventoryView view = admin.getOpenInventory();

        admin.simulateInventoryClick(view, ClickType.LEFT, slotNamed(view.getTopInventory(), "Viewer"));
        server.getScheduler().performOneTick();

        assertTrue(vanishManager.isVanished(viewer));
    }

    @Test
    void itemsCannotBeTakenFromOrPutIntoMenus() {
        TestPlayer admin = joinOperator("Admin");
        admin.performCommand("vanish gui");
        InventoryView view = admin.getOpenInventory();

        assertTrue(admin.simulateInventoryClick(view, ClickType.SHIFT_LEFT, 0).isCancelled());
        assertTrue(admin.simulateInventoryClick(view, ClickType.LEFT, 60).isCancelled(), "own inventory");
        InventoryDragEvent drag = new InventoryDragEvent(view, null, new ItemStack(Material.STONE), false,
                Map.of(10, new ItemStack(Material.STONE)));
        server.getPluginManager().callEvent(drag);
        assertTrue(drag.isCancelled());
    }

    @Test
    void colorMenuSavesTheColorAndConfirms() {
        TestPlayer admin = joinOperator("Admin");
        admin.performCommand("vanish color");
        InventoryView view = admin.getOpenInventory();
        clearMessages(admin);

        admin.simulateInventoryClick(view, ClickType.LEFT, slotNamed(view.getTopInventory(), "Green"));
        server.getScheduler().performOneTick();

        assertEquals("GREEN", plugin.getConfig().getString("plugin-color"));
        assertTrue(admin.nextMessage().contains("changed to GREEN"));
    }

    @Test
    void soundMenuSelectsASoundWithRightClick() {
        TestPlayer admin = joinOperator("Admin");
        admin.performCommand("vanish sound");
        InventoryView view = admin.getOpenInventory();

        admin.simulateInventoryClick(view, ClickType.RIGHT, 0);
        server.getScheduler().performOneTick();

        assertEquals(Sound.values()[0].name(), plugin.getConfig().getString("sound"));
    }

    private static int slotNamed(Inventory inventory, String name) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.hasItemMeta()
                    && name.equals(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) {
                return slot;
            }
        }
        throw new AssertionError(name + " is not in the menu");
    }
}
