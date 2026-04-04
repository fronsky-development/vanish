/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.utils.ChatPaginator;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Permissions {
    public Permissions(CommandSender sender, ChatColor color, Data data, int page) {
        // data reserved for future localization/config, keep parameter for API stability

        List<String> lines = new ArrayList<>();

        // Short lines to fit chat
        lines.add("&7- &fvanish.cmd.vanish &8» &7Use /vanish");
        lines.add("&7- &fvanish.cmd.vanish.others &8» &7Vanish others");
        lines.add("&7- &fvanish.cmd.vanish.gui &8» &7Open GUI");
        lines.add("&7- &fvanish.cmd.vanish.sound &8» &7Sound settings");
        lines.add("&7- &fvanish.cmd.vanish.silent &8» &7Silent join");
        lines.add("&7- &fvanish.cmd.vanish.list &8» &7List vanished");
        lines.add("&7- &fvanish.cmd.vanish.join &8» &7Fake join");
        lines.add("&7- &fvanish.cmd.vanish.quit &8» &7Fake quit");
        lines.add("&7- &fvanish.cmd.vanish.tp &8» &7Teleport");
        lines.add("&7- &fvanish.cmd.vanish.color &8» &7Color GUI");
        lines.add("&7- &fvanish.cmd.vanish.reload &8» &7Reload");
        lines.add("&7- &fvanish.see &8» &7See vanished");
        lines.add("&7- &fvanish.join &8» &7Auto-vanish");
        lines.add("&7- &fvanish.* &8» &7All perms");

        ChatPaginator.send(sender, color, "Permissions", lines, page, 7, "/vanish permissions");
    }
}
