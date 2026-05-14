/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.subcommands.vanish;

import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.module.utils.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Gui {

    private Gui() {
    }

    public static void execute(CommandSender sender, ChatColor color, Data data) {
        if (!(sender instanceof Player player)) {
            Logger.info(Language.NO_PLAYER.getPlainMessage());
            return;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        Inventory inventory = GuiBuilder.createPlayerHeadsInventory(player, onlinePlayers, 45, 1, color, data);
        player.openInventory(inventory);
        player.setMetadata("fronsky_vanish_player_heads_inventory", new FixedMetadataValue(data.getPlugin(), true));
    }
}
