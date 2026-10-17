/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.gui.MenuListener;
import nl.fronsky.vanish.module.gui.SoundMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * {@code /vanish sound} opens the sound menu; {@code /vanish sound <on|off>} toggles the vanish sound.
 */
public final class SoundSubcommand implements Subcommand {
    private final VanishConfig config;
    private final Messages messages;
    private final MenuListener menus;

    public SoundSubcommand(VanishConfig config, Messages messages, MenuListener menus) {
        this.config = config;
        this.messages = messages;
        this.menus = menus;
    }

    @Override
    public String getName() {
        return "sound";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "on", "true" -> setSoundEnabled(sender, true);
                case "off", "false" -> setSoundEnabled(sender, false);
                default -> messages.send(sender, Message.WRONG_ARGS, "{arg}", "{on:off}");
            }
        } else if (sender instanceof Player player) {
            menus.open(player, new SoundMenu(menus, config, 1));
        } else {
            messages.send(sender, Message.NO_PLAYER);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length == 1 ? List.of("on", "off") : List.of();
    }

    private void setSoundEnabled(CommandSender sender, boolean enabled) {
        config.setSoundEnabled(enabled);
        messages.send(sender, enabled ? Message.VANISH_SOUND_ENABLED : Message.VANISH_SOUND_DISABLED);
    }
}
