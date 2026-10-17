/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.integrations.ProtocolLibHook;
import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.events.custom.VisibilityChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps advancement announcements from revealing vanished players.
 * <p>
 * Uses Paper's per-event message API and/or ProtocolLib when available. On plain Spigot without ProtocolLib the
 * gamerule that announces advancements is turned off while anyone is vanished and restored afterwards.
 */
public final class AdvancementSuppressor implements Listener {
    private final VanishConfig config;
    private final VanishManager vanishManager;
    private final ProtocolLibHook protocolLib;
    private final Method paperMessageSetter;
    private final GameRule<Boolean> announceRule;
    private final Map<UUID, Boolean> savedGamerules = new HashMap<>();

    /**
     * @param protocolLib the ProtocolLib hook, or {@code null} if ProtocolLib is not available
     */
    public AdvancementSuppressor(VanishConfig config, VanishManager vanishManager, ProtocolLibHook protocolLib) {
        this.config = config;
        this.vanishManager = vanishManager;
        this.protocolLib = protocolLib;
        this.paperMessageSetter = findPaperMessageSetter();
        this.announceRule = findAnnounceAdvancementsRule();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (!config.isAdvancementsDisabled() || !vanishManager.isVanished(player)) {
            return;
        }
        if (paperMessageSetter != null) {
            try {
                paperMessageSetter.invoke(event, (Object) null);
            } catch (ReflectiveOperationException ignored) {
                // The ProtocolLib or gamerule fallback still applies.
            }
        }
        if (protocolLib != null) {
            protocolLib.suppressAdvancementAnnouncement(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVisibilityChange(VisibilityChangeEvent event) {
        update();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        update();
    }

    /**
     * Applies or reverts the gamerule fallback for the current vanish state and configuration.
     */
    public void update() {
        if (paperMessageSetter != null || protocolLib != null || announceRule == null) {
            return;
        }
        if (!config.isAdvancementsDisabled() || vanishManager.getVanishedCount() == 0) {
            restore();
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            boolean announces = Boolean.TRUE.equals(world.getGameRuleValue(announceRule));
            savedGamerules.putIfAbsent(world.getUID(), announces);
            if (announces) {
                world.setGameRule(announceRule, false);
            }
        }
    }

    /**
     * Restores the gamerule values changed by {@link #update()}.
     */
    public void restore() {
        savedGamerules.forEach((worldId, announce) -> {
            World world = Bukkit.getWorld(worldId);
            if (world != null) {
                world.setGameRule(announceRule, announce);
            }
        });
        savedGamerules.clear();
    }

    /**
     * Looks the gamerule up by field name: Minecraft 1.21.11 renamed {@code announceAdvancements} to
     * {@code show_advancement_messages} and Spigot 26 only has the new constant.
     */
    @SuppressWarnings("unchecked")
    static GameRule<Boolean> findAnnounceAdvancementsRule() {
        for (String field : new String[]{"ANNOUNCE_ADVANCEMENTS", "SHOW_ADVANCEMENT_MESSAGES"}) {
            try {
                return (GameRule<Boolean>) GameRule.class.getField(field).get(null);
            } catch (ReflectiveOperationException ignored) {
                // Try the other name.
            }
        }
        return null;
    }

    private static Method findPaperMessageSetter() {
        // Paper adds PlayerAdvancementDoneEvent#message(Component); Spigot has no equivalent.
        for (Method method : PlayerAdvancementDoneEvent.class.getMethods()) {
            if (method.getName().equals("message") && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }
}
