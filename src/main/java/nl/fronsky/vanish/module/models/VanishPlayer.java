/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.models;

import lombok.Getter;
import lombok.Setter;
import nl.fronsky.vanish.logic.logging.Logger;
import nl.fronsky.vanish.logic.utils.ColorUtil;
import nl.fronsky.vanish.logic.utils.Language;
import nl.fronsky.vanish.logic.utils.Result;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.events.custom.VisibilityChangeEvent;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.Dynmap;
import nl.fronsky.vanish.module.utils.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;
import java.util.UUID;

public class VanishPlayer {
    private static final String METADATA_KEY = "fronsky_vanish";
    private final Data data;
    @Getter
    private final Player player;
    @Getter
    private final UUID uuid;
    @Getter
    private final String name, displayName;
    @Getter
    @Setter
    private State state;

    public VanishPlayer(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
        name = player.getName();
        displayName = player.getDisplayName();
        data = VanishModule.getData();
        instantiatePlayerData();
        state = MetaData.getVanishState(player, data);
        if (state.equals(State.VISIBLE)) {
            player.removeMetadata(METADATA_KEY, data.getPlugin());
        } else {
            player.setMetadata(METADATA_KEY, new FixedMetadataValue(data.getPlugin(), true));
        }
    }

    /**
     * Retrieves a VanishPlayer object for the player with the specified UUID.
     *
     * @param uuid the UUID of the player to retrieve
     * @return a Result object containing either a VanishPlayer object or an exception
     */
    public static Result<VanishPlayer> getPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return Result.fail(new Exception(Language.PLAYER_NOT_FOUND.getMessage()));
        }
        return Result.ok(new VanishPlayer(player));
    }

    /**
     * Retrieves a VanishPlayer object for the player with the specified name.
     *
     * @param name the name of the player to retrieve
     * @return a Result object containing either a VanishPlayer object or an exception
     */
    public static Result<VanishPlayer> getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null) {
            return Result.fail(new Exception(Language.PLAYER_NOT_FOUND.getMessage()));
        }
        return Result.ok(new VanishPlayer(player));
    }

    /**
     * Sends a message to the player.
     *
     * @param message the message to send to the player
     */
    public void sendMessage(String message) {
        if (message.isEmpty()) {
            Logger.severe(Language.MESSAGE_NOT_VALID.getMessage().replace("{player}", displayName));
            return;
        }
        player.sendMessage(ColorUtil.colorize(message));
    }

    /**
     * Checks if the player has a specific permission.
     *
     * @param permission the permission to check
     * @return {@code true} if the player has the permission, otherwise {@code false}
     */
    public boolean hasPermission(String permission) {
        return player.hasPermission("vanish.*") || player.hasPermission(permission);
    }

    /**
     * Hides the player and updates their state to hidden.
     *
     * @param join {@code true} if the player is joining and should be hidden, otherwise {@code false}
     */
    public void hide(boolean join) {
        state = State.HIDDEN;
        player.setMetadata(METADATA_KEY, new FixedMetadataValue(data.getPlugin(), true));
        player.setCollidable(!data.getConfig().get().getBoolean("disabled-actions.player-push"));
        player.setCanPickupItems(!data.getConfig().get().getBoolean("disabled-actions.pickup-items"));
        data.getVanishedPlayers().put(uuid, this);
        data.getVanishedBossBar().addPlayer(player);
        Dynmap.hide(player);
        MetaData.invalidateCache(uuid);
        Logger.debug("Player " + name + " is now hidden");
        Bukkit.getServer().getPluginManager().callEvent(new VisibilityChangeEvent(this, join));
    }

    /**
     * Shows the player and updates their state to visible.
     *
     * @param quit {@code true} if the player is quitting and should be shown, otherwise {@code false}
     */
    public void show(boolean quit) {
        state = State.VISIBLE;
        player.removeMetadata(METADATA_KEY, data.getPlugin());
        player.setCollidable(true);
        player.setCanPickupItems(true);
        data.getVanishedPlayers().remove(uuid);
        data.getVanishedBossBar().removePlayer(player);
        Dynmap.show(player);
        MetaData.invalidateCache(uuid);
        Logger.debug("Player " + name + " is now visible");
        Bukkit.getServer().getPluginManager().callEvent(new VisibilityChangeEvent(this, quit));
    }

    /**
     * Initializes the player's data if it does not exist.
     */
    private void instantiatePlayerData() {
        Objects.requireNonNull(data, "Data must not be null when instantiating player data");
        if (!data.getPlayers().get().contains(uuid.toString()) && !data.getPlayers().get().contains(uuid + ".silent")) {
            data.getPlayers().get().set(uuid + ".silent", true);
            data.getPlayers().save();
            data.getPlayers().reload();
        }
    }
}
