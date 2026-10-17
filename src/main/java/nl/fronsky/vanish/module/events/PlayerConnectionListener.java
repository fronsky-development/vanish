/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.VanishPermissions;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.storage.PlayerDataStore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Applies vanish on join (auto-vanish, silent join) and cleans up when players leave.
 */
public final class PlayerConnectionListener implements Listener {
    private final VanishManager vanishManager;
    private final PlayerDataStore playerData;

    public PlayerConnectionListener(VanishManager vanishManager, PlayerDataStore playerData) {
        this.vanishManager = vanishManager;
        this.playerData = playerData;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VanishPlayer vanishPlayer = vanishManager.track(player);
        vanishManager.hideVanishedPlayersFrom(player);

        if (!player.hasPermission(VanishPermissions.AUTO_VANISH)) {
            vanishManager.revealToOthers(player);
            return;
        }
        if (playerData.isSilentJoin(player.getUniqueId())) {
            event.setJoinMessage(null);
        }
        vanishManager.vanish(vanishPlayer, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            event.setQuitMessage(null);
            vanishManager.unvanish(vanishManager.getPlayer(player), true);
        }
        vanishManager.untrack(player);
    }
}
