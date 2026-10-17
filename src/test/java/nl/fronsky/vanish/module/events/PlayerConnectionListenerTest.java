/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.VanishTestBase;
import nl.fronsky.vanish.module.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class PlayerConnectionListenerTest extends VanishTestBase {
    private final List<String> joinMessages = new ArrayList<>();
    private final List<String> quitMessages = new ArrayList<>();

    @BeforeEach
    void recordMessages() {
        registerOtherPluginListener(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoin(PlayerJoinEvent event) {
                joinMessages.add(event.getJoinMessage());
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(PlayerQuitEvent event) {
                quitMessages.add(event.getQuitMessage());
            }
        });
    }

    @Test
    void playersWithAutoVanishJoinVanishedAndSilently() {
        TestPlayer admin = joinOperator("Admin");
        assertFalse(vanishManager.isVanished(admin));

        rejoin(admin);

        assertTrue(vanishManager.isVanished(admin));
        assertNull(lastJoinMessage());
    }

    @Test
    void silentJoinCanBeTurnedOff() {
        TestPlayer admin = joinOperator("Admin");
        admin.performCommand("vanish silent off");

        rejoin(admin);

        assertTrue(vanishManager.isVanished(admin));
        assertNotNull(lastJoinMessage());
    }

    @Test
    void joinMessageSetByAnotherPluginDoesNotRevealVanishedPlayers() {
        registerOtherPluginListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onJoin(PlayerJoinEvent event) {
                event.setJoinMessage("Welcome back, " + event.getPlayer().getName() + "!");
            }
        });
        TestPlayer admin = joinOperator("Admin");

        rejoin(admin);

        assertNull(lastJoinMessage());
    }

    @Test
    void quittingWhileVanishedIsSilentAndUnvanishes() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        admin.disconnect();

        assertNull(quitMessages.get(quitMessages.size() - 1));
        assertFalse(vanishManager.isVanished(admin));
    }

    @Test
    void otherPluginsStillSeeTheVanishedFlagWhileHandlingTheQuit() {
        AtomicBoolean flaggedDuringQuit = new AtomicBoolean();
        registerOtherPluginListener(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(PlayerQuitEvent event) {
                flaggedDuringQuit.set(event.getPlayer().hasMetadata(VanishManager.VANISHED_METADATA_KEY));
            }
        });
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        admin.disconnect();
        assertTrue(flaggedDuringQuit.get(), "e.g. Discord bridges check the flag to hide the quit");

        server.getScheduler().performOneTick();
        assertFalse(admin.hasMetadata(VanishManager.VANISHED_METADATA_KEY));
    }

    @Test
    void playerWhoLeftVanishedIsVisibleAfterRejoining() {
        TestPlayer target = join("Target");
        TestPlayer viewer = join("Viewer");
        vanish(target);
        assertFalse(viewer.canSee(target));

        rejoin(target);

        assertFalse(vanishManager.isVanished(target));
        assertTrue(viewer.canSee(target));
    }

    @Test
    void autoVanishWithoutSeePermissionDoesNotRevealOtherVanishedPlayers() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        TestPlayer helper = joinWithPermissions("Helper", "vanish.join");

        assertTrue(vanishManager.isVanished(helper));
        assertFalse(helper.canSee(admin));
        assertTrue(admin.canSee(helper));
    }

    private String lastJoinMessage() {
        return joinMessages.get(joinMessages.size() - 1);
    }
}
