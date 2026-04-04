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

public class Sound {
    public Sound(String[] args, CommandSender sender, Data data, ChatColor color) {
        if (args.length > 0) {
            soundState(sender, args, data);
            return;
        }
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessageWithColor());
            return;
        }

        // Open the modern Sound GUI (handled by the Sound listener)
        // We just open page 1 using the same title format the listener expects.
        int pageNum = 1;
        Inventory inv = Bukkit.createInventory(player, 54,
                color + "Select Sound " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY + pageNum + "/" + (int) Math.ceil(org.bukkit.Sound.values().length / 45.0) + ChatColor.DARK_GRAY + ")");
        // Let the listener rebuild the right contents by calling its internal code path:
        // easiest is to open the inventory that the listener creates; since it's private there,
        // we instead mirror its layout here.

        // Build page 1 with the same layout as the listener
        org.bukkit.Sound[] sounds = org.bukkit.Sound.values();
        String currentSound = data.getConfig().get().getString("sound", "AMBIENT_CAVE");
        int startIndex = 0;
        int endIndex = Math.min(45, sounds.length);
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
            inv.setItem(itemIndex++, item);
        }

        // nav
        if (pageNum < Math.ceil(sounds.length / 45.0)) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Next Page");
                meta.setLore(java.util.Collections.singletonList(ChatColor.GRAY + "Go to page " + (pageNum + 1)));
                next.setItemMeta(meta);
            }
            inv.setItem(53, next);
        }

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
        inv.setItem(49, hint);

        player.openInventory(inv);
        player.setMetadata("fronsky_vanish_sound_inventory", new FixedMetadataValue(data.getPlugin(), true));
    }

    /**
     * Toggles the vanish sound state based on the provided arguments and updates the configuration.
     *
     * @param sender the command sender who issued the sound state change
     * @param args   the arguments provided by the command sender (expected to be "on"/"true" or "off"/"false")
     * @param data   the data object containing the configuration and message utilities
     */
    private void soundState(CommandSender sender, String[] args, Data data) {
        String message;
        boolean soundEnabled;
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
            soundEnabled = true;
            message = Language.VANISH_SOUND_ENABLED.getMessageWithColor();
        } else {
            if (!args[0].equalsIgnoreCase("off") && !args[0].equalsIgnoreCase("false")) {
                sender.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
                return;
            }

            soundEnabled = false;
            message = Language.VANISH_SOUND_DISABLED.getMessageWithColor();
        }

        data.getConfig().get().set("sound-enable", soundEnabled);
        data.getConfig().save();
        data.getConfig().reload();
        sender.sendMessage(ColorUtil.colorize(message));
    }
}
