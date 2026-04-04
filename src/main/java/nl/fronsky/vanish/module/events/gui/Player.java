/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.gui;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Player implements Listener {
    private final String KEY = "fronsky_vanish_player_heads_inventory";
    private final Data data;
    private ChatColor color;

    public Player() {
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
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
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

        int onlineCount = Bukkit.getOnlinePlayers().size();
        if (event.getRawSlot() == 53 && pageNum < Math.ceil(onlineCount / 45.0)) {
            int nextPageNum = pageNum + 1;
            Inventory nextPageInventory = createPlayerHeadsInventory(player, new ArrayList<>(Bukkit.getOnlinePlayers()), nextPageNum);
            player.openInventory(nextPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        if (event.getRawSlot() == 45 && pageNum > 1) {
            int prevPageNum = pageNum - 1;
            Inventory prevPageInventory = createPlayerHeadsInventory(player, new ArrayList<>(Bukkit.getOnlinePlayers()), prevPageNum);
            player.openInventory(prevPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        if (current.getType() == Material.PLAYER_HEAD && current.hasItemMeta() && current.getItemMeta() instanceof SkullMeta meta) {
            if (meta.getOwningPlayer() == null) {
                return;
            }
            org.bukkit.entity.Player clickedPlayer = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
            if (clickedPlayer != null) {
                player.performCommand("vanish " + clickedPlayer.getName());
                player.closeInventory();
                player.removeMetadata(KEY, data.getPlugin());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        if (player.hasMetadata(KEY)) {
            player.removeMetadata(KEY, data.getPlugin());
        }
    }

    /**
     * Creates an inventory containing player heads for a given page number.
     *
     * @param player        the player viewing the inventory
     * @param onlinePlayers the list of online players
     * @param pageNum       the page number to display
     * @return the inventory containing player heads and navigation items
     */
    private Inventory createPlayerHeadsInventory(org.bukkit.entity.Player player, List<org.bukkit.entity.Player> onlinePlayers, int pageNum) {
        int maxItemsPerPage = 45;
        int startIndex = (pageNum - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, onlinePlayers.size());
        Inventory inventory = Bukkit.createInventory(player, 54, color + "Player Heads (Page " + pageNum + "/" + (int) Math.ceil(onlinePlayers.size() / (double) maxItemsPerPage) + ")");
        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; ++i) {
            org.bukkit.entity.Player onlinePlayer = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) {
                continue;
            }
            meta.setOwningPlayer(onlinePlayer);

            boolean vanished = MetaData.getVanishState(onlinePlayer, data).equals(State.HIDDEN);
            meta.setDisplayName((vanished ? ChatColor.DARK_AQUA : ChatColor.GREEN) + onlinePlayer.getName());

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Vanished: " + (vanished ? ChatColor.AQUA + "Yes" : ChatColor.RED + "No"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to toggle");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inventory.setItem(itemIndex++, head);
        }
        if (pageNum < Math.ceil(Bukkit.getOnlinePlayers().size() / (double) maxItemsPerPage)) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta2 = nextPage.getItemMeta();
            if (meta2 != null) {
                meta2.setDisplayName(ChatColor.GREEN + "Next Page");
                nextPage.setItemMeta(meta2);
            }
            inventory.setItem(53, nextPage);
        }
        if (pageNum > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta2 = prevPage.getItemMeta();
            if (meta2 != null) {
                meta2.setDisplayName(ChatColor.GREEN + "Previous Page");
                prevPage.setItemMeta(meta2);
            }
            inventory.setItem(45, prevPage);
        }

        return inventory;
    }
}
