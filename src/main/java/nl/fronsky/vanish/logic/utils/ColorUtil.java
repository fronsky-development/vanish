/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ColorUtil {
    /**
     * Converts RGB values to a Color object.
     *
     * @param r the red component of the color (0-255)
     * @param g the green component of the color (0-255)
     * @param b the blue component of the color (0-255)
     * @return a Color object representing the specified RGB values
     */
    public static Color rgbToColor(int r, int g, int b) {
        return Color.fromRGB(r, g, b);
    }

    /**
     * Converts a Color object to RGB values.
     *
     * @param color the Color object to convert to RGB values
     * @return an array containing the red, green, and blue components of the color, respectively
     */
    public static int[] colorToRGB(Color color) {
        return new int[]{color.getRed(), color.getGreen(), color.getBlue()};
    }

    /**
     * Converts a Color object to a hexadecimal representation.
     *
     * @param color the Color object to convert to hexadecimal
     * @return a string representing the color in hexadecimal format
     */
    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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
