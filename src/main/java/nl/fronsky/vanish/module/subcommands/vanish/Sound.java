/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Collections;

public class Sound {

    private Sound() {
    }

    public static void execute(String[] args, CommandSender sender, Data data, ChatColor color) {
        if (args.length > 0) {
            handleSoundToggle(sender, args, data);
            return;
        }
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessage());
            return;
        }

        int pageNum = 1;
        Inventory inv = createSoundInventory(player, pageNum, data, color);
        player.openInventory(inv);
        player.setMetadata("fronsky_vanish_sound_inventory", new FixedMetadataValue(data.getPlugin(), true));
    }

    /**
     * Creates a paginated sound selection inventory.
     */
    public static Inventory createSoundInventory(Player player, int pageNum, Data data, ChatColor color) {
        int maxItemsPerPage = 45;
        org.bukkit.Sound[] sounds = org.bukkit.Sound.values();
        int startIndex = (pageNum - 1) * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, sounds.length);
        int totalPages = Math.max(1, (int) Math.ceil(sounds.length / (double) maxItemsPerPage));

        String currentSound = data.getConfig().get().getString("sound", "AMBIENT_CAVE");

        Inventory inventory = Bukkit.createInventory(player, 54,
                color + "Select Sound " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + pageNum + "/" + totalPages + ChatColor.DARK_GRAY + ")");

        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            org.bukkit.Sound s = sounds[i];
            boolean selected = s.name().equalsIgnoreCase(currentSound);
            ItemStack item = new ItemStack(selected ? Material.JUKEBOX : Material.NOTE_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            meta.setDisplayName((selected ? ChatColor.GREEN + "✔ " : ChatColor.WHITE.toString()) + ChatColor.GRAY + s.name());
            java.util.List<String> lore = new java.util.ArrayList<>();
            if (selected) {
                lore.add(ChatColor.DARK_GRAY + "Currently selected");
                lore.add("");
            }
            lore.add(ChatColor.GRAY + "Left click: " + ChatColor.WHITE + "Preview");
            lore.add(ChatColor.GRAY + "Right click: " + ChatColor.WHITE + "Select");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(itemIndex++, item);
        }

        if (pageNum < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Next Page");
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Go to page " + (pageNum + 1)));
                next.setItemMeta(meta);
            }
            inventory.setItem(53, next);
        }
        if (pageNum > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Previous Page");
                meta.setLore(Collections.singletonList(ChatColor.GRAY + "Go to page " + (pageNum - 1)));
                prev.setItemMeta(meta);
            }
            inventory.setItem(45, prev);
        }

        ItemStack hint = new ItemStack(Material.PAPER);
        ItemMeta hintMeta = hint.getItemMeta();
        if (hintMeta != null) {
            hintMeta.setDisplayName(ChatColor.YELLOW + "How it works");
            hintMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Pick the sound used when vanishing.",
                    ChatColor.DARK_GRAY + "Preview = play sound once.",
                    ChatColor.DARK_GRAY + "Select = save to config."
            ));
            hint.setItemMeta(hintMeta);
        }
        inventory.setItem(49, hint);

        return inventory;
    }

    private static void handleSoundToggle(CommandSender sender, String[] args, Data data) {
        String message;
        boolean soundEnabled;
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
            soundEnabled = true;
            message = Language.VANISH_SOUND_ENABLED.getMessageWithColor();
        } else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("false")) {
            soundEnabled = false;
            message = Language.VANISH_SOUND_DISABLED.getMessageWithColor();
        } else {
            sender.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
            return;
        }

        data.getConfig().get().set("sound-enable", soundEnabled);
        data.getConfig().save();
        data.getConfig().reload();
        sender.sendMessage(ColorUtil.colorize(message));
    }
}
