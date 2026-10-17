/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SilentContainerSessionsTest {
    private final SilentContainerSessions sessions = new SilentContainerSessions();
    private final UUID opener = UUID.randomUUID();
    private final UUID world = UUID.randomUUID();

    @Test
    void containerStaysSilentForAsLongAsItIsOpen() {
        sessions.begin(opener, world, 0, 64, 0, 0);

        long oneMinuteLater = 60_000;
        assertTrue(sessions.isOpener(opener));
        assertTrue(sessions.isNearContainer(world, 10, 64, 10, oneMinuteLater),
                "the closing animation was audible once the chest was open longer than 2.5 seconds");
    }

    @Test
    void nearbyPlayersStayMutedBrieflyAfterClosing() {
        sessions.begin(opener, world, 0, 64, 0, 0);

        sessions.end(opener, 5_000);

        assertFalse(sessions.isOpener(opener));
        assertTrue(sessions.isNearContainer(world, 0, 64, 0, 5_000 + SilentContainerSessions.CLOSE_GRACE_MS));
        assertFalse(sessions.isNearContainer(world, 0, 64, 0, 5_001 + SilentContainerSessions.CLOSE_GRACE_MS));
    }

    @Test
    void onlyPlayersNearbyInTheSameWorldAreMuted() {
        sessions.begin(opener, world, 0, 64, 0, 0);

        assertTrue(sessions.isNearContainer(world, 63, 64, 0, 1));
        assertFalse(sessions.isNearContainer(world, 65, 64, 0, 1));
        assertFalse(sessions.isNearContainer(UUID.randomUUID(), 0, 64, 0, 1));
    }

    @Test
    void sessionsThatAreNeverEndedExpire() {
        sessions.begin(opener, world, 0, 64, 0, 0);

        assertFalse(sessions.isNearContainer(world, 0, 64, 0, SilentContainerSessions.MAX_OPEN_MS + 1));
    }

    @Test
    void endingAnUnknownSessionDoesNothing() {
        sessions.end(opener, 0);

        assertFalse(sessions.isOpener(opener));
        assertFalse(sessions.isNearContainer(world, 0, 64, 0, 0));
    }
}
