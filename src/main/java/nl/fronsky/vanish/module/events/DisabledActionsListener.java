/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.module.VanishManager;
import nl.fronsky.vanish.module.config.VanishConfig;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Blocks the interactions that are disabled for vanished players in {@code config.yml}.
 * Every handler checks the cheap vanish lookup before doing any other work.
 */
public final class DisabledActionsListener implements Listener {
    private final VanishConfig config;
    private final VanishManager vanishManager;

    public DisabledActionsListener(VanishConfig config, VanishManager vanishManager) {
        this.config = config;
        this.vanishManager = vanishManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (config.isDamageDisabled() && event.getEntity() instanceof Player player
                && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (config.isHungerDisabled() && event.getEntity() instanceof Player player
                && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (config.isMobTargetDisabled() && event.getTarget() instanceof Player player
                && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!config.isDeathMessagesDisabled() || !vanishManager.isVanished(player)) {
            return;
        }
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);
        if (deathMessage != null && !deathMessage.isEmpty()) {
            player.sendMessage(deathMessage);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPressurePlate(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.PHYSICAL && config.isPressurePlatesDisabled() && block != null
                && Tag.PRESSURE_PLATES.isTagged(block.getType()) && vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
