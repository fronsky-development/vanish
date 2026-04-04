/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class List {
    public List(CommandSender sender, Data data) {
        int amountVanished = MetaData.getVanishedPlayersAmount(data);
        if (amountVanished == 0) {
            sender.sendMessage(ChatColor.WHITE + "There are " + amountVanished + " of " + MetaData.getOnlinePlayers().size() + " players in vanish.");
            return;
        }

        sender.sendMessage(ChatColor.WHITE + "There are " + amountVanished + " of " + MetaData.getOnlinePlayers().size() + " players in vanish:");
        for (VanishPlayer vanishPlayer : data.getVanishedPlayers().values()) {
            if (vanishPlayer.hasPermission("vanish.*")) {
                sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.GREEN + vanishPlayer.getDisplayName());
            } else {
                sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.GRAY + vanishPlayer.getDisplayName());
            }
        }
    }
}
