/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.commands.subcommands;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.commands.Subcommand;
import nl.fronsky.vanish.module.config.Message;
import nl.fronsky.vanish.module.config.Messages;
import nl.fronsky.vanish.module.config.VanishConfig;
import nl.fronsky.vanish.module.gui.MenuListener;
import nl.fronsky.vanish.module.gui.PlayerListMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * {@code /vanish gui} opens the player head menu.
 */
public final class GuiSubcommand implements Subcommand {
    private final VanishConfig config;
    private final Messages messages;
    private final VanishManager vanishManager;
    private final MenuListener menus;
    private final BiConsumer<Player, Player> toggleAction;

    /**
     * @param toggleAction toggles the vanish state of the second player on behalf of the first
     */
    public GuiSubcommand(VanishConfig config, Messages messages, VanishManager vanishManager, MenuListener menus,
                         BiConsumer<Player, Player> toggleAction) {
        this.config = config;
        this.messages = messages;
        this.vanishManager = vanishManager;
        this.menus = menus;
        this.toggleAction = toggleAction;
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            menus.open(player, new PlayerListMenu(menus, config, vanishManager, toggleAction, 1));
        } else {
            messages.send(sender, Message.NO_PLAYER);
        }
    }
}
