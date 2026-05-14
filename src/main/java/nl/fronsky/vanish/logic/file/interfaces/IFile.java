/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.file.interfaces;

public interface IFile<T> {
    /**
     * Loads the necessary data or resources.
     *
     * @return {@code true} if the loading process was successful; {@code false} otherwise
     */
    boolean load();

    /**
     * Saves any relevant data or state.
     */
    void save();

    /**
     * Reloads the configuration or resources.
     */
    void reload();

    /**
     * Retrieves the value of type T.
     *
     * @return the value of type T
     */
    T get();
}
