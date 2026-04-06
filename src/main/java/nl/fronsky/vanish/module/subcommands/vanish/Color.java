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
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class Color {

    private Color() {
    }

    public static void execute(CommandSender sender, String[] args, ChatColor color, Data data) {
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessage());
            return;
        }
        if (args.length == 0) {
            openColorGui(player, color, data);
            return;
        }

        BarColor barColor;
        try {
            barColor = BarColor.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Language.NO_VALID_COLOR.getMessageWithColor().replace("{color}", args[0]));
            return;
        }

        data.getConfig().get().set("plugin-color", barColor.name());
        data.getConfig().save();
        data.getConfig().reload();
        data.updateBarColor();
    }

    private static void openColorGui(Player player, ChatColor color, Data data) {
        Inventory gui = Bukkit.createInventory(player, 9, color + "Color Selection");
        for (BarColor barColor : BarColor.values()) {
            String key = barColor.name() + "_STAINED_GLASS_PANE";
            Material mat = Material.getMaterial(key);
            if (mat == null) {
                continue;
            }

            ItemStack item = new ItemStack(mat, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }

            ChatColor chatColor = ColorUtil.parsePluginColor(barColor.name());
            if (!chatColor.isColor()) {
                chatColor = ChatColor.WHITE;
            }

            String prettyName = barColor.name().toLowerCase().replace("_", " ");
            prettyName = prettyName.substring(0, 1).toUpperCase() + prettyName.substring(1);

            meta.setDisplayName(chatColor + prettyName);
            meta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Sets the plugin accent color.",
                    ChatColor.DARK_GRAY + "Used in menus, messages, and bossbar.",
                    "",
                    ChatColor.YELLOW + "Click to apply"
            ));

            item.setItemMeta(meta);
            gui.addItem(item);
        }

        player.openInventory(gui);
        player.setMetadata("fronsky_vanish_color_selection", new FixedMetadataValue(data.getPlugin(), true));
    }
}
