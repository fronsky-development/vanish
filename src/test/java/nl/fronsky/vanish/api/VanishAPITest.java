/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.api;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishAPITest {

    @Test
    void returnsSafeDefaultsWhenVanishIsNotLoaded() {
        assertFalse(VanishAPI.isAvailable());
        assertFalse(VanishAPI.isVanished(UUID.randomUUID()));
        assertFalse(VanishAPI.isVanished((org.bukkit.entity.Player) null));
        assertFalse(VanishAPI.vanishPlayer(null));
        assertFalse(VanishAPI.unvanishPlayer(null));
        assertFalse(VanishAPI.canSeeVanished(null));
        assertEquals(0, VanishAPI.getVanishedPlayerCount());
        assertTrue(VanishAPI.getVanishedPlayers().isEmpty());
        assertTrue(VanishAPI.getVanishedPlayerUUIDs().isEmpty());
    }
}
