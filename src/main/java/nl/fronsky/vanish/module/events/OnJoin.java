/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener {
    private final Data data;

    public OnJoin() {
        data = VanishModule.getData();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        VanishPlayer player = new VanishPlayer(event.getPlayer());
        if (!player.hasPermission("vanish.join")) {
            if (!player.hasPermission("vanish.see")) {
                for (VanishPlayer vanishPlayer : data.getVanishedPlayers().values()) {
                    player.getPlayer().hidePlayer(data.getPlugin(), vanishPlayer.getPlayer());
                }
            }
            return;
        }
        if (player.hasPermission("vanish.join")) {
            if (data.getPlayers().get().getBoolean(player.getUuid() + ".silent")) {
                event.setJoinMessage("");
                player.hide(true);
            }
        }
    }
}
