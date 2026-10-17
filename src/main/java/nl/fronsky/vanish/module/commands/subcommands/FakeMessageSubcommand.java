/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /vanish join} and {@code /vanish quit}: broadcast a fake join or quit message for the sender.
 */
public final class FakeMessageSubcommand implements Subcommand {
    private final String name;
    private final Message message;
    private final Messages messages;

    private FakeMessageSubcommand(String name, Message message, Messages messages) {
        this.name = name;
        this.message = message;
        this.messages = messages;
    }

    public static FakeMessageSubcommand join(Messages messages) {
        return new FakeMessageSubcommand("join", Message.FAKE_JOIN, messages);
    }

    public static FakeMessageSubcommand quit(Messages messages) {
        return new FakeMessageSubcommand("quit", Message.FAKE_QUIT, messages);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            messages.broadcast(messages.format(message, "{player}", player.getDisplayName()));
        } else {
            messages.send(sender, Message.NO_PLAYER);
        }
    }
}
