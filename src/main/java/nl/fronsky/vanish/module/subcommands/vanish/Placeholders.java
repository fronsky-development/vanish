/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.utils.ChatPaginator;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class Placeholders {

    private Placeholders() {
    }

    public static void execute(CommandSender sender, ChatColor color, Data data, int page) {
        java.util.List<String> lines = new ArrayList<>();

        lines.add("&7- &f%vanish_isvanished% &8» &7true/false");
        lines.add("&7- &f%vanish_status% &8» &7Vanished/Visible");
        lines.add("&7- &f%vanish_count% &8» &7Vanished count");
        lines.add("&7- &f%vanish_cansee% &8» &7Can see vanished");
        lines.add("&7- &f%vanish_total_online% &8» &7Visible online");
        lines.add("&7- &f%vanish_color% &8» &7Plugin color");
        lines.add("&7- &f%vanish_silent% &8» &7Silent join");

        ChatPaginator.send(sender, color, "Placeholders", lines, page, 7, "/vanish placeholders");
    }
}
