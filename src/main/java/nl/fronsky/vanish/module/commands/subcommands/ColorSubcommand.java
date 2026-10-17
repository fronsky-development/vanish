/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.gui.ColorMenu;
import nl.fronsky.vanish.module.gui.MenuListener;
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * {@code /vanish color} opens the color menu; {@code /vanish color <color>} sets the plugin accent color.
 */
public final class ColorSubcommand implements Subcommand {
    private final VanishConfig config;
    private final Messages messages;
    private final VanishManager vanishManager;
    private final MenuListener menus;

    public ColorSubcommand(VanishConfig config, Messages messages, VanishManager vanishManager, MenuListener menus) {
        this.config = config;
        this.messages = messages;
        this.vanishManager = vanishManager;
        this.menus = menus;
    }

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                menus.open(player, new ColorMenu(config.getAccentColor(), this::applyColor));
            } else {
                messages.send(sender, Message.NO_PLAYER);
            }
            return;
        }

        try {
            applyColor(BarColor.valueOf(args[0].toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            messages.send(sender, Message.NO_VALID_COLOR, "{color}", args[0]);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return Arrays.stream(BarColor.values()).map(color -> color.name().toLowerCase(Locale.ROOT)).toList();
    }

    private void applyColor(BarColor color) {
        config.setPluginColor(color);
        vanishManager.refreshBossBar();
    }
}
