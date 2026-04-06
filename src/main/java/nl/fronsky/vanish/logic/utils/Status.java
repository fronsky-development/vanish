/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.utils;

import lombok.Getter;

@Getter
public enum Status {
    IDLE("Idle"),
    ENABLING("Enabling"),
    ENABLED("Enabled"),
    DISABLING("Disabling"),
    DISABLED("Disabled"),
    LOADING("Loading"),
    LOADED("Loaded");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
