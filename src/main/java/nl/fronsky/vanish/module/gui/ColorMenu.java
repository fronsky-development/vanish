/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import nl.fronsky.vanish.logic.utils.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Lets an admin pick the plugin accent color from the available boss bar colors.
 */
public final class ColorMenu extends Menu {
    private static final List<String> LORE = List.of(
            ChatColor.GRAY + "Sets the plugin accent color.",
            ChatColor.DARK_GRAY + "Used in menus, messages, and bossbar.",
            "",
            ChatColor.YELLOW + "Click to apply");

    private final BiConsumer<Player, BarColor> colorAction;
    private final List<BarColor> colors = new ArrayList<>();

    /**
     * @param colorAction applies the color picked by the player
     */
    public ColorMenu(ChatColor accent, BiConsumer<Player, BarColor> colorAction) {
        super(9, accent + "Color Selection");
        this.colorAction = colorAction;

        for (BarColor color : BarColor.values()) {
            Material pane = Material.getMaterial(color.name() + "_STAINED_GLASS_PANE");
            if (pane != null) {
                getInventory().setItem(colors.size(), createItem(pane, displayName(color), LORE));
                colors.add(color);
            }
        }
    }

    @Override
    protected void onClick(Player viewer, int slot, ClickType click) {
        if (slot < colors.size()) {
            colorAction.accept(viewer, colors.get(slot));
            viewer.closeInventory();
        }
    }

    private static String displayName(BarColor color) {
        ChatColor chatColor = ColorUtil.parsePluginColor(color.name());
        String name = color.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return (chatColor.isColor() ? chatColor : ChatColor.WHITE) + name.substring(0, 1).toUpperCase(Locale.ROOT)
                + name.substring(1);
    }
}
