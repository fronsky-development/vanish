/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish;

import nl.fronsky.vanish.integrations.DynmapHook;
import nl.fronsky.vanish.integrations.PlaceholderAPIExpansion;
import nl.fronsky.vanish.integrations.ProtocolLibHook;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.commands.VanishCommand;
import nl.fronsky.vanish.module.commands.subcommands.ColorSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.FakeMessageSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.GuiSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.InfoSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.ListSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.OverviewSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.ReloadSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.SilentSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.SoundSubcommand;
import nl.fronsky.vanish.module.commands.subcommands.TpSubcommand;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.events.AdvancementSuppressor;
import nl.fronsky.vanish.module.events.DisabledActionsListener;
import nl.fronsky.vanish.module.events.PlayerConnectionListener;
import nl.fronsky.vanish.module.events.ServerListPingListener;
import nl.fronsky.vanish.module.events.SilentContainerListener;
import nl.fronsky.vanish.module.gui.MenuListener;
import nl.fronsky.vanish.module.storage.PlayerDataStore;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public class VanishPlugin extends JavaPlugin {
    // Fixed bStats service ID of Fronsky Vanish; intentionally not configurable.
    private static final int BSTATS_SERVICE_ID = 34774;

    private VanishConfig vanishConfig;
    private Messages messages;
    private PlayerDataStore playerData;
    private VanishManager vanishManager;
    private ProtocolLibHook protocolLibHook;
    private AdvancementSuppressor advancementSuppressor;
    private SilentContainerListener silentContainerListener;
    private PlaceholderAPIExpansion placeholderExpansion;
    private Metrics metrics;

    @Override
    public void onEnable() {
        try {
            enable();
        } catch (RuntimeException e) {
            getLogger().log(Level.SEVERE, "Failed to enable Vanish; the plugin will be disabled.", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (metrics != null) {
                metrics.shutdown();
            }
            if (placeholderExpansion != null) {
                placeholderExpansion.unregister();
            }
            if (silentContainerListener != null) {
                silentContainerListener.restoreAll();
            }
            if (vanishManager != null) {
                vanishManager.shutdown();
            }
            if (advancementSuppressor != null) {
                advancementSuppressor.restore();
            }
            if (protocolLibHook != null) {
                protocolLibHook.unregister();
            }
        } finally {
            if (playerData != null) {
                playerData.shutdown();
            }
        }
    }

    /**
     * Re-reads the configuration, messages and player data and applies them immediately.
     */
    public void reload() {
        vanishConfig.reload();
        messages.reload();
        playerData.reload();
        vanishManager.refreshBossBar();
        advancementSuppressor.update();
    }

    /**
     * Logs a message when {@code debug-mode} is enabled.
     */
    public void debug(String message) {
        if (vanishConfig != null && vanishConfig.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * @return the vanish manager, or {@code null} while the plugin is not enabled
     */
    public VanishManager getVanishManager() {
        return vanishManager;
    }

    private void enable() {
        vanishConfig = new VanishConfig(this);
        messages = new Messages(this);
        playerData = new PlayerDataStore(new File(getDataFolder(), "players.yml"), getLogger());
        vanishManager = new VanishManager(this, vanishConfig, messages, hookDynmap());
        protocolLibHook = hookProtocolLib();
        advancementSuppressor = new AdvancementSuppressor(vanishConfig, vanishManager, protocolLibHook);
        silentContainerListener = new SilentContainerListener(this, vanishConfig, vanishManager, protocolLibHook);
        MenuListener menus = new MenuListener(this);

        registerListeners(
                new PlayerConnectionListener(vanishManager, playerData),
                new DisabledActionsListener(vanishConfig, vanishManager),
                new ServerListPingListener(vanishManager),
                advancementSuppressor,
                silentContainerListener,
                menus);
        registerCommand(menus);
        hookPlaceholderAPI();

        vanishManager.trackOnlinePlayers();
        metrics = new Metrics(this, BSTATS_SERVICE_ID);
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
    }

    private void registerCommand(MenuListener menus) {
        VanishCommand command = new VanishCommand(vanishConfig, messages, vanishManager);
        command.register(new GuiSubcommand(vanishConfig, messages, vanishManager, menus, command::toggleOther));
        command.register(new SoundSubcommand(vanishConfig, messages, menus));
        command.register(new SilentSubcommand(messages, playerData));
        command.register(new ListSubcommand(vanishManager));
        command.register(FakeMessageSubcommand.join(messages));
        command.register(FakeMessageSubcommand.quit(messages));
        command.register(new TpSubcommand(messages));
        command.register(new ColorSubcommand(vanishConfig, messages, vanishManager, menus));
        command.register(new InfoSubcommand(this, vanishConfig));
        command.register(OverviewSubcommand.help(vanishConfig));
        command.register(new ReloadSubcommand(this, messages));
        command.register(OverviewSubcommand.permissions(vanishConfig));
        command.register(OverviewSubcommand.placeholders(vanishConfig));

        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("vanish"), "plugin.yml must declare /vanish");
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    private DynmapHook hookDynmap() {
        if (!getServer().getPluginManager().isPluginEnabled(DynmapHook.PLUGIN_NAME)) {
            return null;
        }
        getLogger().info("Dynmap integration enabled.");
        return new DynmapHook(getLogger());
    }

    private ProtocolLibHook hookProtocolLib() {
        if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().warning("ProtocolLib not found. Some features will not function without it.");
            return null;
        }
        try {
            ProtocolLibHook hook = new ProtocolLibHook(this, vanishManager);
            hook.register();
            getLogger().info("ProtocolLib integration enabled.");
            return hook;
        } catch (RuntimeException | LinkageError e) {
            getLogger().log(Level.WARNING, "This ProtocolLib version is not supported; continuing without it.", e);
            return null;
        }
    }

    private void hookPlaceholderAPI() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not be available.");
            return;
        }
        try {
            PlaceholderAPIExpansion expansion =
                    new PlaceholderAPIExpansion(this, vanishConfig, vanishManager, playerData);
            if (expansion.register()) {
                placeholderExpansion = expansion;
                getLogger().info("PlaceholderAPI integration enabled.");
            } else {
                getLogger().warning("PlaceholderAPI did not accept the Vanish expansion; "
                        + "placeholders are unavailable.");
            }
        } catch (RuntimeException | LinkageError e) {
            getLogger().log(Level.WARNING, "This PlaceholderAPI version is not supported; continuing without it.", e);
        }
    }
}
