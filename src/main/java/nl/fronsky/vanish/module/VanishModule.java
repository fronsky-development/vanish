/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module;

import lombok.Getter;
import lombok.Setter;
import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.integrations.PlaceholderAPIExpansion;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.module.Module;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.commands.VanishCommand;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.events.DisabledActions;
import nl.fronsky.vanish.module.events.OnJoin;
import nl.fronsky.vanish.module.events.OnQuit;
import nl.fronsky.vanish.module.events.OnVisibilityChange;
import nl.fronsky.vanish.module.events.gui.Color;
import nl.fronsky.vanish.module.events.gui.Player;
import nl.fronsky.vanish.module.events.gui.Sound;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.Dynmap;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class VanishModule extends Module {
    @Getter
    @Setter
    private static Data data;

    @Override
    public void onLoad() {
        setData(new Data());
    }

    @Override
    public void onEnable() {
        if (data.getProtocolLib() == null) {
            Logger.warning(Language.PROTOCOLLIB_NOT_FOUND.getMessage());
        } else {
            Logger.info("ProtocolLib integration enabled.");
        }

        // PlaceholderAPI integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                org.bukkit.plugin.Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                if (papi != null) {
                    Logger.info("Found PlaceholderAPI v" + papi.getDescription().getVersion());
                }

                boolean registered = new PlaceholderAPIExpansion(Main.getInstance()).register();
                if (registered) {
                    Logger.info("PlaceholderAPI integration enabled.");
                } else {
                    Logger.warning("PlaceholderAPI detected, but expansion registration returned false (placeholders may not be available). Try /papi reload.");
                }
            } catch (Exception e) {
                Logger.exception("Failed to register PlaceholderAPI expansion", e);
            }
        } else {
            Logger.warning("PlaceholderAPI not found. Placeholders will not be available.");
        }

        event(Color::new);
        event(Player::new);
        event(Sound::new);
        DisabledActions disabledActions = new DisabledActions();
        event(() -> disabledActions);
        data.setDisabledActions(disabledActions);
        event(OnJoin::new);
        event(OnQuit::new);
        event(OnVisibilityChange::new);
        command(VanishCommand::new);
        for (VanishPlayer vanishPlayer : MetaData.getOnlinePlayers()) {
            if (vanishPlayer.hasPermission("vanish.join")) {
                vanishPlayer.hide(false);
            }
        }
    }

    @Override
    public void onDisable() {
        data.getVanishedBossBar().setVisible(false);
        data.getVanishedBossBar().removeAll();

        // BUG-2 fix: take a snapshot to avoid ConcurrentModificationException
        List<VanishPlayer> vanishedSnapshot = new ArrayList<>(data.getVanishedPlayers().values());

        for (VanishPlayer player : MetaData.getOnlinePlayers()) {
            for (VanishPlayer vanishPlayer : vanishedSnapshot) {
                player.getPlayer().showPlayer(data.getPlugin(), vanishPlayer.getPlayer());
            }
        }
        for (VanishPlayer vanishPlayer : vanishedSnapshot) {
            vanishPlayer.setState(State.VISIBLE);
            data.getVanishedBossBar().removePlayer(vanishPlayer.getPlayer());
            vanishPlayer.getPlayer().setCollidable(true);
            vanishPlayer.getPlayer().setCanPickupItems(true);
            vanishPlayer.getPlayer().removeMetadata("fronsky_vanish", data.getPlugin());
            Dynmap.show(vanishPlayer.getPlayer());
        }

        data.getVanishedPlayers().clear();

        // Cleanup resources
        data.cleanup();
        Logger.info("Vanish module disabled and cleaned up successfully.");
    }
}
