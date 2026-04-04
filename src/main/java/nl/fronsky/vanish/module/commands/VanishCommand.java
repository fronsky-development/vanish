/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands;

import nl.fronsky.vanish.logic.commands.CommandHandler;
import nl.fronsky.vanish.logic.commands.annotations.CommandClass;
import nl.fronsky.vanish.logic.commands.annotations.SubCommandMethod;
import nl.fronsky.vanish.logic.logging.Logger;
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
    private ChatColor color;
    private boolean soundEnabled;
    private org.bukkit.Sound sound;

    public VanishCommand() {
        data = VanishModule.getData();
        setConfigData();
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        setConfigData();
        VanishPlayer vanishPlayer = null;
        if (sender instanceof Player) {
            vanishPlayer = new VanishPlayer((Player) sender);
        }
        if (args.length > 0) {
            Result<VanishPlayer> result = VanishPlayer.getPlayer(args[0]);
            if (result.Success()) {
                VanishPlayer target = result.Value();
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
            if (soundEnabled) {
                vanishPlayer.getPlayer().playSound(vanishPlayer.getPlayer().getLocation(), sound, 1.0f, 1.0f);
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
        setConfigData();
        boolean isVanished = MetaData.getVanishState(target.getPlayer(), data).equals(State.HIDDEN);
        Language messageKey = isVanished ? Language.VISIBLE_SUCCESS : Language.VANISH_SUCCESS;
        String message = messageKey.getMessageWithColor();
        message = message.replace("{player}", target.getPlayer().getDisplayName());
        sender.sendMessage(message);
        if (isVanished) {
            target.show(false);
        } else {
            target.hide(false);
            if (soundEnabled) {
                target.getPlayer().playSound(target.getPlayer().getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    @SubCommandMethod
    public void gui(CommandSender sender, String label, String[] args) {
        new Gui(sender, color, data);
        setConfigData();
    }

    @SubCommandMethod
    public void sound(CommandSender sender, String label, String[] args) {
        new Sound(args, sender, data, color);
        setConfigData();
    }

    @SubCommandMethod
    public void silent(CommandSender sender, String label, String[] args) {
        new Silent(args, sender, data);
        setConfigData();
    }

    @SubCommandMethod
    public void list(CommandSender sender, String label, String[] args) {
        new List(sender, data);
        setConfigData();
    }

    @SubCommandMethod
    public void join(CommandSender sender, String label, String[] args) {
        new Join(sender);
        setConfigData();
    }

    @SubCommandMethod
    public void quit(CommandSender sender, String label, String[] args) {
        new Quit(sender);
        setConfigData();
    }

    @SubCommandMethod
    public void tp(CommandSender sender, String label, String[] args) {
        new Tp(sender, args);
        setConfigData();
    }

    @SubCommandMethod
    public void color(CommandSender sender, String label, String[] args) {
        new Color(sender, args, color, data);
        setConfigData();
    }

    @SubCommandMethod
    public void info(CommandSender sender, String label, String[] args) {
        new Info(sender, color);
        setConfigData();
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
        new Help(sender, color, page);
        setConfigData();
    }

    @SubCommandMethod
    public void reload(CommandSender sender, String label, String[] args) {
        new Reload(data, sender);
        setConfigData();
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
        new Permissions(sender, color, data, page);
        setConfigData();
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
        new Placeholders(sender, color, data, page);
        setConfigData();
    }

    /**
     * Sets configuration data for sound and color settings from the config file.
     */
    private void setConfigData() {
        soundEnabled = data.getConfig().get().getBoolean("sound-enable");
        sound = org.bukkit.Sound.AMBIENT_CAVE;
        if (data.getConfig().get().getString("sound") != null) {
            try {
                sound = org.bukkit.Sound.valueOf(data.getConfig().get().getString("sound"));
            } catch (IllegalArgumentException exception) {
                Logger.severe(exception.getMessage());
            }
        }

        String colorKey = data.getConfig().get().getString("plugin-color");
        color = ChatColor.BLUE;
        if (colorKey != null) {
            try {
                if (colorKey.equals("PINK")) {
                    color = ChatColor.LIGHT_PURPLE;
                } else if (colorKey.equals("PURPLE")) {
                    color = ChatColor.DARK_PURPLE;
                } else {
                    color = ChatColor.valueOf(colorKey);
                }
            } catch (IllegalArgumentException exception) {
                Logger.severe(exception.getMessage());
            }
        }
    }
}
