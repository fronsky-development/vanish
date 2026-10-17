/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /vanish tp <player>} teleports the sender to an online player.
 */
public final class TpSubcommand implements Subcommand {
    private final Messages messages;

    public TpSubcommand(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, Message.NO_PLAYER);
            return;
        }
        if (args.length == 0) {
            messages.send(sender, Message.WRONG_ARGS, "{arg}", "{player}");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(sender, Message.PLAYER_NOT_ONLINE);
            return;
        }
        player.teleport(target.getLocation());
        messages.send(player, Message.TELEPORT, "{player}", target.getDisplayName());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
