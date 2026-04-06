/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.gui;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.subcommands.vanish.GuiBuilder;
import nl.fronsky.vanish.module.utils.Data;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

public class Player implements Listener {
    private static final String KEY = "fronsky_vanish_player_heads_inventory";
    private final Data data;
    private final ChatColor color;

    public Player() {
        data = VanishModule.getData();
        color = data.getPluginChatColor();
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
            Inventory nextPageInventory = GuiBuilder.createPlayerHeadsInventory(player, new ArrayList<>(Bukkit.getOnlinePlayers()), 45, nextPageNum, color, data);
            player.openInventory(nextPageInventory);
            player.setMetadata(KEY, new FixedMetadataValue(data.getPlugin(), true));
            return;
        }

        if (event.getRawSlot() == 45 && pageNum > 1) {
            int prevPageNum = pageNum - 1;
            Inventory prevPageInventory = GuiBuilder.createPlayerHeadsInventory(player, new ArrayList<>(Bukkit.getOnlinePlayers()), 45, prevPageNum, color, data);
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
}
