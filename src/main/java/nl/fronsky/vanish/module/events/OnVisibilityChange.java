/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.events.custom.VisibilityChangeEvent;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import nl.fronsky.vanish.module.utils.ProtocolLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnVisibilityChange implements Listener {
    private final Data data;
    private final ProtocolLib protocolLib;

    public OnVisibilityChange() {
        data = VanishModule.getData();
        protocolLib = data.getProtocolLib();
    }

    @EventHandler
    public void visibilityChange(VisibilityChangeEvent event) {
        boolean quit;
        if (event.isVisible()) {
            show(event.getVanishPlayer(), event.isAction());
            quit = false;
        } else {
            hide(event.getVanishPlayer(), event.isAction());
            quit = event.isAction();
        }
        if (event.isProtocolLibActive()) {
            event.getProtocolLib().updateOnlinePlayers(data, event.getVanishPlayer(), quit);
        }
    }

    /**
     * Hides a specified player and notifies other players based on their permissions.
     *
     * @param player the {@code VanishPlayer} to hide
     * @param action {@code true} if the player joined silently, otherwise {@code false}
     */
    private void hide(VanishPlayer player, boolean action) {
        Language messageKey = action ? Language.PLAYER_JOINED_VANISHED_SILENTLY : Language.PLAYER_VANISHED;
        Language selfMessageKey = action ? Language.JOINED_VANISHED : Language.YOU_VANISHED;
        String message = messageKey.getMessageWithColor().replace("{player}", player.getDisplayName());
        MetaData.getOnlinePlayers().stream().filter(vanishPlayer -> !vanishPlayer.getUuid().equals(player.getUuid())).forEach(vanishPlayer -> {
            if (!vanishPlayer.hasPermission("vanish.see")) {
                vanishPlayer.getPlayer().hidePlayer(data.getPlugin(), player.getPlayer());
            } else {
                vanishPlayer.getPlayer().showPlayer(data.getPlugin(), player.getPlayer());
                vanishPlayer.sendMessage(message);
            }
        });
        if (protocolLib != null) {
            protocolLib.updateOnlinePlayers(data, player, false);
        }
        player.sendMessage(selfMessageKey.getMessageWithColor());
    }

    /**
     * Shows a specified player and notifies other players based on their permissions.
     *
     * @param player the {@code VanishPlayer} to show
     * @param action {@code true} if the player quit while vanished, otherwise {@code false}
     */
    private void show(VanishPlayer player, boolean action) {
        Language messageKey = action ? Language.PLAYER_QUIT_VANISHED : Language.PLAYER_BECAME_VISIBLE;
        String message = messageKey.getMessageWithColor().replace("{player}", player.getDisplayName());
        MetaData.getOnlinePlayers().stream().filter(vanishPlayer -> !vanishPlayer.getUuid().equals(player.getUuid())).forEach(vanishPlayer -> {
            if (!action) {
                vanishPlayer.getPlayer().showPlayer(data.getPlugin(), player.getPlayer());
            }
            if (vanishPlayer.hasPermission("vanish.see")) {
                vanishPlayer.sendMessage(message);
            }
        });
        if (protocolLib != null) {
            protocolLib.updateOnlinePlayers(data, player, action);
        }
        player.sendMessage(Language.YOU_BECAME_VISIBLE.getMessageWithColor());
    }
}
