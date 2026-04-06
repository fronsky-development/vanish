/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import nl.fronsky.vanish.module.VanishModule;

public enum Language {
    DEFAULT("Please choose a language message. The current message is a default message."),
    NO_PERMISSION("&cYou do not have permissions to perform this action. Please contact your system administrator for assistance."),
    PROTOCOLLIB_NOT_FOUND("ProtocolLib not found. Some features will not function without it"),
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
    PLUGIN_RELOADED("&aThe Fronsky (r) Vanish plugin has been reloaded successfully!"),
    PLAYER_NOT_FOUND("We were unable to find a player in the server based on the given data."),
    MESSAGE_NOT_VALID("The message sent to {player} was not valid."),
    ;

    private final String message;

    Language(String message) {
        this.message = message;
    }

    /**
     * Retrieves the message associated with this enum constant.
     *
     * @return the message associated with this enum constant
     */
    public String getMessage() {
        try {
            if (VanishModule.getData() == null || VanishModule.getData().getMessages() == null
                    || VanishModule.getData().getMessages().get() == null) {
                return message;
            }
            String configMessage = VanishModule.getData().getMessages().get().getString(name().toLowerCase());
            if (configMessage == null) {
                configMessage = message;
            }
            return configMessage;
        } catch (Exception e) {
            return message;
        }
    }

    /**
     * Retrieves the message associated with this enum constant, with color formatting applied.
     *
     * @return the message associated with this enum constant with color formatting applied
     */
    public String getMessageWithColor() {
        return ColorUtil.colorize(getMessage());
    }

    /**
     * Retrieves a Language enum constant based on its name.
     *
     * @param name the name of the Language enum constant to retrieve
     * @return the Language enum constant corresponding to the specified name,
     * or Language.DEFAULT if no matching enum constant is found
     */
    public static Language getLanguage(String name) {
        Language language = null;
        for (Language obj : Language.values()) {
            if (obj.name().equalsIgnoreCase(name)) {
                language = obj;
                break;
            }
        }
        if (language == null) language = Language.DEFAULT;
        return language;
    }
}
