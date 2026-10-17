/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.config;

import nl.fronsky.vanish.logic.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Colored message texts from {@code messages.yml}, cached until the next {@link #reload()}.
 * A message that is set to an empty string in {@code messages.yml} is never sent.
 */
public final class Messages {
    private static final String FILE_NAME = "messages.yml";

    private final JavaPlugin plugin;
    private final File file;
    private Map<Message, String> texts = new EnumMap<>(Message.class);

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        reload();
    }

    /**
     * Re-reads {@code messages.yml}. Missing entries fall back to the bundled file and then to the built-in text.
     */
    public void reload() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStream bundled = plugin.getResource(FILE_NAME)) {
            if (bundled != null) {
                InputStreamReader reader = new InputStreamReader(bundled, StandardCharsets.UTF_8);
                yaml.setDefaults(YamlConfiguration.loadConfiguration(reader));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read the bundled " + FILE_NAME + ".", e);
        }

        Map<Message, String> loaded = new EnumMap<>(Message.class);
        for (Message message : Message.values()) {
            String text = yaml.getString(message.getKey());
            loaded.put(message, ColorUtil.colorize(text != null ? text : message.getDefaultText()));
        }
        texts = loaded;
    }

    /**
     * @return the colored text of the message
     */
    public String get(Message message) {
        return texts.get(message);
    }

    /**
     * @return the colored text of the message with {@code placeholder} replaced by {@code value}
     */
    public String format(Message message, String placeholder, String value) {
        return get(message).replace(placeholder, value);
    }

    public void send(CommandSender recipient, Message message) {
        send(recipient, get(message));
    }

    public void send(CommandSender recipient, Message message, String placeholder, String value) {
        send(recipient, format(message, placeholder, value));
    }

    /**
     * Sends already formatted text, unless it is empty.
     */
    public void send(CommandSender recipient, String text) {
        if (!text.isEmpty()) {
            recipient.sendMessage(text);
        }
    }

    /**
     * Sends already formatted text to every online player and to the console, like a vanilla join or quit message.
     */
    public void broadcast(String text) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, text);
        }
        send(Bukkit.getConsoleSender(), text);
    }
}
