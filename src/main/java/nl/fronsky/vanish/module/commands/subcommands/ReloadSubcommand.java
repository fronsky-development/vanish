/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.VanishPlugin;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import org.bukkit.command.CommandSender;

/**
 * {@code /vanish reload} re-reads all configuration and data files.
 */
public final class ReloadSubcommand implements Subcommand {
    private final VanishPlugin plugin;
    private final Messages messages;

    public ReloadSubcommand(VanishPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reload();
        messages.send(sender, Message.PLUGIN_RELOADED);
        plugin.getLogger().info("Configuration reloaded by " + sender.getName());
    }
}
