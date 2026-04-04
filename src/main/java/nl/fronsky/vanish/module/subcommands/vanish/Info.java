/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.util.Properties;

public class Info {
    public Info(CommandSender sender, ChatColor color) {
        Main plugin = Main.getInstance();
        String version = plugin != null ? plugin.getDescription().getVersion() : "unknown";
        String buildId = "unknown";

        if (plugin != null) {
            try (InputStream in = plugin.getResource("vanish-build.properties")) {
                if (in != null) {
                    Properties props = new Properties();
                    props.load(in);
                    version = props.getProperty("version", version);
                    buildId = props.getProperty("buildId", buildId);
                }
            } catch (Exception ignored) {
            }
        }

        String buildNumber = "FV-" + version.replace(".", "") + "." + buildId;
        String author = "Fronsky";
        String url = "https://fronsky.nl/projects/vanish";

        String header = ChatColor.DARK_GRAY + "<--------- " + color + ChatColor.BOLD + "Info" + ChatColor.RESET + ChatColor.DARK_GRAY + " --------->";
        sender.sendMessage(header);

        sender.sendMessage(ColorUtil.colorize("&7Version: &f" + version));
        sender.sendMessage(ColorUtil.colorize("&7Build: &f" + buildNumber));
        sender.sendMessage(ColorUtil.colorize("&7Author: &f" + author));

        if (sender instanceof Player player) {
            TextComponent message = new TextComponent(ColorUtil.colorize("&7Website: &f" + url));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            player.spigot().sendMessage(message);
        } else {
            sender.sendMessage(ColorUtil.colorize("&7Website: &f" + url));
        }

        // Footer aligned with header length
        String plain = ChatColor.stripColor(header);
        int len = plain.length();
        if (len <= 1) {
            sender.sendMessage(ChatColor.DARK_GRAY + plain);
        } else {
            sender.sendMessage(ChatColor.DARK_GRAY + "<" + "-".repeat(Math.max(0, len - 2)) + ">");
        }
    }
}
