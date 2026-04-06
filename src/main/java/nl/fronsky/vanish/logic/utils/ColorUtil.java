/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import nl.fronsky.vanish.logic.logging.Logger;
import org.bukkit.ChatColor;

public class ColorUtil {

    private ColorUtil() {
    }

    /**
     * Parses a plugin color key (from config) into a {@link ChatColor}.
     * Handles BarColor-to-ChatColor mapping for PINK and PURPLE.
     *
     * @param colorKey the color key from config (e.g. "BLUE", "PINK", "PURPLE")
     * @return the corresponding ChatColor, or {@code ChatColor.BLUE} as default
     */
    public static ChatColor parsePluginColor(String colorKey) {
        if (colorKey == null || colorKey.isEmpty()) {
            return ChatColor.BLUE;
        }
        try {
            return switch (colorKey.toUpperCase()) {
                case "PINK" -> ChatColor.LIGHT_PURPLE;
                case "PURPLE" -> ChatColor.DARK_PURPLE;
                default -> ChatColor.valueOf(colorKey.toUpperCase());
            };
        } catch (IllegalArgumentException e) {
            Logger.warning("Invalid plugin color '" + colorKey + "', using BLUE as default.");
            return ChatColor.BLUE;
        }
    }

    /**
     * Applies color formatting to a string.
     *
     * @param message the string to apply color formatting to
     * @return the string with color formatting applied
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Applies color formatting to a string using an alternate color character.
     *
     * @param message      the string to apply color formatting to
     * @param altColorChar the alternate color character to use for color codes
     * @return the string with color formatting applied
     */
    public static String colorize(String message, char altColorChar) {
        return ChatColor.translateAlternateColorCodes(altColorChar, message);
    }

    /**
     * Removes color codes from a string.
     *
     * @param message the string from which to remove color codes
     * @return the string with color codes removed
     */
    public static String decolorize(String message) {
        return ChatColor.stripColor(message);
    }

    /**
     * Retrieves the ChatColor corresponding to a given color code character.
     *
     * @param code the color code character to retrieve the ChatColor for
     * @return the ChatColor corresponding to the given color code character, or ChatColor.WHITE
     * if no corresponding ChatColor is found
     */
    public static ChatColor getChatColor(char code) {
        ChatColor chatColor = ChatColor.getByChar(code);
        if (chatColor == null) {
            return ChatColor.WHITE;
        }
        return chatColor;
    }
}
