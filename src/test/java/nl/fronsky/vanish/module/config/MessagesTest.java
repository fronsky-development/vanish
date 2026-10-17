/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.config;

import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest extends VanishTestBase {

    @Test
    void customMessagesAreUsedAfterAReload() throws IOException {
        writeMessages("you_vanished: '&dNow hidden'\n");
        plugin.reload();
        TestPlayer admin = joinOperator("Admin");
        clearMessages(admin);

        admin.performCommand("vanish");

        assertEquals(ChatColor.LIGHT_PURPLE + "Now hidden", admin.nextMessage());
    }

    @Test
    void emptyMessagesAreNotSent() throws IOException {
        writeMessages("you_vanished: ''\n");
        plugin.reload();
        TestPlayer admin = joinOperator("Admin");
        clearMessages(admin);

        admin.performCommand("vanish");

        assertFalse(receivedMessages(admin).stream().anyMatch(message -> message.contains("vanished from the game")));
    }

    @Test
    void missingMessagesFallBackToTheBundledText() throws IOException {
        writeMessages("unrelated: 'value'\n");
        plugin.reload();
        TestPlayer admin = joinOperator("Admin");
        clearMessages(admin);

        admin.performCommand("vanish");

        assertTrue(receivedMessages(admin).contains(ChatColor.DARK_AQUA + "You have vanished from the game. Poof!"));
    }

    @Test
    void bundledMessagesOnlyContainKnownKeys() throws IOException {
        List<String> known = new ArrayList<>();
        for (Message message : Message.values()) {
            known.add(message.getKey());
        }
        // Kept for existing configurations; the overview pages are rendered in code.
        known.addAll(List.of("permissions_header", "permissions_footer", "placeholders_header", "placeholders_footer"));

        try (InputStream in = Messages.class.getResourceAsStream("/messages.yml")) {
            assertNotNull(in);
            YamlConfiguration bundled = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String key : bundled.getKeys(false)) {
                assertTrue(known.contains(key), "Unknown message key in messages.yml: " + key);
            }
        }
    }

    private void writeMessages(String content) throws IOException {
        Files.writeString(new File(plugin.getDataFolder(), "messages.yml").toPath(), content);
    }

    private static List<String> receivedMessages(TestPlayer player) {
        List<String> messages = new ArrayList<>();
        String message;
        while ((message = player.nextMessage()) != null) {
            messages.add(message);
        }
        return messages;
    }
}
