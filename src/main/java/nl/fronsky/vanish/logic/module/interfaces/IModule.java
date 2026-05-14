/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.module.interfaces;

public interface IModule {
    /**
     * Called when the module is loaded.
     */
    void onLoad();

    /**
     * Called when the module is enabled.
     */
    void onEnable();

    /**
     * Called when the module is disabled.
     */
    void onDisable();
}
