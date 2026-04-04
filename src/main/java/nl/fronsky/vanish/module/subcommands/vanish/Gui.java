/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Gui {
    private final Data data;

    public Gui(CommandSender sender, ChatColor color, Data data) {
        this.data = data;
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessageWithColor());
            return;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int maxItemsPerPage = 45;
        int pageNum = 1;
        Inventory inventory = createPlayerHeadsInventory(player, onlinePlayers, maxItemsPerPage, pageNum, color);
        player.openInventory(inventory);
        player.setMetadata("fronsky_vanish_player_heads_inventory", new FixedMetadataValue(data.getPlugin(), true));
    }

    /**
     * Creates an inventory filled with player heads representing the online players, paginated.
     *
     * @param player          the player who will view the inventory
     * @param onlinePlayers   the list of online players to display
     * @param maxItemsPerPage the maximum number of player heads to display per page
     * @param pageNum         the current page number to display
     * @param color           the color to be used for the inventory title
     * @return the created inventory containing the player heads and navigation arrows
     */
    private Inventory createPlayerHeadsInventory(Player player, List<Player> onlinePlayers, int maxItemsPerPage, int pageNum, ChatColor color) {
        int startIndex = (pageNum - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, onlinePlayers.size());
        Inventory inventory = Bukkit.createInventory(player, 54, color + "Player Heads (Page " + pageNum + "/" + (int) Math.ceil(onlinePlayers.size() / (double) maxItemsPerPage) + ")");
        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; ++i) {
            Player onlinePlayer = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) {
                continue;
            }
            meta.setOwningPlayer(onlinePlayer);

            boolean vanished = MetaData.getVanishState(onlinePlayer, this.data).equals(State.HIDDEN);
            meta.setDisplayName((vanished ? ChatColor.DARK_AQUA : ChatColor.GREEN) + onlinePlayer.getName());

            List<String> lore = new ArrayList<>();
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
