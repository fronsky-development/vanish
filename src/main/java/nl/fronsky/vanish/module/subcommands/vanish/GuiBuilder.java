/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for creating GUI inventories.
 * Eliminates code duplication between Gui subcommand and Player event listener.
 */
public final class GuiBuilder {

    private GuiBuilder() {
    }

    /**
     * Creates an inventory filled with player heads representing the online players, paginated.
     *
     * @param viewer          the player who will view the inventory
     * @param onlinePlayers   the list of online players to display
     * @param maxItemsPerPage the maximum number of player heads to display per page
     * @param pageNum         the current page number to display
     * @param color           the color to be used for the inventory title
     * @param data            the plugin data
     * @return the created inventory containing the player heads and navigation arrows
     */
    public static Inventory createPlayerHeadsInventory(Player viewer, List<Player> onlinePlayers,
                                                       int maxItemsPerPage, int pageNum,
                                                       ChatColor color, Data data) {
        int startIndex = (pageNum - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, onlinePlayers.size());
        int totalPages = Math.max(1, (int) Math.ceil(onlinePlayers.size() / (double) maxItemsPerPage));

        Inventory inventory = Bukkit.createInventory(viewer, 54,
                color + "Player Heads (Page " + pageNum + "/" + totalPages + ")");

        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; ++i) {
            Player onlinePlayer = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) {
                continue;
            }
            meta.setOwningPlayer(onlinePlayer);

            boolean vanished = MetaData.getVanishState(onlinePlayer, data).equals(State.HIDDEN);
            meta.setDisplayName((vanished ? ChatColor.DARK_AQUA : ChatColor.GREEN) + onlinePlayer.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Vanished: " + (vanished ? ChatColor.AQUA + "Yes" : ChatColor.RED + "No"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to toggle");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inventory.setItem(itemIndex++, head);
        }

        if (pageNum < totalPages) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Next Page");
                nextPage.setItemMeta(meta);
            }
            inventory.setItem(53, nextPage);
        }
        if (pageNum > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Previous Page");
                prevPage.setItemMeta(meta);
            }
            inventory.setItem(45, prevPage);
        }

        return inventory;
    }
}

