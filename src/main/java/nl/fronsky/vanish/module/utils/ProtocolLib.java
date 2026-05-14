/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProtocolLib {
    private static final long SILENT_CONTAINER_TTL_MS = 2500; // enough for open + close animation/sounds
    private final Set<PacketAdapter> registeredAdapters = new HashSet<>();
    // Players currently opening containers in "silent" mode.
    private final Set<UUID> silentContainerPlayers = ConcurrentHashMap.newKeySet();
    /**
     * Cache of last silent container interactions.
     * Keyed by vanished player's UUID so we can support multiple vanished players at once.
     */
    private final Map<UUID, SilentContainerContext> silentContainerContext = new ConcurrentHashMap<>();
    // We register these once and keep them for the lifetime of the plugin.
    private PacketAdapter serverInfoAdapter;
    private PacketAdapter silentContainerAdapter;

    /**
     * Enable ProtocolLib hooks. Call once on plugin enable.
     */
    public void enable(Data data) {
        if (data == null || data.getPlugin() == null) return;

        try {
            registerServerInfoAdapter(data);
            registerSilentContainerAdapter(data);
            Logger.info("ProtocolLib hooks enabled.");
        } catch (Exception e) {
            Logger.exception("Failed to enable ProtocolLib hooks", e);
        }
    }

    /**
     * Marks a player as currently opening a container silently.
     */
    public void setSilentContainer(Player player, boolean silent) {
        if (player == null) return;
        if (silent) silentContainerPlayers.add(player.getUniqueId());
        else silentContainerPlayers.remove(player.getUniqueId());
    }

    public boolean isSilentContainer(Player player) {
        return player != null && silentContainerPlayers.contains(player.getUniqueId());
    }

    /**
     * Updates information about online players for server status ping.
     * This is called when visibility changes.
     */
    public void updateOnlinePlayers(Data data, VanishPlayer player, boolean quit) {
        // Nothing to do here anymore besides ensuring adapter exists.
        // The adapter reads fresh data on each ping to avoid stale values.
        if (serverInfoAdapter == null) {
            registerServerInfoAdapter(data);
        }
    }

    private void registerServerInfoAdapter(Data data) {
        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();

            if (serverInfoAdapter != null) {
                pm.removePacketListener(serverInfoAdapter);
                registeredAdapters.remove(serverInfoAdapter);
            }

            serverInfoAdapter = new PacketAdapter(data.getPlugin(), ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    try {
                        WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                        if (ping == null) return;

                        int online = MetaData.getOnlinePlayers().size() - MetaData.getVanishedPlayersAmount(data);
                        if (online < 0) online = 0;

                        Collection<Player> visiblePlayers = new HashSet<>();
                        for (VanishPlayer vanishPlayer : MetaData.getOnlinePlayers()) {
                            if (vanishPlayer.getState().equals(State.VISIBLE)) {
                                visiblePlayers.add(vanishPlayer.getPlayer());
                            }
                        }

                        ping.setPlayersOnline(online);
                        // Only set sample list if supported; ProtocolLib handles version differences internally.
                        ping.setBukkitPlayers(visiblePlayers);
                    } catch (Exception e) {
                        Logger.debug("Error updating server ping: " + e.getMessage());
                    }
                }
            };

            pm.addPacketListener(serverInfoAdapter);
            registeredAdapters.add(serverInfoAdapter);
        } catch (Exception e) {
            Logger.exception("Failed to register SERVER_INFO adapter", e);
        }
    }

    /**
     * Packet-level suppression of container sounds/animations for players marked as silent.
     * <p>
     * What this does:
     * - Cancels BLOCK_ACTION packets related to containers (chest/shulker etc.)
     * - Cancels NAMED_SOUND_EFFECT packets in the BLOCKS category
     * <p>
     * Important: this cancels packets *to the silent player only*.
     * Other players nearby may still hear sounds/see animations depending on server behavior.
     * To avoid that too, we also cancel broadcast sounds when the *source player* is silent.
     */
    private void registerSilentContainerAdapter(Data data) {
        try {
            ProtocolManager pm = ProtocolLibrary.getProtocolManager();

            if (silentContainerAdapter != null) {
                pm.removePacketListener(silentContainerAdapter);
                registeredAdapters.remove(silentContainerAdapter);
            }

            silentContainerAdapter = new PacketAdapter(data.getPlugin(), ListenerPriority.HIGHEST,
                    PacketType.Play.Server.BLOCK_ACTION,
                    PacketType.Play.Server.NAMED_SOUND_EFFECT
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    try {
                        Player receiver = event.getPlayer();
                        if (receiver == null) return;

                        // Case 1: opener themselves in silent container mode
                        boolean openerSilent = isSilentContainer(receiver) && MetaData.getVanishState(receiver, data).equals(State.HIDDEN);

                        // Case 2: observer near an active silent container location (vanished opener)
                        boolean observerSilent = !openerSilent && shouldSilenceForObserver(data, receiver, event);

                        if (!openerSilent && !observerSilent) return;

                        StructureModifier<Object> modifier = event.getPacket().getModifier();
                        if (modifier == null) return;

                        if (event.getPacketType().equals(PacketType.Play.Server.BLOCK_ACTION)) {
                            // Best-effort: cancel container block actions (chest/shulker/barrel)
                            Object block = null;
                            try {
                                block = modifier.read(3);
                            } catch (Exception ignored) {
                            }
                            if (block != null) {
                                String s = block.toString().toLowerCase();
                                if (s.contains("chest") || s.contains("shulker") || s.contains("barrel")) {
                                    // For observers, we still cancel, because we want 100% silence/animation.
                                    event.setCancelled(true);
                                }
                            }
                            return;
                        }

                        if (event.getPacketType().equals(PacketType.Play.Server.NAMED_SOUND_EFFECT)) {
                            Object soundCategory = null;
                            try {
                                // category index for NAMED_SOUND_EFFECT
                                soundCategory = modifier.read(1);
                            } catch (Exception ignored) {
                            }

                            if (soundCategory != null && soundCategory.toString().equalsIgnoreCase("BLOCKS")) {
                                if (isContainerSound(event)) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.debug("Silent container packet error: " + e.getMessage());
                    }
                }
            };

            pm.addPacketListener(silentContainerAdapter);
            registeredAdapters.add(silentContainerAdapter);
        } catch (Exception e) {
            Logger.exception("Failed to register silent container adapter", e);
        }
    }

    /**
     * Call when a vanished player is about to open a container silently.
     * This enables packet suppression for the opener AND for observers around the container location.
     */
    public void beginSilentContainer(Player opener, Location containerLocation) {
        if (opener == null || containerLocation == null) return;
        setSilentContainer(opener, true);
        silentContainerContext.put(opener.getUniqueId(), new SilentContainerContext(containerLocation.clone(), System.currentTimeMillis() + SILENT_CONTAINER_TTL_MS));
    }

    /**
     * Call when the vanished player is done (inventory closed).
     */
    public void endSilentContainer(Player opener) {
        if (opener == null) return;
        setSilentContainer(opener, false);
        silentContainerContext.remove(opener.getUniqueId());
    }

    private void cleanupExpiredSilentContexts() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, SilentContainerContext>> it = silentContainerContext.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, SilentContainerContext> entry = it.next();
            if (entry.getValue() == null || entry.getValue().expiresAt < now) {
                it.remove();
            }
        }
    }

    private boolean isNear(Location a, Location b) {
        if (a == null || b == null) return false;
        if (a.getWorld() == null || b.getWorld() == null) return false;
        if (!a.getWorld().equals(b.getWorld())) return false;
        // vanilla uses whole block coords for block actions; use same block + small radius.
        if (a.getBlockX() != b.getBlockX()) return false;
        if (a.getBlockY() != b.getBlockY()) return false;
        return a.getBlockZ() == b.getBlockZ();
    }

    private boolean shouldSilenceForObserver(Data data, Player receiver, PacketEvent event) {
        if (receiver == null || data == null) return false;

        cleanupExpiredSilentContexts();
        if (silentContainerContext.isEmpty()) return false;

        // Only bother if receiver is in a world where a silent container is active.
        Location receiverLoc = receiver.getLocation();
        if (receiverLoc == null) return false;

        // For each active silent container, check if receiver is close enough to have gotten the sound/animation.
        // In practice Minecraft sends these packets to tracking players; we don't know exact tracking rules here,
        // so we use a conservative radius check.
        for (SilentContainerContext ctx : silentContainerContext.values()) {
            if (ctx == null || ctx.location == null) continue;
            if (ctx.location.getWorld() == null || receiverLoc.getWorld() == null) continue;
            if (!ctx.location.getWorld().equals(receiverLoc.getWorld())) continue;

            // if receiver is within 64 blocks, they could normally see/hear it.
            if (receiverLoc.distanceSquared(ctx.location) <= (64 * 64)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cleans up all registered packet listeners.
     */
    public void cleanup() {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            for (PacketAdapter adapter : registeredAdapters) {
                protocolManager.removePacketListener(adapter);
            }
            registeredAdapters.clear();
            serverInfoAdapter = null;
            silentContainerAdapter = null;
            silentContainerPlayers.clear();
            Logger.debug("ProtocolLib packet listeners cleaned up");
        } catch (Exception e) {
            Logger.exception("Failed to cleanup ProtocolLib listeners", e);
        }
    }

    private boolean isContainerSound(PacketEvent event) {
        try {
            if (!event.getPacketType().equals(PacketType.Play.Server.NAMED_SOUND_EFFECT)) {
                return false;
            }

            Object soundEffect = null;
            try {
                soundEffect = event.getPacket().getSoundEffects().read(0);
            } catch (Exception ignored) {
            }

            if (soundEffect == null) {
                try {
                    StructureModifier<Object> modifier = event.getPacket().getModifier();
                    if (modifier != null && modifier.size() > 0) {
                        soundEffect = modifier.read(0);
                    }
                } catch (Exception ignored) {
                }
            }

            if (soundEffect == null) {
                return false;
            }

            String s = soundEffect.toString().toLowerCase();

            if (s.contains("chestplate")) {
                return false;
            }

            boolean openClose = s.contains("open") || s.contains("close");
            boolean isChest = s.contains("chest") && openClose && !s.contains("chestplate");
            boolean isEnderChest = (s.contains("ender_chest") || s.contains("ender chest") || s.contains("enderchest")) && openClose;
            boolean isShulker = s.contains("shulker") && openClose;
            boolean isBarrel = s.contains("barrel") && openClose;

            if (isEnderChest || isShulker || isBarrel) return true;
            return isChest && !s.contains("ender");
        } catch (Exception ignored) {
            return false;
        }
    }

    private record SilentContainerContext(Location location, long expiresAt) {
    }
}
