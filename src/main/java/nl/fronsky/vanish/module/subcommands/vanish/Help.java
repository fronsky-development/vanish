/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.utils.ChatPaginator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Help {
    public Help(CommandSender sender, ChatColor color, int page) {
        List<String> lines = new ArrayList<>();

        lines.add("&7- &f/vanish &8» &7Toggle vanish");
        lines.add("&7- &f/vanish <player> &8» &7Toggle others");
        lines.add("&7- &f/vanish gui &8» &7Player GUI");
        lines.add("&7- &f/vanish sound &8» &7Sound GUI");
        lines.add("&7- &f/vanish sound <on|off> &8» &7Enable/disable sound");
        lines.add("&7- &f/vanish silent <on|off> &8» &7Silent join");
        lines.add("&7- &f/vanish list &8» &7List vanished");
        lines.add("&7- &f/vanish join &8» &7Fake join");
        lines.add("&7- &f/vanish quit &8» &7Fake quit");
        lines.add("&7- &f/vanish tp <player> &8» &7Teleport");
        lines.add("&7- &f/vanish color &8» &7Color GUI");
        lines.add("&7- &f/vanish info &8» &7Plugin info");
        lines.add("&7- &f/vanish reload &8» &7Reload");
        lines.add("&7- &f/vanish permissions &8» &7Permissions list");
        lines.add("&7- &f/vanish placeholders &8» &7Placeholder list");

        ChatPaginator.send(sender, color, "Help", lines, page, 7, "/vanish help");
    }
}
