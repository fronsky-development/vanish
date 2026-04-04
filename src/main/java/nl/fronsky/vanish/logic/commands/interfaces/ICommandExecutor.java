/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.commands.interfaces;

import org.bukkit.command.CommandSender;

public interface ICommandExecutor {
    /**
     * Executes the command when called by a sender.
     *
     * @param sender the sender who issued the command
     * @param label  the alias used to invoke the command
     * @param args   the arguments provided with the command
     */
    void onCommand(CommandSender sender, String label, String[] args);
}
