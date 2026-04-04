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

public class Reload {
    public Reload(Data data, CommandSender sender) {
        try {
            data.reloadConfigurations();

            String message = Language.PLUGIN_RELOADED.getMessageWithColor();

            if (sender instanceof Player) {
                sender.sendMessage(ColorUtil.colorize(message));
            } else {
                // Console execution should go to log, not to CommandSender output as a chat-like message.
                Logger.info(ColorUtil.colorize(message));
            }

            Logger.info("Plugin reloaded by " + sender.getName());
        } catch (Exception e) {
            Logger.exception("Failed to reload plugin configurations", e);
            if (sender instanceof Player) {
                sender.sendMessage(ColorUtil.colorize("&cFailed to reload plugin. Check console for details."));
            } else {
                Logger.severe("Failed to reload plugin. Check console for details.");
            }
        }
    }
}
