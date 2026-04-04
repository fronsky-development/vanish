/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.gui;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class Sound implements Listener {
    private final String KEY = "fronsky_vanish_sound_inventory";
    private final Data data;
    private ChatColor color;

    public Sound() {
        data = VanishModule.getData();
        String colorKey = data.getConfig().get().getString("plugin-color");
        color = ChatColor.BLUE;
        if (colorKey != null) {
            try {
                if (colorKey.equals("PINK")) {
                    color = ChatColor.LIGHT_PURPLE;
                } else if (colorKey.equals("PURPLE")) {
                    color = ChatColor.DARK_PURPLE;
                } else {
                    color = ChatColor.valueOf(colorKey);
                }
            } catch (IllegalArgumentException exception) {
                Logger.severe(exception.getMessage());
            }
        }
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
            // Title format: "Select Sound (x/y)" (colors included)
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
            Inventory nextPageInventory = createSoundInventory(player, nextPageNum);
            player.openInventory(nextPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        if (event.getRawSlot() == 45 && pageNum > 1) {
            int prevPageNum = pageNum - 1;
            Inventory prevPageInventory = createSoundInventory(player, prevPageNum);
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
                // Preview
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                return;
            }

            if (click.isRightClick()) {
                // Select
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

    /**
     * Creates an inventory containing a list of sounds for a given page number.
     *
     * @param player  the player viewing the inventory
     * @param pageNum the page number to display
     * @return the inventory containing sound items and navigation items
     */
    private Inventory createSoundInventory(Player player, int pageNum) {
        int maxItemsPerPage = 45;
        org.bukkit.Sound[] sounds = org.bukkit.Sound.values();
        int startIndex = (pageNum - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, sounds.length);

        String currentSound = data.getConfig().get().getString("sound", "AMBIENT_CAVE");

        Inventory inventory = Bukkit.createInventory(player, 54,
                color + "Select Sound " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + pageNum + "/" + (int) Math.ceil(sounds.length / (double) maxItemsPerPage) + ChatColor.DARK_GRAY + ")");

        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; ++i) {
            org.bukkit.Sound sound = sounds[i];

            boolean selected = sound.name().equalsIgnoreCase(currentSound);

            ItemStack soundItem = new ItemStack(selected ? Material.JUKEBOX : Material.NOTE_BLOCK);
            ItemMeta meta = soundItem.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName((selected ? ChatColor.GREEN + "✔ " : ChatColor.WHITE.toString()) + ChatColor.GRAY + sound.name());
            java.util.List<String> lore = new java.util.ArrayList<>();
            if (selected) {
                lore.add(ChatColor.DARK_GRAY + "Currently selected");
                lore.add("");
            }
            lore.add(ChatColor.GRAY + "Left click: " + ChatColor.WHITE + "Preview");
            lore.add(ChatColor.GRAY + "Right click: " + ChatColor.WHITE + "Select");
            meta.setLore(lore);

            soundItem.setItemMeta(meta);
            inventory.setItem(itemIndex++, soundItem);
        }

        // Navigation
        if (pageNum < Math.ceil(sounds.length / (double) maxItemsPerPage)) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta2 = nextPage.getItemMeta();
            if (meta2 != null) {
                meta2.setDisplayName(ChatColor.GREEN + "Next Page");
                meta2.setLore(java.util.Collections.singletonList(ChatColor.GRAY + "Go to page " + (pageNum + 1)));
                nextPage.setItemMeta(meta2);
            }
            inventory.setItem(53, nextPage);
        }
        if (pageNum > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta2 = prevPage.getItemMeta();
            if (meta2 != null) {
                meta2.setDisplayName(ChatColor.GREEN + "Previous Page");
                meta2.setLore(java.util.Collections.singletonList(ChatColor.GRAY + "Go to page " + (pageNum - 1)));
                prevPage.setItemMeta(meta2);
            }
            inventory.setItem(45, prevPage);
        }

        // Footer hint
        ItemStack hint = new ItemStack(Material.PAPER);
        ItemMeta hintMeta = hint.getItemMeta();
        if (hintMeta != null) {
            hintMeta.setDisplayName(ChatColor.YELLOW + "How it works");
            hintMeta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Pick the sound used when vanishing.",
                    ChatColor.DARK_GRAY + "Preview = play sound once.",
                    ChatColor.DARK_GRAY + "Select = save to config."
            ));
            hint.setItemMeta(hintMeta);
        }
        inventory.setItem(49, hint);

        return inventory;
    }
}
