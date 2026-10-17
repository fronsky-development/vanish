/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets an admin preview (left click) and select (right click) the sound played when vanishing.
 */
public final class SoundMenu extends PagedMenu {
    private static final int HINT_SLOT = 49;

    private final MenuListener menus;
    private final VanishConfig config;
    private final Sound[] sounds;

    public SoundMenu(MenuListener menus, VanishConfig config, int page) {
        this(menus, config, Sound.values(), page);
    }

    private SoundMenu(MenuListener menus, VanishConfig config, Sound[] sounds, int page) {
        super("Select Sound", config.getAccentColor(), page, sounds.length);
        this.menus = menus;
        this.config = config;
        this.sounds = sounds;

        Sound selected = config.getSound();
        for (int i = getFirstIndex(); i < getEndIndex(sounds.length); i++) {
            getInventory().setItem(i - getFirstIndex(), createSoundItem(sounds[i], sounds[i].equals(selected)));
        }
        getInventory().setItem(HINT_SLOT, createItem(Material.PAPER, ChatColor.YELLOW + "How it works", List.of(
                ChatColor.GRAY + "Pick the sound used when vanishing.",
                ChatColor.DARK_GRAY + "Preview = play sound once.",
                ChatColor.DARK_GRAY + "Select = save to config.")));
    }

    @Override
    protected void openPage(Player viewer, int page) {
        menus.open(viewer, new SoundMenu(menus, config, sounds, page));
    }

    @Override
    protected void onEntryClick(Player viewer, int index, ClickType click) {
        if (index >= sounds.length) {
            return;
        }
        Sound sound = sounds[index];
        if (click.isLeftClick()) {
            viewer.playSound(viewer.getLocation(), sound, 1.0f, 1.0f);
        } else if (click.isRightClick()) {
            config.setSound(sound);
            viewer.closeInventory();
        }
    }

    private static ItemStack createSoundItem(Sound sound, boolean selected) {
        List<String> lore = new ArrayList<>();
        if (selected) {
            lore.add(ChatColor.DARK_GRAY + "Currently selected");
            lore.add("");
        }
        lore.add(ChatColor.GRAY + "Left click: " + ChatColor.WHITE + "Preview");
        lore.add(ChatColor.GRAY + "Right click: " + ChatColor.WHITE + "Select");
        String name = (selected ? ChatColor.GREEN + "✔ " : "") + ChatColor.GRAY + sound.name();
        return createItem(selected ? Material.JUKEBOX : Material.NOTE_BLOCK, name, lore);
    }
}
