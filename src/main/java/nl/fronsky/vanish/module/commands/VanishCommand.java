/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.VanishPermissions;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.models.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code /vanish} toggles the sender, {@code /vanish <player>} toggles another player and
 * {@code /vanish <subcommand>} runs one of the registered {@link Subcommand}s.
 */
public final class VanishCommand implements TabExecutor {
    private static final String SUBCOMMAND_PERMISSION_PREFIX = VanishPermissions.COMMAND + ".";

    private final VanishConfig config;
    private final Messages messages;
    private final VanishManager vanishManager;
    private final Map<String, Subcommand> subcommands = new LinkedHashMap<>();

    public VanishCommand(VanishConfig config, Messages messages, VanishManager vanishManager) {
        this.config = config;
        this.messages = messages;
        this.vanishManager = vanishManager;
    }

    public void register(Subcommand subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                toggleSelf(player);
            } else {
                messages.send(sender, Message.NO_PLAYER);
            }
            return true;
        }

        Subcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand != null) {
            if (sender.hasPermission(permissionOf(subcommand))) {
                subcommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                messages.send(sender, Message.NO_PERMISSION);
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(sender, Message.PLAYER_NOT_ONLINE);
        } else {
            toggleOther(sender, target);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> names = subcommands.values().stream()
                    .filter(subcommand -> sender.hasPermission(permissionOf(subcommand)))
                    .map(Subcommand::getName)
                    .toList();
            return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
        }

        Subcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null || !sender.hasPermission(permissionOf(subcommand))) {
            return List.of();
        }
        List<String> candidates = subcommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        return StringUtil.copyPartialMatches(args[args.length - 1], candidates, new ArrayList<>());
    }

    /**
     * Toggles the vanish state of a player on behalf of the sender, who needs the "others" permission.
     */
    public void toggleOther(CommandSender sender, Player target) {
        if (!sender.hasPermission(VanishPermissions.COMMAND_OTHERS)) {
            messages.send(sender, Message.NO_PERMISSION);
            return;
        }
        VanishPlayer vanishPlayer = vanishManager.getPlayer(target);
        boolean vanish = !vanishPlayer.isVanished();
        Message confirmation = vanish ? Message.VANISH_SUCCESS : Message.VISIBLE_SUCCESS;
        messages.send(sender, confirmation, "{player}", target.getDisplayName());
        setVanished(vanishPlayer, vanish);
    }

    private void toggleSelf(Player player) {
        VanishPlayer vanishPlayer = vanishManager.getPlayer(player);
        setVanished(vanishPlayer, !vanishPlayer.isVanished());
    }

    private void setVanished(VanishPlayer vanishPlayer, boolean vanish) {
        if (!vanish) {
            vanishManager.unvanish(vanishPlayer, false);
            return;
        }
        vanishManager.vanish(vanishPlayer, false);
        if (config.isSoundEnabled()) {
            Player player = vanishPlayer.getPlayer();
            player.playSound(player.getLocation(), config.getSound(), 1.0f, 1.0f);
        }
    }

    private static String permissionOf(Subcommand subcommand) {
        return SUBCOMMAND_PERMISSION_PREFIX + subcommand.getName();
    }
}
