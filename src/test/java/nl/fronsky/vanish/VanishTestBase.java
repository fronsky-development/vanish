/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import nl.fronsky.vanish.module.VanishManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs every test against a fresh mock server with Vanish loaded.
 */
public abstract class VanishTestBase {
    protected ServerMock server;
    protected VanishPlugin plugin;
    protected VanishManager vanishManager;

    @BeforeEach
    protected void loadPlugin() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(VanishPlugin.class);
        vanishManager = plugin.getVanishManager();
    }

    @AfterEach
    protected void unloadPlugin() {
        MockBukkit.unmock();
    }

    protected TestPlayer join(String name) {
        TestPlayer player = new TestPlayer(server, name);
        server.addPlayer(player);
        return player;
    }

    /**
     * Joins a player and makes them an operator afterwards, so this join does not auto-vanish them.
     */
    protected TestPlayer joinOperator(String name) {
        TestPlayer player = join(name);
        player.setOp(true);
        return player;
    }

    /**
     * Joins a player that has exactly the given permissions (besides the defaults for non-operators).
     */
    protected TestPlayer joinWithPermissions(String name, String... permissions) {
        TestPlayer player = new TestPlayer(server, name);
        for (String permission : permissions) {
            player.addAttachment(plugin, permission, true);
        }
        server.addPlayer(player);
        return player;
    }

    protected void vanish(PlayerMock player) {
        vanishManager.vanish(vanishManager.getPlayer(player), false);
    }

    /**
     * Registers a listener of another plugin, which runs after Vanish within the same event priority.
     */
    protected void registerOtherPluginListener(Listener listener) {
        Plugin other = MockBukkit.createMockPlugin();
        server.getPluginManager().registerEvents(listener, other);
    }

    protected static void rejoin(PlayerMock player) {
        player.disconnect();
        player.reconnect();
    }

    protected static void clearMessages(PlayerMock player) {
        while (player.nextMessage() != null) {
            // discard
        }
    }

    /**
     * MockBukkit does not implement toggling item pickup and treats infinite potion effects as expired; this player
     * keeps track of both.
     */
    protected static final class TestPlayer extends PlayerMock {
        private final Map<PotionEffectType, PotionEffect> potionEffects = new HashMap<>();
        private boolean canPickupItems = true;

        TestPlayer(ServerMock server, String name) {
            super(server, name);
        }

        @Override
        public void setCanPickupItems(boolean pickup) {
            canPickupItems = pickup;
        }

        @Override
        public boolean getCanPickupItems() {
            return canPickupItems;
        }

        @Override
        public boolean addPotionEffect(PotionEffect effect) {
            potionEffects.put(effect.getType(), effect);
            return true;
        }

        @Override
        public void removePotionEffect(PotionEffectType type) {
            potionEffects.remove(type);
        }

        @Override
        public boolean hasPotionEffect(PotionEffectType type) {
            return potionEffects.containsKey(type);
        }
    }
}
