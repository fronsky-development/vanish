/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.gui;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Lists the heads of all online players; clicking a head toggles that player's vanish state.
 */
public final class PlayerListMenu extends PagedMenu {
    private final MenuListener menus;
    private final VanishConfig config;
    private final VanishManager vanishManager;
    private final BiConsumer<Player, Player> toggleAction;
    private final List<UUID> playerIds = new ArrayList<>();

    /**
     * @param toggleAction toggles the vanish state of the second player on behalf of the first
     */
    public PlayerListMenu(MenuListener menus, VanishConfig config, VanishManager vanishManager,
                          BiConsumer<Player, Player> toggleAction, int page) {
        this(menus, config, vanishManager, toggleAction, new ArrayList<>(Bukkit.getOnlinePlayers()), page);
    }

    private PlayerListMenu(MenuListener menus, VanishConfig config, VanishManager vanishManager,
                           BiConsumer<Player, Player> toggleAction, List<Player> players, int page) {
        super("Player Heads", config.getAccentColor(), page, players.size());
        this.menus = menus;
        this.config = config;
        this.vanishManager = vanishManager;
        this.toggleAction = toggleAction;

        for (int i = getFirstIndex(); i < getEndIndex(players.size()); i++) {
            Player player = players.get(i);
            playerIds.add(player.getUniqueId());
            getInventory().setItem(i - getFirstIndex(), createHead(player, vanishManager.isVanished(player)));
        }
    }

    @Override
    protected void openPage(Player viewer, int page) {
        menus.open(viewer, new PlayerListMenu(menus, config, vanishManager, toggleAction, page));
    }

    @Override
    protected void onEntryClick(Player viewer, int index, ClickType click) {
        int slot = index - getFirstIndex();
        if (slot >= playerIds.size()) {
            return;
        }
        Player target = Bukkit.getPlayer(playerIds.get(slot));
        if (target != null) {
            toggleAction.accept(viewer, target);
            viewer.closeInventory();
        }
    }

    private static ItemStack createHead(Player player, boolean vanished) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            meta.setDisplayName((vanished ? ChatColor.DARK_AQUA : ChatColor.GREEN) + player.getName());
            meta.setLore(List.of(
                    ChatColor.GRAY + "Vanished: " + (vanished ? ChatColor.AQUA + "Yes" : ChatColor.RED + "No"),
                    "",
                    ChatColor.YELLOW + "Click to toggle"));
            head.setItemMeta(meta);
        }
        return head;
    }
}
