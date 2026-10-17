/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Iterator;

/**
 * Removes vanished players from the server list ping, so they are not counted or shown when hovering over the
 * player count. The event may be called off the main thread.
 */
public final class ServerListPingListener implements Listener {
    private final VanishManager vanishManager;

    public ServerListPingListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        if (vanishManager.getVanishedCount() == 0) {
            return;
        }
        try {
            Iterator<Player> players = event.iterator();
            while (players.hasNext()) {
                if (vanishManager.isVanished(players.next())) {
                    players.remove();
                }
            }
        } catch (UnsupportedOperationException ignored) {
            // This server implementation cannot hide players from the ping.
        }
    }
}
