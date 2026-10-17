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
import org.bukkit.profile.PlayerProfile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Removes vanished players from the server list ping, so they are not counted or shown when hovering over the
 * player count. The event may be called off the main thread.
 */
public final class ServerListPingListener implements Listener {
    private static final String[] SAMPLE_GETTERS = {"getListedPlayers", "getPlayerSample"};

    private final VanishManager vanishManager;
    private final Map<Class<?>, List<Method>> sampleGetters = new ConcurrentHashMap<>();

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
        removeVanishedFromSample(event);
    }

    /**
     * Paper keeps the names shown when hovering over the player count in a separate list, which some versions
     * (e.g. 1.20.6) do not update when players are removed through the iterator.
     */
    private void removeVanishedFromSample(ServerListPingEvent event) {
        List<Method> getters = sampleGetters.computeIfAbsent(event.getClass(),
                ServerListPingListener::findSampleGetters);
        for (Method getter : getters) {
            try {
                List<?> sample = (List<?>) getter.invoke(event);
                sample.removeIf(entry -> vanishManager.isVanished(idOf(entry)));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // The iterator above already hid the players where the server supports it.
            }
        }
    }

    private static List<Method> findSampleGetters(Class<?> eventClass) {
        List<Method> getters = new ArrayList<>();
        for (String name : SAMPLE_GETTERS) {
            try {
                getters.add(eventClass.getMethod(name));
            } catch (NoSuchMethodException ignored) {
                // Not available on this server version.
            }
        }
        return getters;
    }

    /**
     * @return the UUID of a sample entry: a {@link PlayerProfile} or a record with an {@code id()} accessor
     */
    private static UUID idOf(Object entry) {
        if (entry instanceof PlayerProfile profile) {
            return profile.getUniqueId();
        }
        try {
            return entry.getClass().getMethod("id").invoke(entry) instanceof UUID uuid ? uuid : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
