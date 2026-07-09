/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Iterator;

/**
 * Removes vanished players from the server list ping player sample so they are not
 * shown when hovering over the player count in the multiplayer server list.
 */
public class OnServerListPing implements Listener {
    private final Data data;

    public OnServerListPing() {
        data = VanishModule.getData();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        if (data == null || data.getVanishedPlayers().isEmpty()) {
            return;
        }

        try {
            Iterator<Player> iterator = event.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (player != null && data.getVanishedPlayers().containsKey(player.getUniqueId())) {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            Logger.debug("Could not filter vanished players from server list ping sample: " + e.getMessage());
        }
    }
}
