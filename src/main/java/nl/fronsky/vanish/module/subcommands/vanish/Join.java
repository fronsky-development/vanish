/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Join {
    public Join(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Logger.warning(Language.NO_PLAYER.getMessageWithColor());
            return;
        }

        String message = Language.FAKE_JOIN.getMessageWithColor();
        message = message.replace("{player}", player.getDisplayName());
        Bukkit.broadcastMessage(message);
    }
}
