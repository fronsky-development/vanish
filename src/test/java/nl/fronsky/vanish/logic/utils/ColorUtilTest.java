/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorUtilTest {

    @Test
    void everyBarColorHasAnAccentColor() {
        for (BarColor color : BarColor.values()) {
            assertTrue(ColorUtil.parsePluginColor(color.name()).isColor(), color.name());
        }
        assertEquals(ChatColor.LIGHT_PURPLE, ColorUtil.parsePluginColor("PINK"));
        assertEquals(ChatColor.DARK_PURPLE, ColorUtil.parsePluginColor("purple"));
        assertEquals(ChatColor.RED, ColorUtil.parsePluginColor("red"));
    }

    @Test
    void unknownOrMissingColorsFallBackToBlue() {
        assertEquals(ChatColor.BLUE, ColorUtil.parsePluginColor("sparkly"));
        assertEquals(ChatColor.BLUE, ColorUtil.parsePluginColor(""));
        assertEquals(ChatColor.BLUE, ColorUtil.parsePluginColor(null));
    }

    @Test
    void translatesAmpersandColorCodes() {
        assertEquals(ChatColor.GREEN + "Hi " + ChatColor.BOLD + "there", ColorUtil.colorize("&aHi &lthere"));
    }
}
