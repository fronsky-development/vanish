/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module;

import nl.fronsky.vanish.VanishTestBase;
import nl.fronsky.vanish.module.events.custom.VisibilityChangeEvent;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishManagerTest extends VanishTestBase {

    @Test
    void vanishHidesThePlayerAndAppliesTheEffects() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");

        vanish(admin);

        assertTrue(vanishManager.isVanished(admin));
        assertFalse(viewer.canSee(admin));
        assertTrue(admin.hasMetadata(VanishManager.VANISHED_METADATA_KEY), "TAB and Essentials read this flag");
        assertFalse(admin.isCollidable());
        assertFalse(admin.getCanPickupItems());
        assertTrue(admin.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        assertTrue(admin.getAllowFlight());
    }

    @Test
    void playersWhoJoinLaterCannotSeeVanishedPlayers() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        TestPlayer late = join("Late");

        assertFalse(late.canSee(admin));
    }

    @Test
    void staffKeepSeeingVanishedPlayersAndAreNotified() {
        TestPlayer staff = joinOperator("Staff");
        TestPlayer admin = joinOperator("Admin");
        clearMessages(staff);

        vanish(admin);

        assertTrue(staff.canSee(admin));
        assertTrue(staff.nextMessage().contains("Admin has vanished"));
    }

    @Test
    void unvanishRevertsEverything() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");
        vanish(admin);

        vanishManager.unvanish(vanishManager.getPlayer(admin), false);

        assertFalse(vanishManager.isVanished(admin));
        assertTrue(viewer.canSee(admin));
        assertFalse(admin.hasMetadata(VanishManager.VANISHED_METADATA_KEY));
        assertTrue(admin.isCollidable());
        assertTrue(admin.getCanPickupItems());
        assertFalse(admin.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        assertFalse(admin.getAllowFlight());
    }

    @Test
    void unvanishKeepsFlightForPlayersWhoCouldAlreadyFly() {
        TestPlayer flyer = joinOperator("Flyer");
        flyer.setAllowFlight(true);
        TestPlayer builder = joinOperator("Builder");
        builder.setGameMode(GameMode.CREATIVE);

        for (TestPlayer player : new TestPlayer[]{flyer, builder}) {
            vanish(player);
            vanishManager.unvanish(vanishManager.getPlayer(player), false);
            assertTrue(player.getAllowFlight(), player.getName() + " lost flight");
        }
    }

    @Test
    void vanishAndUnvanishOnlyActOnce() {
        TestPlayer admin = joinOperator("Admin");
        AtomicInteger events = new AtomicInteger();
        registerOtherPluginListener(new Listener() {
            @EventHandler
            public void onVisibilityChange(VisibilityChangeEvent event) {
                events.incrementAndGet();
            }
        });

        vanish(admin);
        vanish(admin);
        vanishManager.unvanish(vanishManager.getPlayer(admin), false);
        vanishManager.unvanish(vanishManager.getPlayer(admin), false);

        assertEquals(2, events.get());
    }

    @Test
    void disablingThePluginRevealsEveryone() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer viewer = join("Viewer");
        vanish(admin);

        server.getPluginManager().disablePlugin(plugin);

        assertTrue(viewer.canSee(admin));
        assertFalse(admin.hasMetadata(VanishManager.VANISHED_METADATA_KEY));
        assertTrue(admin.isCollidable());
        assertTrue(admin.getCanPickupItems());
        assertFalse(admin.hasPotionEffect(PotionEffectType.NIGHT_VISION));
    }

    @Test
    void reloadingTheServerVanishesOnlinePlayersWithAutoVanish() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer user = join("User");

        server.getPluginManager().disablePlugin(plugin);
        server.getPluginManager().enablePlugin(plugin);

        assertTrue(plugin.getVanishManager().isVanished(admin));
        assertFalse(plugin.getVanishManager().isVanished(user));
        assertFalse(user.canSee(admin));
    }
}
