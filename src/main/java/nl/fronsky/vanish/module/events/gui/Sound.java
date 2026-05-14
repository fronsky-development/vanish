/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.gui;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Sound implements Listener {
    private static final String KEY = "fronsky_vanish_sound_inventory";
    private final Data data;
    private final ChatColor color;

    public Sound() {
        data = VanishModule.getData();
        color = data.getPluginChatColor();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.hasMetadata(KEY)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        String title = event.getView().getTitle();
        int pageNum = 1;
        try {
            String stripped = ChatColor.stripColor(title);
            int open = stripped.lastIndexOf('(');
            int slash = stripped.lastIndexOf('/');
            if (open >= 0 && slash > open) {
                String num = stripped.substring(open + 1, slash).replaceAll("\\D+", "");
                if (!num.isEmpty()) {
                    pageNum = Integer.parseInt(num);
                }
            }
        } catch (Exception ignored) {
            pageNum = 1;
        }

        if (event.getRawSlot() == 53 && pageNum < Math.ceil(org.bukkit.Sound.values().length / 45.0)) {
            int nextPageNum = pageNum + 1;
            Inventory nextPageInventory = nl.fronsky.vanish.module.subcommands.vanish.Sound.createSoundInventory(player, nextPageNum, data, color);
            player.openInventory(nextPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        if (event.getRawSlot() == 45 && pageNum > 1) {
            int prevPageNum = pageNum - 1;
            Inventory prevPageInventory = nl.fronsky.vanish.module.subcommands.vanish.Sound.createSoundInventory(player, prevPageNum, data, color);
            player.openInventory(prevPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        // Ignore informational footer
        if (event.getRawSlot() == 49) {
            return;
        }

        // Sound items
        if ((current.getType() == Material.NOTE_BLOCK || current.getType() == Material.JUKEBOX) && current.hasItemMeta() && current.getItemMeta() != null && current.getItemMeta().hasDisplayName()) {
            String raw = ChatColor.stripColor(current.getItemMeta().getDisplayName());
            raw = raw.replace("✔", "").trim();

            org.bukkit.Sound sound;
            try {
                sound = org.bukkit.Sound.valueOf(raw);
            } catch (IllegalArgumentException ignored) {
                return;
            }

            ClickType click = event.getClick();
            if (click.isLeftClick()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                return;
            }

            if (click.isRightClick()) {
                data.getConfig().get().set("sound", sound.name());
                data.getConfig().save();
                data.getConfig().reload();
                player.closeInventory();
                player.removeMetadata(KEY, data.getPlugin());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.hasMetadata(KEY)) {
            player.removeMetadata(KEY, data.getPlugin());
        }
    }
}
