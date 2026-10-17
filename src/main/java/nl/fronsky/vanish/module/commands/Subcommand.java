/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A {@code /vanish <name>} subcommand. It requires the permission {@code vanish.cmd.vanish.<name>}.
 */
public interface Subcommand {

    /**
     * @return the lower-case name typed after {@code /vanish}
     */
    String getName();

    /**
     * @param args the arguments after the subcommand name
     */
    void execute(CommandSender sender, String[] args);

    /**
     * @param args the arguments after the subcommand name; the last one is being completed
     * @return the candidates for the last argument
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
