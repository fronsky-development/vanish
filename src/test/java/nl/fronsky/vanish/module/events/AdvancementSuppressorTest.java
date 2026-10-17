/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import org.bukkit.GameRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvancementSuppressorTest {

    @Test
    void findsTheAdvancementAnnouncementGamerule() {
        assertEquals(GameRule.ANNOUNCE_ADVANCEMENTS, AdvancementSuppressor.findAnnounceAdvancementsRule());
    }
}
