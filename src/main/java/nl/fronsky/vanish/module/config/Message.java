/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.config;

import java.util.Locale;

/**
 * Player-facing messages. Each constant maps to a lower-case key in {@code messages.yml} and carries the text
 * used when that key is missing.
 */
public enum Message {
    NO_PERMISSION("&cYou do not have permissions to perform this action. "
            + "Please contact your system administrator for assistance."),
    PLAYER_JOINED_VANISHED_SILENTLY("&e{player} has joined and vanished silently"),
    PLAYER_QUIT_VANISHED("&e{player} has left the game and is no longer vanished."),
    PLAYER_VANISHED("&e{player} has vanished from the game. Poof!"),
    PLAYER_BECAME_VISIBLE("&e{player} has become visible to other players."),
    VANISH_SUCCESS("&aYou have successfully vanished {player} from the game."),
    VISIBLE_SUCCESS("&aYou have successfully made {player} visible to other players."),
    JOINED_VANISHED("&3You have joined the game in vanished mode. To appear, type /vanish!"),
    YOU_VANISHED("&3You have vanished from the game. Poof!"),
    YOU_BECAME_VISIBLE("&3You are now visible to other players."),
    FAKE_JOIN("&e{player} joined the game."),
    FAKE_QUIT("&e{player} left the game."),
    TELEPORT("&aYou have been teleported to {player}."),
    VANISH_SOUND_ENABLED("&aYou have enabled the sound when vanishing."),
    VANISH_SOUND_DISABLED("&aYou have disabled the sound when vanishing."),
    VANISH_SILENT_ENABLED("&aYou have enabled the silent mode when joining the game."),
    VANISH_SILENT_DISABLED("&aYou have disabled the silent mode when joining the game."),
    PLAYER_NOT_ONLINE("&cThis player is not online."),
    WRONG_ARGS("&cPlease provide the required argument '{arg}' before executing this command."),
    NO_PLAYER("You must be a player to use this command."),
    NO_VALID_COLOR("&cThe given color '{color}' is not a valid color."),
    PLUGIN_COLOR_CHANGED("&aThe plugin color has been changed to {color}."),
    PLUGIN_RELOADED("&aThe Fronsky (r) Vanish plugin has been reloaded successfully!"),
    NOTIFICATION_VANISHED_ACTIONBAR("&3You are now vanished"),
    NOTIFICATION_VISIBLE_ACTIONBAR("&3You are now visible"),
    NOTIFICATION_VANISHED_TITLE("&3Vanished"),
    NOTIFICATION_VISIBLE_TITLE("&3Visible");

    private final String defaultText;

    Message(String defaultText) {
        this.defaultText = defaultText;
    }

    /**
     * @return the key of this message in {@code messages.yml}
     */
    public String getKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * @return the uncolored text used when {@code messages.yml} does not define this message
     */
    public String getDefaultText() {
        return defaultText;
    }
}
