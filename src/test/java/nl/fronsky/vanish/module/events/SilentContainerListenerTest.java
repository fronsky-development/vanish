/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events;

import nl.fronsky.vanish.VanishTestBase;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests run without ProtocolLib, so containers are opened in spectator mode.
 */
class SilentContainerListenerTest extends VanishTestBase {

    @Test
    void vanishedPlayerOpensChestsSilentlyAndGetsTheGameModeBack() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        Block chest = placeNextTo(admin, Material.CHEST);

        PlayerInteractEvent click = rightClick(admin, chest, EquipmentSlot.HAND);

        assertEquals(Event.Result.DENY, click.useInteractedBlock());
        assertEquals(GameMode.SPECTATOR, admin.getGameMode());
        assertEquals(InventoryType.CHEST, admin.getOpenInventory().getTopInventory().getType());

        admin.closeInventory();
        server.getScheduler().performOneTick();
        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
    }

    @Test
    void enderChestOpensWithoutChangingTheGameMode() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        Block enderChest = placeNextTo(admin, Material.ENDER_CHEST);

        rightClick(admin, enderChest, EquipmentSlot.HAND);
        server.getScheduler().performOneTick();

        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
        assertEquals(InventoryType.ENDER_CHEST, admin.getOpenInventory().getTopInventory().getType());
    }

    @Test
    void quittingInsideAChestRestoresTheGameModeBeforeTheDataIsSaved() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        rightClick(admin, placeNextTo(admin, Material.CHEST), EquipmentSlot.HAND);

        admin.disconnect();

        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
    }

    @Test
    void offHandClickDoesNotOpenTheChestAgain() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);

        PlayerInteractEvent click = rightClick(admin, placeNextTo(admin, Material.CHEST), EquipmentSlot.OFF_HAND);

        assertNotEquals(Event.Result.DENY, click.useInteractedBlock());
        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
    }

    @Test
    void chestProtectedByAnotherPluginStaysClosed() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        Block chest = placeNextTo(admin, Material.CHEST);

        PlayerInteractEvent click = new PlayerInteractEvent(admin, Action.RIGHT_CLICK_BLOCK, null, chest,
                BlockFace.UP, EquipmentSlot.HAND);
        click.setUseInteractedBlock(Event.Result.DENY);
        server.getPluginManager().callEvent(click);

        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
        Inventory top = admin.getOpenInventory().getTopInventory();
        assertTrue(top == null || top.getType() != InventoryType.CHEST, "the chest was opened");
    }

    @Test
    void sneakingWithABlockPlacesItInsteadOfOpeningTheChest() {
        TestPlayer admin = joinOperator("Admin");
        vanish(admin);
        admin.setSneaking(true);
        admin.getInventory().setItemInMainHand(new ItemStack(Material.STONE));

        PlayerInteractEvent click = rightClick(admin, placeNextTo(admin, Material.CHEST), EquipmentSlot.HAND);

        assertNotEquals(Event.Result.DENY, click.useInteractedBlock());
        assertEquals(GameMode.SURVIVAL, admin.getGameMode());
    }

    @Test
    void visiblePlayersOpenChestsNormally() {
        TestPlayer user = join("User");

        PlayerInteractEvent click = rightClick(user, placeNextTo(user, Material.CHEST), EquipmentSlot.HAND);

        assertNotEquals(Event.Result.DENY, click.useInteractedBlock());
        assertEquals(GameMode.SURVIVAL, user.getGameMode());
    }

    private Block placeNextTo(TestPlayer player, Material type) {
        Block block = player.getLocation().getBlock().getRelative(BlockFace.EAST);
        block.setType(type);
        return block;
    }

    private PlayerInteractEvent rightClick(TestPlayer player, Block block, EquipmentSlot hand) {
        ItemStack item = hand == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, block,
                BlockFace.UP, hand);
        server.getPluginManager().callEvent(event);
        return event;
    }
}
