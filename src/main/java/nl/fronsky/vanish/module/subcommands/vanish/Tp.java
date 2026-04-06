/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.logic.utils.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Tp {

    private Tp() {
    }

    public static void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Logger.info(Language.NO_PLAYER.getPlainMessage());
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{player}"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Language.PLAYER_NOT_ONLINE.getMessageWithColor());
            return;
        }

        player.teleport(target.getLocation());
        String message = Language.TELEPORT.getMessageWithColor();
        message = message.replace("{player}", target.getDisplayName());
        player.sendMessage(ColorUtil.colorize(message));
    }
}
