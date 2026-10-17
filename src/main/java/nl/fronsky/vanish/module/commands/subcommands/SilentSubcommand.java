/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.storage.PlayerDataStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * {@code /vanish silent <on|off>} sets whether the sender's join message is hidden when auto-vanished.
 */
public final class SilentSubcommand implements Subcommand {
    private final Messages messages;
    private final PlayerDataStore playerData;

    public SilentSubcommand(Messages messages, PlayerDataStore playerData) {
        this.messages = messages;
        this.playerData = playerData;
    }

    @Override
    public String getName() {
        return "silent";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, Message.NO_PLAYER);
            return;
        }
        String argument = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (argument) {
            case "on", "true" -> setSilentJoin(player, true);
            case "off", "false" -> setSilentJoin(player, false);
            default -> messages.send(sender, Message.WRONG_ARGS, "{arg}", "{on:off}");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length == 1 ? List.of("on", "off") : List.of();
    }

    private void setSilentJoin(Player player, boolean silent) {
        playerData.setSilentJoin(player.getUniqueId(), silent);
        messages.send(player, silent ? Message.VANISH_SILENT_ENABLED : Message.VANISH_SILENT_DISABLED);
    }
}
