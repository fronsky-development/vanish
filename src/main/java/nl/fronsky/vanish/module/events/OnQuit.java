/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnQuit implements Listener {
    private final Data data;

    public OnQuit() {
        data = VanishModule.getData();
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        VanishPlayer vanishPlayer = new VanishPlayer(event.getPlayer());
        if (MetaData.getVanishState(vanishPlayer.getPlayer(), data).equals(State.HIDDEN)) {
            event.setQuitMessage("");
            vanishPlayer.show(true);
        }
    }
}
