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
    LOADED("Loaded"),
    ERROR("Error"),
    SUCCESS("Success"),
    FAILURE("Failure"),
    NOT_FOUND("Not Found"),
    INVALID_INPUT("Invalid Input"),
    TIMEOUT("Timeout"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    INTERNAL_ERROR("Internal Error");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    /**
     * Retrieves the numerical ID of this enum constant.
     *
     * @return the numerical ID of this enum constant
     */
    public int getId() {
        return ordinal();
    }
}
