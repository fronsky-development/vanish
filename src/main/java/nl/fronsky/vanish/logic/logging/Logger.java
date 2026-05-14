/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.logging;

import nl.fronsky.vanish.Main;
import nl.fronsky.vanish.logic.utils.ColorUtil;

import java.util.logging.Level;

public class Logger {
    private static boolean debugEnabled = false;

    private static java.util.logging.Logger getLogger() {
        Main plugin = Main.getInstance();
        if (plugin != null) {
            return plugin.getLogger();
        }
        // Fallback for early lifecycle / tests
        return java.util.logging.Logger.getLogger("Vanish");
    }

    /**
     * Checks if debug logging is enabled.
     *
     * @return {@code true} if debug logging is enabled, {@code false} otherwise
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Enables or disables debug logging.
     *
     * @param enabled {@code true} to enable debug logging, {@code false} to disable
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to be logged
     */
    public static void info(String message) {
        getLogger().log(Level.INFO, sanitize(message));
    }

    /**
     * Logs a debug message (only if debug mode is enabled).
     *
     * @param message the message to be logged
     */
    public static void debug(String message) {
        if (debugEnabled) {
            getLogger().log(Level.INFO, "[DEBUG] " + sanitize(message));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to be logged
     */
    public static void warning(String message) {
        getLogger().log(Level.WARNING, sanitize(message));
    }

    /**
     * Logs a severe (error) message.
     *
     * @param message the message to be logged
     */
    public static void severe(String message) {
        getLogger().log(Level.SEVERE, sanitize(message));
    }

    /**
     * Logs a severe (error) message with exception details.
     *
     * @param message   the message to be logged
     * @param throwable the exception to log
     */
    public static void severe(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, sanitize(message), throwable);
    }

    /**
     * Logs an exception with debug information.
     *
     * @param message   the message to be logged
     * @param throwable the exception to log
     */
    public static void exception(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, sanitize(message) + ": " + (throwable != null ? throwable.getMessage() : ""), throwable);
    }

    private static String sanitize(String message) {
        if (message == null) {
            return "";
        }
        // Strip Minecraft formatting codes for console readability
        return ColorUtil.decolorize(message);
    }
}
