/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"deprecation", "removal"})
class ServerListPingListenerTest extends VanishTestBase {

    @Test
    void vanishedPlayersAreRemovedFromTheServerList() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");
        vanish(admin);
        PingWithPlayers ping = new PingWithPlayers(List.of(admin, viewer));

        new ServerListPingListener(vanishManager).onServerListPing(ping);

        assertEquals(List.of(viewer), ping.players);
    }

    @Test
    void pingsThatCannotHidePlayersAreLeftAlone() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        ServerListPingEvent ping = new ServerListPingEvent("localhost", InetAddress.getLoopbackAddress(), "motd",
                1, 20);

        assertDoesNotThrow(() -> new ServerListPingListener(vanishManager).onServerListPing(ping));
    }

    @Test
    void vanishedPlayersAreRemovedFromThePaperHoverList() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");
        vanish(admin);
        PaperLikePing ping = new PaperLikePing(List.of(admin, viewer));

        new ServerListPingListener(vanishManager).onServerListPing(ping);

        assertEquals(List.of(new ListedPlayer("Viewer", viewer.getUniqueId())), ping.getListedPlayers(),
                "Paper 1.20.6 showed vanished players when hovering over the player count");
    }

    /**
     * A ping that supports removing players, like the one fired by the server.
     */
    private static class PingWithPlayers extends ServerListPingEvent {
        private final List<Player> players;

        PingWithPlayers(List<Player> players) {
            super("localhost", InetAddress.getLoopbackAddress(), "motd", 20);
            this.players = new ArrayList<>(players);
        }

        @Override
        public Iterator<Player> iterator() {
            return players.iterator();
        }
    }

    /**
     * Like Paper 1.20.6: removing a player through the iterator does not update the separate hover list.
     */
    private static final class PaperLikePing extends PingWithPlayers {
        private final List<ListedPlayer> listedPlayers = new ArrayList<>();

        PaperLikePing(List<Player> players) {
            super(players);
            players.forEach(player -> listedPlayers.add(new ListedPlayer(player.getName(), player.getUniqueId())));
        }

        public List<ListedPlayer> getListedPlayers() {
            return listedPlayers;
        }
    }

    record ListedPlayer(String name, UUID id) {
    }
}
