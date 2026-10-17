/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nl.fronsky.vanish.VanishPlugin;
import nl.fronsky.vanish.integrations.DynmapHook;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.events.custom.VisibilityChangeEvent;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the vanish state of every player and applies or reverts everything vanishing involves.
 * <p>
 * State queries are lock-free and safe from any thread; changing state must happen on the main thread.
 */
public final class VanishManager {
    /**
     * Metadata flag that other plugins (such as TAB and Essentials) check to detect vanished players.
     */
    public static final String VANISHED_METADATA_KEY = "vanished";

    private final VanishPlugin plugin;
    private final VanishConfig config;
    private final Messages messages;
    private final DynmapHook dynmap;
    private final VanishBossBar bossBar;
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();
    private final Map<UUID, VanishPlayer> players = new ConcurrentHashMap<>();
    private final Map<UUID, AppliedEffects> appliedEffects = new HashMap<>();

    /**
     * @param dynmap the Dynmap hook, or {@code null} when Dynmap is not installed
     */
    public VanishManager(VanishPlugin plugin, VanishConfig config, Messages messages, DynmapHook dynmap) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.dynmap = dynmap;
        this.bossBar = new VanishBossBar(config);
    }

    public static boolean canSeeVanished(Player player) {
        return player.hasPermission(VanishPermissions.SEE);
    }

    public boolean isVanished(UUID uuid) {
        return uuid != null && vanished.contains(uuid);
    }

    public boolean isVanished(Player player) {
        return player != null && vanished.contains(player.getUniqueId());
    }

    public int getVanishedCount() {
        return vanished.size();
    }

    /**
     * @return a live, read-only view of the UUIDs of all vanished players
     */
    public Set<UUID> getVanishedIds() {
        return Collections.unmodifiableSet(vanished);
    }

    /**
     * @return a snapshot of the vanished players that are online
     */
    public List<VanishPlayer> getVanishedPlayers() {
        List<VanishPlayer> result = new ArrayList<>();
        for (Player player : getOnlineVanishedPlayers()) {
            result.add(getPlayer(player));
        }
        return result;
    }

    /**
     * @return the cached {@link VanishPlayer} of an online player, or a new one if the player is not tracked yet
     */
    public VanishPlayer getPlayer(Player player) {
        VanishPlayer cached = players.get(player.getUniqueId());
        return cached != null ? cached : new VanishPlayer(this, player);
    }

    /**
     * Starts caching the {@link VanishPlayer} of a player who joined.
     */
    public VanishPlayer track(Player player) {
        VanishPlayer vanishPlayer = new VanishPlayer(this, player);
        players.put(player.getUniqueId(), vanishPlayer);
        return vanishPlayer;
    }

    public void untrack(Player player) {
        players.remove(player.getUniqueId());
    }

    /**
     * Tracks players that are already online when the plugin enables (after a reload) and vanishes
     * those with the auto-vanish permission.
     */
    public void trackOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            VanishPlayer vanishPlayer = track(player);
            if (player.hasPermission(VanishPermissions.AUTO_VANISH)) {
                vanish(vanishPlayer, false);
            }
        }
    }

    /**
     * Vanishes a player. Does nothing if the player is already vanished.
     *
     * @param joined {@code true} if the player is being vanished because they joined
     */
    public void vanish(VanishPlayer vanishPlayer, boolean joined) {
        Player player = vanishPlayer.getPlayer();
        if (!vanished.add(player.getUniqueId())) {
            return;
        }

        applyEffects(player);
        bossBar.show(player);
        if (dynmap != null) {
            dynmap.hide(player);
        }

        Message notice = joined ? Message.PLAYER_JOINED_VANISHED_SILENTLY : Message.PLAYER_VANISHED;
        String noticeText = messages.format(notice, "{player}", player.getDisplayName());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (canSeeVanished(other)) {
                other.showPlayer(plugin, player);
                messages.send(other, noticeText);
            } else {
                other.hidePlayer(plugin, player);
            }
        }
        messages.send(player, joined ? Message.JOINED_VANISHED : Message.YOU_VANISHED);
        sendNotification(player, true);

        plugin.debug(player.getName() + " is now vanished");
        Bukkit.getPluginManager().callEvent(new VisibilityChangeEvent(vanishPlayer, joined));
    }

    /**
     * Makes a vanished player visible again. Does nothing if the player is not vanished.
     *
     * @param quitting {@code true} if the player is leaving the server
     */
    public void unvanish(VanishPlayer vanishPlayer, boolean quitting) {
        Player player = vanishPlayer.getPlayer();
        if (!vanished.remove(player.getUniqueId())) {
            return;
        }

        removeEffects(player);
        bossBar.hide(player);
        if (dynmap != null) {
            dynmap.show(player);
        }

        Message notice = quitting ? Message.PLAYER_QUIT_VANISHED : Message.PLAYER_BECAME_VISIBLE;
        String noticeText = messages.format(notice, "{player}", player.getDisplayName());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (!quitting) {
                other.showPlayer(plugin, player);
            }
            if (canSeeVanished(other)) {
                messages.send(other, noticeText);
            }
        }
        if (!quitting) {
            messages.send(player, Message.YOU_BECAME_VISIBLE);
            sendNotification(player, false);
        }

        plugin.debug(player.getName() + " is now visible");
        Bukkit.getPluginManager().callEvent(new VisibilityChangeEvent(vanishPlayer, quitting));
    }

    /**
     * Hides all vanished players from the viewer, unless the viewer may see vanished players.
     */
    public void hideVanishedPlayersFrom(Player viewer) {
        if (canSeeVanished(viewer)) {
            return;
        }
        for (Player vanishedPlayer : getOnlineVanishedPlayers()) {
            if (!vanishedPlayer.equals(viewer)) {
                viewer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    /**
     * Clears hide state this plugin may still hold for the player from a previous vanished session.
     */
    public void revealToOthers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.showPlayer(plugin, player);
            }
        }
    }

    public void refreshBossBar() {
        bossBar.refresh(getOnlineVanishedPlayers());
    }

    /**
     * Makes every vanished player visible without notifications. Used when the plugin is disabled.
     */
    public void shutdown() {
        for (Player player : getOnlineVanishedPlayers()) {
            removeEffects(player);
            revealToOthers(player);
            if (dynmap != null) {
                dynmap.show(player);
            }
        }
        vanished.clear();
        players.clear();
        bossBar.removeAll();
    }

    private List<Player> getOnlineVanishedPlayers() {
        List<Player> result = new ArrayList<>();
        for (UUID uuid : vanished) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    private void applyEffects(Player player) {
        player.setMetadata(VANISHED_METADATA_KEY, new FixedMetadataValue(plugin, true));
        player.setCollidable(!config.isPlayerPushDisabled());
        player.setCanPickupItems(!config.isItemPickupDisabled());

        boolean nightVision = config.isNightVisionEnabled();
        if (nightVision) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION,
                    0, false, false));
        }

        boolean flight = config.isFlightEnabled();
        boolean couldFly = player.getAllowFlight();
        if (flight) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        appliedEffects.put(player.getUniqueId(), new AppliedEffects(nightVision, flight, couldFly));
    }

    private void removeEffects(Player player) {
        player.removeMetadata(VANISHED_METADATA_KEY, plugin);
        player.setCollidable(true);
        player.setCanPickupItems(true);

        AppliedEffects effects = appliedEffects.remove(player.getUniqueId());
        if (effects == null) {
            return;
        }
        if (effects.nightVision()) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        GameMode gameMode = player.getGameMode();
        boolean alwaysFlies = gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
        if (effects.flight() && !effects.couldFly() && !alwaysFlies) {
            player.setAllowFlight(false);
        }
    }

    private void sendNotification(Player player, boolean nowVanished) {
        if (config.isActionBarNotificationEnabled()) {
            String text = messages.get(nowVanished
                    ? Message.NOTIFICATION_VANISHED_ACTIONBAR : Message.NOTIFICATION_VISIBLE_ACTIONBAR);
            if (!text.isEmpty()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
            }
        }
        if (config.isTitleNotificationEnabled()) {
            String title = messages.get(nowVanished
                    ? Message.NOTIFICATION_VANISHED_TITLE : Message.NOTIFICATION_VISIBLE_TITLE);
            if (!title.isEmpty()) {
                player.sendTitle(title, "", 10, 40, 10);
            }
        }
    }

    /**
     * What {@link #applyEffects} changed, so {@link #removeEffects} restores exactly that.
     */
    private record AppliedEffects(boolean nightVision, boolean flight, boolean couldFly) {
    }
}
