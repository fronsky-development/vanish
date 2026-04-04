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
    public Silent(String[] args, CommandSender sender, Data data) {
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessage());
            return;
        }

        if (args.length > 0) {
            silentState(sender, args, data, player);
            return;
        }

        player.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
    }


    private void silentState(CommandSender sender, String[] args, Data data, Player player) {
        String message;
        boolean silentEnabled;
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
            silentEnabled = true;
            message = Language.VANISH_SILENT_ENABLED.getMessageWithColor();
        } else {
            if (!args[0].equalsIgnoreCase("off") && !args[0].equalsIgnoreCase("false")) {
                sender.sendMessage(Language.WRONG_ARGS.getMessageWithColor().replace("{arg}", "{on:off}"));
                return;
            }

            silentEnabled = false;
            message = Language.VANISH_SILENT_DISABLED.getMessageWithColor();
        }

        data.getPlayers().get().set(player.getUniqueId() + ".silent", silentEnabled);
        data.getPlayers().save();
        data.getPlayers().reload();
        player.sendMessage(ColorUtil.colorize(message));
    }
}
