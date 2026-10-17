/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module;

/**
 * Permission nodes checked by the plugin. Descriptions and defaults are declared in {@code plugin.yml}.
 */
public final class VanishPermissions {
    public static final String ALL = "vanish.*";
    public static final String SEE = "vanish.see";
    public static final String AUTO_VANISH = "vanish.join";
    public static final String COMMAND = "vanish.cmd.vanish";
    public static final String COMMAND_OTHERS = COMMAND + ".others";

    private VanishPermissions() {
    }
}
