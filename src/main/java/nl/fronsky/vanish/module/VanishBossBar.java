/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module;

import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * The boss bar shown to vanished players as a reminder.
 */
final class VanishBossBar {
    private final VanishConfig config;
    private final BossBar bossBar;

    VanishBossBar(VanishConfig config) {
        this.config = config;
        this.bossBar = Bukkit.createBossBar(config.getBossBarTitle(), config.getBarColor(), config.getBossBarStyle());
    }

    void show(Player player) {
        if (config.isBossBarEnabled()) {
            bossBar.addPlayer(player);
        }
    }

    void hide(Player player) {
        bossBar.removePlayer(player);
    }

    /**
     * Applies the current configuration, e.g. after a reload or a color change.
     */
    void refresh(Collection<Player> vanishedPlayers) {
        bossBar.setTitle(config.getBossBarTitle());
        bossBar.setColor(config.getBarColor());
        bossBar.setStyle(config.getBossBarStyle());
        if (config.isBossBarEnabled()) {
            vanishedPlayers.forEach(bossBar::addPlayer);
        } else {
            bossBar.removeAll();
        }
    }

    void removeAll() {
        bossBar.removeAll();
    }
}
