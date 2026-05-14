/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.module.ModuleManager;
import nl.fronsky.vanish.logic.utils.MinecraftVersion;
import nl.fronsky.vanish.module.VanishModule;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private final ModuleManager moduleManager = new ModuleManager();

    /**
     * Retrieves the instance of the Main class.
     *
     * @return the instance of the Main class
     */
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        Logger.info("Loading Vanish plugin v" + getDescription().getVersion() + "...");

        moduleManager.prepare(new VanishModule());
        try {
            moduleManager.load();
            Logger.info("Vanish modules loaded successfully!");
        } catch (Exception exception) {
            Logger.severe("Failed to load modules!", exception);
            Logger.exception("Module loading failed", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
        try {
            Logger.info("Enabling Vanish plugin...");

            // Check Minecraft version compatibility
            String version = MinecraftVersion.getCurrentVersion();
            Logger.info("Running on Minecraft version: " + version + " (" + getServer().getBukkitVersion() + ")");

            if (!MinecraftVersion.is1_13OrNewer()) {
                Logger.warning("This plugin is designed for Minecraft 1.13+. Your version may not be fully supported.");
            }

            if (MinecraftVersion.is1_21OrNewer()) {
                Logger.info("Detected Minecraft 1.21+ - Full compatibility enabled!");
            }

            moduleManager.enable();
            Logger.info("Vanish plugin v" + getDescription().getVersion() + " enabled successfully!");
        } catch (Exception exception) {
            Logger.severe("Failed to enable modules!", exception);
            Logger.exception("Module enabling failed", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            Logger.info("Disabling Vanish plugin...");
            moduleManager.disable();
            Logger.info("Vanish plugin disabled successfully!");
        } catch (Exception exception) {
            Logger.severe("Failed to disable modules!", exception);
            Logger.exception("Module disabling failed", exception);
        } finally {
            instance = null;
        }
    }
}
