/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import org.bukkit.ChatColor;

import java.util.Locale;

public final class ColorUtil {

    private ColorUtil() {
    }

    /**
     * Maps a plugin color name (a {@code BarColor} or {@code ChatColor} name) to the chat color used for accents.
     * PINK and PURPLE, which have no chat color with that name, map to LIGHT_PURPLE and DARK_PURPLE.
     *
     * @param colorName the configured color name
     * @return the matching chat color, or {@code ChatColor.BLUE} if the name is unknown
     */
    public static ChatColor parsePluginColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return ChatColor.BLUE;
        }
        String key = colorName.toUpperCase(Locale.ROOT);
        switch (key) {
            case "PINK":
                return ChatColor.LIGHT_PURPLE;
            case "PURPLE":
                return ChatColor.DARK_PURPLE;
            default:
                try {
                    return ChatColor.valueOf(key);
                } catch (IllegalArgumentException e) {
                    return ChatColor.BLUE;
                }
        }
    }

    /**
     * Translates {@code &} color codes.
     *
     * @param message the text to color
     * @return the colored text
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
