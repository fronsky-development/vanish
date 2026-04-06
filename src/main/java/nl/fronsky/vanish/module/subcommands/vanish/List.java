/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class List {

    private List() {
    }

    public static void execute(CommandSender sender, Data data) {
        int amountVanished = MetaData.getVanishedPlayersAmount(data);
        boolean isPlayer = sender instanceof Player;
        String countMessage = "There are " + amountVanished + " of " + MetaData.getOnlinePlayers().size() + " players in vanish.";

        if (amountVanished == 0) {
            if (isPlayer) {
                sender.sendMessage(ChatColor.WHITE + countMessage);
            } else {
                Logger.info(countMessage);
            }
            return;
        }

        if (isPlayer) {
            sender.sendMessage(ChatColor.WHITE + countMessage.replace("vanish.", "vanish:"));
            for (VanishPlayer vanishPlayer : data.getVanishedPlayers().values()) {
                if (vanishPlayer.hasPermission("vanish.*")) {
                    sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.GREEN + vanishPlayer.getDisplayName());
                } else {
                    sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.GRAY + vanishPlayer.getDisplayName());
                }
            }
        } else {
            Logger.info(countMessage.replace("vanish.", "vanish:"));
            for (VanishPlayer vanishPlayer : data.getVanishedPlayers().values()) {
                Logger.info("- " + vanishPlayer.getDisplayName());
            }
        }
    }
}
