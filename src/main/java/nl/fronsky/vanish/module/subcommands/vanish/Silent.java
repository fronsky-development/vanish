/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Silent {

    private Silent() {
    }

    public static void execute(String[] args, CommandSender sender, Data data) {
        if (!(sender instanceof Player player)) {
            Logger.info(Language.NO_PLAYER.getPlainMessage());
            return;
        }

        if (args.length == 0) {
            player.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
            return;
        }

        String message;
        boolean silentEnabled;
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
            silentEnabled = true;
            message = Language.VANISH_SILENT_ENABLED.getMessageWithColor();
        } else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("false")) {
            silentEnabled = false;
            message = Language.VANISH_SILENT_DISABLED.getMessageWithColor();
        } else {
            sender.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
            return;
        }

        data.getPlayers().get().set(player.getUniqueId() + ".silent", silentEnabled);
        data.getPlayers().save();
        data.getPlayers().reload();
        player.sendMessage(ColorUtil.colorize(message));
    }
}
