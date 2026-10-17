/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.integrations;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which containers vanished players are opening silently, so their animation and sounds can be muted.
 * A session lasts while the container is open; afterwards nearby players stay muted for a short grace period
 * to cover the closing animation. Safe to use from network threads.
 */
final class SilentContainerSessions {
    // Safety net for a session that is never ended; a normal session ends when the inventory closes.
    static final long MAX_OPEN_MS = 30 * 60 * 1000;
    static final long CLOSE_GRACE_MS = 1000;
    private static final double OBSERVER_RANGE_SQUARED = 64 * 64;

    private final Set<UUID> openers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    void begin(UUID opener, UUID worldId, double x, double y, double z, long now) {
        openers.add(opener);
        sessions.put(opener, new Session(worldId, x, y, z, now + MAX_OPEN_MS));
    }

    void end(UUID opener, long now) {
        openers.remove(opener);
        sessions.computeIfPresent(opener, (uuid, session) -> session.expiringAt(now + CLOSE_GRACE_MS));
    }

    boolean isOpener(UUID player) {
        return openers.contains(player);
    }

    /**
     * @return whether a position is close enough to an active or just closed silent container to see or hear it
     */
    boolean isNearContainer(UUID worldId, double x, double y, double z, long now) {
        sessions.values().removeIf(session -> session.expiresAt() < now);
        for (Session session : sessions.values()) {
            if (session.worldId().equals(worldId) && session.distanceSquared(x, y, z) <= OBSERVER_RANGE_SQUARED) {
                return true;
            }
        }
        return false;
    }

    void clear() {
        openers.clear();
        sessions.clear();
    }

    private record Session(UUID worldId, double x, double y, double z, long expiresAt) {
        Session expiringAt(long time) {
            return new Session(worldId, x, y, z, Math.min(expiresAt, time));
        }

        double distanceSquared(double otherX, double otherY, double otherZ) {
            double dx = x - otherX;
            double dy = y - otherY;
            double dz = z - otherZ;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
