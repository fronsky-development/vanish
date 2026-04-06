/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands;

import nl.fronsky.vanish.logic.commands.CommandHandler;
import nl.fronsky.vanish.logic.commands.annotations.CommandClass;
import nl.fronsky.vanish.logic.commands.annotations.SubCommandMethod;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.logic.utils.Result;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.subcommands.vanish.*;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandClass(name = "vanish", permission = "vanish.cmd.vanish")
public class VanishCommand extends CommandHandler {
    private final Data data;

    public VanishCommand() {
        data = VanishModule.getData();
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        VanishPlayer vanishPlayer = null;
        if (sender instanceof Player) {
            vanishPlayer = new VanishPlayer((Player) sender);
        }
        if (args.length > 0) {
            Result<VanishPlayer> result = VanishPlayer.getPlayer(args[0]);
            if (result.success()) {
                VanishPlayer target = result.value();
                if (vanishPlayer != null && !vanishPlayer.hasPermission("vanish.cmd.vanish.others")) {
                    vanishPlayer.sendMessage(Language.NO_PERMISSION.getMessageWithColor());
                    return;
                }
                others(sender, target);
                return;
            }
        }
        if (vanishPlayer == null) {
            Logger.warning(Language.NO_PLAYER.getMessageWithColor());
            return;
        }
        if (MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            vanishPlayer.show(false);
        } else {
            vanishPlayer.hide(false);
            if (data.isSoundEnabled()) {
                vanishPlayer.getPlayer().playSound(vanishPlayer.getPlayer().getLocation(), data.getSound(), 1.0f, 1.0f);
            }
        }
    }

    /**
     * Toggles the vanish state of a specified player and sends a message to the command sender.
     *
     * @param sender the command sender who triggered the vanish toggle
     * @param target the {@link VanishPlayer} whose vanish state is being toggled
     */
    private void others(CommandSender sender, VanishPlayer target) {
        boolean isVanished = MetaData.getVanishState(target.getPlayer(), data).equals(State.HIDDEN);
        Language messageKey = isVanished ? Language.VISIBLE_SUCCESS : Language.VANISH_SUCCESS;
        String message = messageKey.getMessageWithColor();
        message = message.replace("{player}", target.getPlayer().getDisplayName());
        sender.sendMessage(message);
        if (isVanished) {
            target.show(false);
        } else {
            target.hide(false);
            if (data.isSoundEnabled()) {
                target.getPlayer().playSound(target.getPlayer().getLocation(), data.getSound(), 1.0f, 1.0f);
            }
        }
    }

    @SubCommandMethod
    public void gui(CommandSender sender, String label, String[] args) {
        Gui.execute(sender, data.getPluginChatColor(), data);
    }

    @SubCommandMethod
    public void sound(CommandSender sender, String label, String[] args) {
        Sound.execute(args, sender, data, data.getPluginChatColor());
    }

    @SubCommandMethod
    public void silent(CommandSender sender, String label, String[] args) {
        Silent.execute(args, sender, data);
    }

    @SubCommandMethod
    public void list(CommandSender sender, String label, String[] args) {
        List.execute(sender, data);
    }

    @SubCommandMethod
    public void join(CommandSender sender, String label, String[] args) {
        Join.execute(sender);
    }

    @SubCommandMethod
    public void quit(CommandSender sender, String label, String[] args) {
        Quit.execute(sender);
    }

    @SubCommandMethod
    public void tp(CommandSender sender, String label, String[] args) {
        Tp.execute(sender, args);
    }

    @SubCommandMethod
    public void color(CommandSender sender, String label, String[] args) {
        Color.execute(sender, args, data.getPluginChatColor(), data);
    }

    @SubCommandMethod
    public void info(CommandSender sender, String label, String[] args) {
        Info.execute(sender, data.getPluginChatColor());
    }

    @SubCommandMethod
    public void help(CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        Help.execute(sender, data.getPluginChatColor(), page);
    }

    @SubCommandMethod
    public void reload(CommandSender sender, String label, String[] args) {
        Reload.execute(data, sender);
    }

    @SubCommandMethod
    public void permissions(CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        Permissions.execute(sender, data.getPluginChatColor(), data, page);
    }

    @SubCommandMethod
    public void placeholders(CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        Placeholders.execute(sender, data.getPluginChatColor(), data, page);
    }
}
