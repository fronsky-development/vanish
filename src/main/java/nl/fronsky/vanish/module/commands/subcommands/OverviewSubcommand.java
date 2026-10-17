/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.logic.utils.ChatPaginator;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Paginated reference pages: {@code /vanish help}, {@code /vanish permissions} and {@code /vanish placeholders}.
 */
public final class OverviewSubcommand implements Subcommand {
    private static final int LINES_PER_PAGE = 7;

    private final String name;
    private final String title;
    private final List<String> lines;
    private final VanishConfig config;

    private OverviewSubcommand(String name, String title, List<String> lines, VanishConfig config) {
        this.name = name;
        this.title = title;
        this.lines = lines;
        this.config = config;
    }

    public static OverviewSubcommand help(VanishConfig config) {
        return new OverviewSubcommand("help", "Help", List.of(
                "&7- &f/vanish &8» &7Toggle vanish",
                "&7- &f/vanish <player> &8» &7Toggle others",
                "&7- &f/vanish gui &8» &7Player GUI",
                "&7- &f/vanish sound &8» &7Sound GUI",
                "&7- &f/vanish sound <on|off> &8» &7Enable/disable sound",
                "&7- &f/vanish silent <on|off> &8» &7Silent join",
                "&7- &f/vanish list &8» &7List vanished",
                "&7- &f/vanish join &8» &7Fake join",
                "&7- &f/vanish quit &8» &7Fake quit",
                "&7- &f/vanish tp <player> &8» &7Teleport",
                "&7- &f/vanish color &8» &7Color GUI",
                "&7- &f/vanish info &8» &7Plugin info",
                "&7- &f/vanish reload &8» &7Reload",
                "&7- &f/vanish permissions &8» &7Permissions list",
                "&7- &f/vanish placeholders &8» &7Placeholder list"), config);
    }

    public static OverviewSubcommand permissions(VanishConfig config) {
        return new OverviewSubcommand("permissions", "Permissions", List.of(
                "&7- &fvanish.cmd.vanish &8» &7Use /vanish",
                "&7- &fvanish.cmd.vanish.others &8» &7Vanish others",
                "&7- &fvanish.cmd.vanish.gui &8» &7Open GUI",
                "&7- &fvanish.cmd.vanish.sound &8» &7Sound settings",
                "&7- &fvanish.cmd.vanish.silent &8» &7Silent join",
                "&7- &fvanish.cmd.vanish.list &8» &7List vanished",
                "&7- &fvanish.cmd.vanish.join &8» &7Fake join",
                "&7- &fvanish.cmd.vanish.quit &8» &7Fake quit",
                "&7- &fvanish.cmd.vanish.tp &8» &7Teleport",
                "&7- &fvanish.cmd.vanish.color &8» &7Color GUI",
                "&7- &fvanish.cmd.vanish.reload &8» &7Reload",
                "&7- &fvanish.see &8» &7See vanished",
                "&7- &fvanish.join &8» &7Auto-vanish",
                "&7- &fvanish.* &8» &7All perms"), config);
    }

    public static OverviewSubcommand placeholders(VanishConfig config) {
        return new OverviewSubcommand("placeholders", "Placeholders", List.of(
                "&7- &f%vanish_isvanished% &8» &7true/false",
                "&7- &f%vanish_status% &8» &7Vanished/Visible",
                "&7- &f%vanish_count% &8» &7Vanished count",
                "&7- &f%vanish_cansee% &8» &7Can see vanished",
                "&7- &f%vanish_total_online% &8» &7Visible online",
                "&7- &f%vanish_color% &8» &7Plugin color",
                "&7- &f%vanish_silent% &8» &7Silent join"), config);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ChatPaginator.send(sender, config.getAccentColor(), title, lines, ChatPaginator.parsePage(args),
                LINES_PER_PAGE, "/vanish " + name);
    }
}
