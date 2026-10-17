/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands;

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permission;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishCommandTest extends VanishTestBase {

    @Test
    void vanishTogglesTheSender() {
        TestPlayer admin = joinOperator("Admin");

        admin.performCommand("vanish");
        assertTrue(vanishManager.isVanished(admin));

        admin.performCommand("vanish");
        assertFalse(vanishManager.isVanished(admin));
    }

    @Test
    void deniedSubcommandDoesNotToggleTheSender() {
        TestPlayer helper = joinWithPermissions("Helper", "vanish.cmd.vanish");

        helper.performCommand("vanish reload");

        assertFalse(vanishManager.isVanished(helper));
        assertTrue(helper.nextMessage().contains("do not have permissions"));
    }

    @Test
    void unknownPlayerNameDoesNotToggleTheSender() {
        TestPlayer admin = joinOperator("Admin");

        admin.performCommand("vanish Nobody");

        assertFalse(vanishManager.isVanished(admin));
        assertTrue(admin.nextMessage().contains("not online"));
    }

    @Test
    void togglingOthersRequiresTheOthersPermission() {
        TestPlayer helper = joinWithPermissions("Helper", "vanish.cmd.vanish");
        TestPlayer target = join("Target");

        helper.performCommand("vanish Target");

        assertFalse(vanishManager.isVanished(target));
        assertFalse(vanishManager.isVanished(helper));
    }

    @Test
    void consoleCanToggleOthers() {
        TestPlayer target = join("Target");
        ConsoleCommandSenderMock console = console();

        server.dispatchCommand(console, "vanish Target");

        assertTrue(vanishManager.isVanished(target));
        assertTrue(console.nextMessage().contains("successfully vanished Target"));
    }

    @Test
    void colorChangeIsSavedAndConfirmed() {
        ConsoleCommandSenderMock console = console();

        server.dispatchCommand(console, "vanish color red");

        assertEquals("RED", plugin.getConfig().getString("plugin-color"));
        String confirmation = console.nextMessage();
        assertNotNull(confirmation, "the sender gets no feedback");
        assertTrue(confirmation.contains("changed to RED"));
    }

    @Test
    void invalidColorIsRejected() {
        ConsoleCommandSenderMock console = console();

        server.dispatchCommand(console, "vanish color sparkly");

        assertEquals("BLUE", plugin.getConfig().getString("plugin-color"));
        assertTrue(console.nextMessage().contains("'sparkly' is not a valid color"));
    }

    @Test
    void soundCanBeTurnedOffAndOn() {
        ConsoleCommandSenderMock console = console();

        server.dispatchCommand(console, "vanish sound off");
        assertFalse(plugin.getConfig().getBoolean("sound-enable"));

        server.dispatchCommand(console, "vanish sound on");
        assertTrue(plugin.getConfig().getBoolean("sound-enable"));
    }

    @Test
    void silentPreferenceIsSavedPerPlayer() throws Exception {
        TestPlayer admin = joinOperator("Admin");
        clearMessages(admin);

        admin.performCommand("vanish silent off");
        assertTrue(admin.nextMessage().contains("disabled the silent mode"));

        server.getPluginManager().disablePlugin(plugin);
        String players = Files.readString(new File(plugin.getDataFolder(), "players.yml").toPath());
        assertTrue(players.contains(admin.getUniqueId() + ":") && players.contains("silent: false"), players);
    }

    @Test
    void listShowsVanishedPlayers() {
        TestPlayer admin = joinOperator("Admin");
        join("Viewer");
        vanish(admin);
        ConsoleCommandSenderMock console = console();

        server.dispatchCommand(console, "vanish list");

        assertTrue(console.nextMessage().contains("There are 1 of 2 players in vanish"));
        assertTrue(console.nextMessage().contains("Admin"));
    }

    @Test
    void tabCompletionOnlyListsPermittedSubcommands() {
        TestPlayer helper = joinWithPermissions("Helper", "vanish.cmd.vanish", "vanish.cmd.vanish.list");

        List<String> completions = vanishCommand().tabComplete(helper, "vanish", new String[]{""});

        // info, help, permissions and placeholders are available to everyone.
        assertEquals(Set.of("list", "info", "help", "permissions", "placeholders"), Set.copyOf(completions));
    }

    @Test
    void wildcardPermissionGrantsEverySubcommand() {
        TestPlayer admin = joinOperator("Admin");
        List<String> subcommands = vanishCommand().tabComplete(admin, "vanish", new String[]{""});
        Map<String, Boolean> children = plugin.getDescription().getPermissions().stream()
                .filter(permission -> permission.getName().equals("vanish.*"))
                .findFirst()
                .map(Permission::getChildren)
                .orElseThrow();

        assertTrue(subcommands.size() >= 13, subcommands.toString());
        for (String subcommand : subcommands) {
            assertEquals(Boolean.TRUE, children.get("vanish.cmd.vanish." + subcommand), subcommand);
        }
        for (String node : List.of("vanish.cmd.vanish", "vanish.cmd.vanish.others", "vanish.see", "vanish.join")) {
            assertEquals(Boolean.TRUE, children.get(node), node);
        }
    }

    private Command vanishCommand() {
        return server.getCommandMap().getCommand("vanish");
    }

    private ConsoleCommandSenderMock console() {
        return (ConsoleCommandSenderMock) server.getConsoleSender();
    }
}
