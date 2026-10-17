/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.VanishPermissions;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * {@code /vanish list} lists the vanished players; players with all vanish permissions are shown in green.
 */
public final class ListSubcommand implements Subcommand {
    private final VanishManager vanishManager;

    public ListSubcommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<VanishPlayer> vanished = vanishManager.getVanishedPlayers();
        String summary = "There are " + vanished.size() + " of " + Bukkit.getOnlinePlayers().size()
                + " players in vanish";
        sender.sendMessage(ChatColor.WHITE + summary + (vanished.isEmpty() ? "." : ":"));
        for (VanishPlayer vanishPlayer : vanished) {
            ChatColor color = vanishPlayer.hasPermission(VanishPermissions.ALL) ? ChatColor.GREEN : ChatColor.GRAY;
            sender.sendMessage(ChatColor.WHITE + "- " + color + vanishPlayer.getDisplayName());
        }
    }
}
