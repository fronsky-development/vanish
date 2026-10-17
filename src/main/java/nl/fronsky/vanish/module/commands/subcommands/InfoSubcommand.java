/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@code /vanish info} shows the plugin version, build, author and website.
 */
public final class InfoSubcommand implements Subcommand {
    private static final String AUTHOR = "Fronsky";
    private static final String WEBSITE = "https://fronsky.nl";
    private static final String BUILD_INFO_RESOURCE = "vanish-build.properties";

    private final VanishConfig config;
    private final String version;
    private final String build;

    public InfoSubcommand(JavaPlugin plugin, VanishConfig config) {
        this.config = config;
        Properties buildInfo = new Properties();
        try (InputStream in = plugin.getResource(BUILD_INFO_RESOURCE)) {
            if (in != null) {
                buildInfo.load(in);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read " + BUILD_INFO_RESOURCE + ": " + e.getMessage());
        }
        this.version = buildInfo.getProperty("version", plugin.getDescription().getVersion());
        this.build = "FV-" + version.replace(".", "") + "." + buildInfo.getProperty("buildId", "unknown");
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("--- Vanish Plugin Info ---");
            sender.sendMessage("Version: " + version);
            sender.sendMessage("Build: " + build);
            sender.sendMessage("Author: " + AUTHOR);
            sender.sendMessage("Website: " + WEBSITE);
            return;
        }

        String header = ChatColor.DARK_GRAY + "<--------- " + config.getAccentColor() + ChatColor.BOLD + "Info"
                + ChatColor.RESET + ChatColor.DARK_GRAY + " --------->";
        player.sendMessage(header);
        player.sendMessage(ColorUtil.colorize("&7Version: &f" + version));
        player.sendMessage(ColorUtil.colorize("&7Build: &f" + build));
        player.sendMessage(ColorUtil.colorize("&7Author: &f" + AUTHOR));

        TextComponent website = new TextComponent(ColorUtil.colorize("&7Website: &f" + WEBSITE));
        website.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WEBSITE));
        player.spigot().sendMessage(website);

        int width = ChatColor.stripColor(header).length();
        player.sendMessage(ChatColor.DARK_GRAY + "<" + "-".repeat(Math.max(0, width - 2)) + ">");
    }
}
