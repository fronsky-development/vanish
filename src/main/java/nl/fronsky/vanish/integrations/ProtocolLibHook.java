/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import nl.fronsky.vanish.VanishPlugin;
import nl.fronsky.vanish.module.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Packet-level features that require ProtocolLib: an accurate server list, silent containers and suppression of
 * advancement announcements. Packet handlers run on network threads and only use thread-safe state.
 */
public final class ProtocolLibHook {
    private static final long ADVANCEMENT_ANNOUNCEMENT_TTL_MS = 5000;

    private final VanishPlugin vanishPlugin;
    private final VanishManager vanishManager;
    private final List<PacketAdapter> adapters = new ArrayList<>();
    private final SilentContainerSessions silentContainers = new SilentContainerSessions();
    private final Map<UUID, PendingAnnouncement> pendingAnnouncements = new ConcurrentHashMap<>();
    private volatile boolean soundEffectsReadable = true;

    public ProtocolLibHook(VanishPlugin vanishPlugin, VanishManager vanishManager) {
        this.vanishPlugin = vanishPlugin;
        this.vanishManager = vanishManager;
    }

    /**
     * Registers the packet listeners. A listener that the running ProtocolLib version does not support is
     * skipped, the others still work.
     */
    public void register() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        register(protocolManager, "server list", this::createServerInfoAdapter);
        register(protocolManager, "silent container", this::createSilentContainerAdapter);
        register(protocolManager, "advancement announcement", this::createAdvancementChatAdapter);
    }

    public void unregister() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        adapters.forEach(protocolManager::removePacketListener);
        adapters.clear();
        silentContainers.clear();
        pendingAnnouncements.clear();
    }

    /**
     * Suppresses container animations and sounds for the opener and for players near the container until
     * {@link #endSilentContainer(UUID)} is called.
     */
    public void beginSilentContainer(Player opener, Location containerLocation) {
        silentContainers.begin(opener.getUniqueId(), containerLocation.getWorld().getUID(), containerLocation.getX(),
                containerLocation.getY(), containerLocation.getZ(), System.currentTimeMillis());
    }

    /**
     * Ends the silent container of the opener; nearby players stay muted briefly for the closing animation.
     */
    public void endSilentContainer(UUID opener) {
        silentContainers.end(opener, System.currentTimeMillis());
    }

    public boolean isSilentContainer(UUID player) {
        return silentContainers.isOpener(player);
    }

    /**
     * Cancels the chat announcement of the advancement the player just completed.
     */
    public void suppressAdvancementAnnouncement(Player player) {
        String displayName = ChatColor.stripColor(player.getDisplayName());
        if (displayName == null || displayName.isBlank()) {
            displayName = player.getName();
        }
        pendingAnnouncements.put(player.getUniqueId(), new PendingAnnouncement(
                player.getName().toLowerCase(Locale.ROOT),
                displayName.toLowerCase(Locale.ROOT),
                System.currentTimeMillis() + ADVANCEMENT_ANNOUNCEMENT_TTL_MS));
    }

    private void register(ProtocolManager protocolManager, String feature, Supplier<PacketAdapter> factory) {
        try {
            PacketAdapter adapter = factory.get();
            protocolManager.addPacketListener(adapter);
            adapters.add(adapter);
        } catch (RuntimeException | LinkageError e) {
            vanishPlugin.getLogger().log(Level.WARNING, "ProtocolLib " + feature + " support is unavailable.", e);
        }
    }

    private PacketAdapter createServerInfoAdapter() {
        return new PacketAdapter(vanishPlugin, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                    if (ping == null) {
                        return;
                    }
                    List<Player> visiblePlayers = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!vanishManager.isVanished(player)) {
                            visiblePlayers.add(player);
                        }
                    }
                    ping.setPlayersOnline(visiblePlayers.size());
                    ping.setBukkitPlayers(visiblePlayers);
                } catch (RuntimeException | LinkageError e) {
                    vanishPlugin.debug("Could not update the server list ping: " + e);
                }
            }
        };
    }

    private PacketAdapter createSilentContainerAdapter() {
        return new PacketAdapter(vanishPlugin, ListenerPriority.HIGHEST,
                PacketType.Play.Server.BLOCK_ACTION, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    Player receiver = event.getPlayer();
                    if (receiver == null || !shouldSilence(receiver)) {
                        return;
                    }
                    boolean containerPacket = event.getPacketType().equals(PacketType.Play.Server.BLOCK_ACTION)
                            ? isContainerBlockAction(event)
                            : isContainerSound(event);
                    if (containerPacket) {
                        event.setCancelled(true);
                    }
                } catch (RuntimeException | LinkageError e) {
                    vanishPlugin.debug("Silent container packet error: " + e);
                }
            }
        };
    }

    private PacketAdapter createAdvancementChatAdapter() {
        return new PacketAdapter(vanishPlugin, ListenerPriority.HIGHEST, PacketType.Play.Server.SYSTEM_CHAT,
                PacketType.Play.Server.CHAT, PacketType.Play.Server.DISGUISED_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (isAdvancementAnnouncement(event)) {
                        event.setCancelled(true);
                    }
                } catch (RuntimeException | LinkageError e) {
                    vanishPlugin.debug("Advancement announcement packet error: " + e);
                }
            }
        };
    }

    /**
     * @return whether the receiver is a vanished player opening a container silently, or is close enough to an
     * active silent container to see or hear it
     */
    private boolean shouldSilence(Player receiver) {
        if (silentContainers.isOpener(receiver.getUniqueId()) && vanishManager.isVanished(receiver)) {
            return true;
        }
        Location location = receiver.getLocation();
        return location.getWorld() != null && silentContainers.isNearContainer(location.getWorld().getUID(),
                location.getX(), location.getY(), location.getZ(), System.currentTimeMillis());
    }

    private static boolean isContainerBlockAction(PacketEvent event) {
        Object block = readField(event, 3);
        if (block == null) {
            return false;
        }
        String name = block.toString().toLowerCase(Locale.ROOT);
        return name.contains("chest") || name.contains("shulker") || name.contains("barrel");
    }

    private boolean isContainerSound(PacketEvent event) {
        Object category = readField(event, 1);
        if (category == null || !category.toString().equalsIgnoreCase("BLOCKS")) {
            return false;
        }

        Object soundEffect = null;
        if (soundEffectsReadable) {
            try {
                soundEffect = event.getPacket().getSoundEffects().read(0);
            } catch (RuntimeException | LinkageError e) {
                // Unsupported packet layout (e.g. ProtocolLib 5.4.0 on 1.21.11); use the raw field from now on.
                soundEffectsReadable = false;
            }
        }
        if (soundEffect == null) {
            soundEffect = readField(event, 0);
        }
        if (soundEffect == null) {
            return false;
        }

        String sound = soundEffect.toString().toLowerCase(Locale.ROOT);
        if (sound.contains("chestplate") || !(sound.contains("open") || sound.contains("close"))) {
            return false;
        }
        boolean enderChest = sound.contains("ender_chest") || sound.contains("ender chest")
                || sound.contains("enderchest");
        boolean chest = sound.contains("chest") && !sound.contains("ender");
        return enderChest || chest || sound.contains("shulker") || sound.contains("barrel");
    }

    private boolean isAdvancementAnnouncement(PacketEvent event) {
        long now = System.currentTimeMillis();
        pendingAnnouncements.values().removeIf(announcement -> announcement.expiresAt() < now);
        if (pendingAnnouncements.isEmpty()) {
            return false;
        }

        String text = readPacketText(event);
        if (text.isEmpty()) {
            return false;
        }
        // "advancement" also matches the translation keys chat.type.advancement.task/goal/challenge.
        boolean mentionsAdvancement = text.contains("advancement")
                || text.contains("has reached the goal")
                || text.contains("has completed the challenge");
        if (!mentionsAdvancement) {
            return false;
        }
        for (PendingAnnouncement announcement : pendingAnnouncements.values()) {
            if ((!announcement.name().isEmpty() && text.contains(announcement.name()))
                    || (!announcement.displayName().isEmpty() && text.contains(announcement.displayName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the lower-cased text of all chat components and fields of the packet
     */
    private static String readPacketText(PacketEvent event) {
        StringBuilder builder = new StringBuilder();
        try {
            StructureModifier<WrappedChatComponent> components = event.getPacket().getChatComponents();
            for (int i = 0; i < components.size(); i++) {
                WrappedChatComponent component = components.read(i);
                if (component != null && component.getJson() != null) {
                    builder.append(component.getJson()).append(' ');
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            // Not every chat packet has chat component fields.
        }
        try {
            StructureModifier<Object> fields = event.getPacket().getModifier();
            for (int i = 0; i < fields.size(); i++) {
                Object value = fields.read(i);
                if (value != null) {
                    builder.append(value).append(' ');
                }
            }
        } catch (RuntimeException ignored) {
            // Unreadable fields are skipped.
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private static Object readField(PacketEvent event, int index) {
        try {
            StructureModifier<Object> fields = event.getPacket().getModifier();
            return index < fields.size() ? fields.read(index) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private record PendingAnnouncement(String name, String displayName, long expiresAt) {
    }
}
