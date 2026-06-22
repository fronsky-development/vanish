/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
        sendNotifications(player, true);
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
        sendNotifications(player, false);
    }

    /**
     * Sends actionbar and/or title notifications to the player based on config.
     *
     * @param player  the player to notify
     * @param vanished true if the player just vanished, false if they became visible
     */
    private void sendNotifications(VanishPlayer player, boolean vanished) {
        if (data.getConfig().get().getBoolean("notifications.actionbar", false)) {
            String msg = vanished
                    ? Language.NOTIFICATION_VANISHED_ACTIONBAR.getMessageWithColor()
                    : Language.NOTIFICATION_VISIBLE_ACTIONBAR.getMessageWithColor();
            player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
        }
        if (data.getConfig().get().getBoolean("notifications.title", false)) {
            String title = vanished
                    ? Language.NOTIFICATION_VANISHED_TITLE.getMessageWithColor()
                    : Language.NOTIFICATION_VISIBLE_TITLE.getMessageWithColor();
            player.getPlayer().sendTitle(title, "", 10, 40, 10);
        }
    }
}
