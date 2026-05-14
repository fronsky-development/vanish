/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import org.bukkit.Bukkit;

public class MinecraftVersion {
    private static String cachedVersion = null;
    private static int cachedMajor = -1;
    private static int cachedMinor = -1;
    private static int cachedPatch = -1;

    /**
     * Retrieves the current Bukkit version.
     *
     * @return the version of Bukkit currently running
     */
    public static String getCurrentVersion() {
        if (cachedVersion == null) {
            String bukkitVersion = Bukkit.getBukkitVersion();
            String[] split = bukkitVersion.split("-");
            cachedVersion = split[0];
            parseVersion();
        }
        return cachedVersion;
    }

    /**
     * Parses the version into major, minor, and patch components.
     */
    private static void parseVersion() {
        try {
            String version = cachedVersion;
            String[] parts = version.split("\\.");
            cachedMajor = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            cachedMinor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            cachedPatch = parts.length > 2 ? Integer.parseInt(parts[2].replaceAll("[^0-9]", "")) : 0;
        } catch (Exception e) {
            cachedMajor = 1;
            cachedMinor = 13;
            cachedPatch = 0;
        }
    }

    /**
     * Checks if the current version is at least the specified version.
     *
     * @param major the major version
     * @param minor the minor version
     * @return {@code true} if current version is >= specified version
     */
    public static boolean isAtLeast(int major, int minor) {
        getCurrentVersion();
        if (cachedMajor > major) return true;
        return cachedMajor == major && cachedMinor >= minor;
    }

    /**
     * Checks if the server is running version 1.13 or newer.
     *
     * @return {@code true} if version is 1.13+
     */
    public static boolean is1_13OrNewer() {
        return isAtLeast(1, 13);
    }

    /**
     * Checks if the server is running version 1.16 or newer.
     *
     * @return {@code true} if version is 1.16+
     */
    public static boolean is1_16OrNewer() {
        return isAtLeast(1, 16);
    }

    /**
     * Checks if the server is running version 1.19 or newer.
     *
     * @return {@code true} if version is 1.19+
     */
    public static boolean is1_19OrNewer() {
        return isAtLeast(1, 19);
    }

    /**
     * Checks if the server is running version 1.20 or newer.
     *
     * @return {@code true} if version is 1.20+
     */
    public static boolean is1_20OrNewer() {
        return isAtLeast(1, 20);
    }

    /**
     * Checks if the server is running version 1.21 or newer.
     *
     * @return {@code true} if version is 1.21+
     */
    public static boolean is1_21OrNewer() {
        return isAtLeast(1, 21);
    }

    /**
     * Gets the major version number.
     *
     * @return the major version
     */
    public static int getMajorVersion() {
        getCurrentVersion();
        return cachedMajor;
    }

    /**
     * Gets the minor version number.
     *
     * @return the minor version
     */
    public static int getMinorVersion() {
        getCurrentVersion();
        return cachedMinor;
    }

    /**
     * Gets the patch version number.
     *
     * @return the patch version
     */
    public static int getPatchVersion() {
        getCurrentVersion();
        return cachedPatch;
    }
}
