/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Quit {

    private Quit() {
    }

    public static void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Logger.info(Language.NO_PLAYER.getPlainMessage());
            return;
        }

        String message = Language.FAKE_QUIT.getMessageWithColor();
        message = message.replace("{player}", player.getDisplayName());
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(message);
        }
    }
}
