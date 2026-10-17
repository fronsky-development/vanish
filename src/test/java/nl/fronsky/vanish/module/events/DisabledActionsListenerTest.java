/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"deprecation", "removal"})
class DisabledActionsListenerTest extends VanishTestBase {

    @Test
    void vanishedPlayersTakeNoDamageAndDoNotGetHungry() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        EntityDamageEvent damage = new EntityDamageEvent(admin, EntityDamageEvent.DamageCause.FALL, 4.0);
        FoodLevelChangeEvent hunger = new FoodLevelChangeEvent(admin, 10);
        server.getPluginManager().callEvent(damage);
        server.getPluginManager().callEvent(hunger);

        assertTrue(damage.isCancelled());
        assertTrue(hunger.isCancelled());
    }

    @Test
    void visiblePlayersAreNotAffected() {
        TestPlayer user = join("User");

        EntityDamageEvent damage = new EntityDamageEvent(user, EntityDamageEvent.DamageCause.FALL, 4.0);
        FoodLevelChangeEvent hunger = new FoodLevelChangeEvent(user, 10);
        server.getPluginManager().callEvent(damage);
        server.getPluginManager().callEvent(hunger);

        assertFalse(damage.isCancelled());
        assertFalse(hunger.isCancelled());
    }

    @Test
    void mobsDoNotTargetVanishedPlayers() {
        TestPlayer admin = joinOperator("Admin");
        TestPlayer user = join("User");
        vanish(admin);
        Zombie zombie = admin.getWorld().spawn(admin.getLocation(), Zombie.class);

        EntityTargetEvent targetAdmin = new EntityTargetEvent(zombie, admin, TargetReason.CLOSEST_PLAYER);
        EntityTargetEvent targetUser = new EntityTargetEvent(zombie, user, TargetReason.CLOSEST_PLAYER);
        server.getPluginManager().callEvent(targetAdmin);
        server.getPluginManager().callEvent(targetUser);

        assertTrue(targetAdmin.isCancelled());
        assertFalse(targetUser.isCancelled());
    }

    @Test
    void deathMessageOfAVanishedPlayerIsOnlyShownToThemselves() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        clearMessages(admin);

        admin.setHealth(0);

        server.getPluginManager().assertEventFired(PlayerDeathEvent.class, death -> death.getDeathMessage() == null);
        assertEquals("Admin got killed", admin.nextMessage());
    }

    @Test
    void vanishedPlayersDoNotTriggerPressurePlates() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        Block plate = admin.getLocation().getBlock();
        plate.setType(Material.STONE_PRESSURE_PLATE);

        PlayerInteractEvent step = new PlayerInteractEvent(admin, Action.PHYSICAL, null, plate, BlockFace.SELF);
        server.getPluginManager().callEvent(step);

        assertEquals(Event.Result.DENY, step.useInteractedBlock());
    }
}
