/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A 54-slot menu that shows up to {@value #PAGE_SIZE} entries per page with arrows to switch pages.
 */
abstract class PagedMenu extends Menu {
    static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int NEXT_SLOT = 53;

    private final int page;
    private final int pageCount;

    /**
     * @param requestedPage the page to show, clamped to the available pages
     * @param entryCount    the total number of entries across all pages
     */
    PagedMenu(String name, ChatColor accent, int requestedPage, int entryCount) {
        super(54, accent + name + " " + ChatColor.DARK_GRAY + "(" + ChatColor.GRAY
                + clampPage(requestedPage, entryCount) + "/" + countPages(entryCount) + ChatColor.DARK_GRAY + ")");
        this.page = clampPage(requestedPage, entryCount);
        this.pageCount = countPages(entryCount);
        if (page > 1) {
            getInventory().setItem(PREVIOUS_SLOT, createArrow("Previous Page", page - 1));
        }
        if (page < pageCount) {
            getInventory().setItem(NEXT_SLOT, createArrow("Next Page", page + 1));
        }
    }

    /**
     * @return the index of the first entry shown on this page
     */
    final int getFirstIndex() {
        return (page - 1) * PAGE_SIZE;
    }

    /**
     * @return the index after the last entry shown on this page
     */
    final int getEndIndex(int entryCount) {
        return Math.min(getFirstIndex() + PAGE_SIZE, entryCount);
    }

    @Override
    protected final void onClick(Player viewer, int slot, ClickType click) {
        if (slot == PREVIOUS_SLOT && page > 1) {
            openPage(viewer, page - 1);
        } else if (slot == NEXT_SLOT && page < pageCount) {
            openPage(viewer, page + 1);
        } else if (slot < PAGE_SIZE) {
            onEntryClick(viewer, getFirstIndex() + slot, click);
        }
    }

    protected abstract void openPage(Player viewer, int page);

    /**
     * @param index the index of the clicked entry; may be past the last entry when an empty slot was clicked
     */
    protected abstract void onEntryClick(Player viewer, int index, ClickType click);

    private static int countPages(int entryCount) {
        return Math.max(1, (entryCount + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private static int clampPage(int page, int entryCount) {
        return Math.min(Math.max(page, 1), countPages(entryCount));
    }

    private static ItemStack createArrow(String name, int targetPage) {
        return createItem(Material.ARROW, ChatColor.GREEN + name, List.of(ChatColor.GRAY + "Go to page " + targetPage));
    }
}
