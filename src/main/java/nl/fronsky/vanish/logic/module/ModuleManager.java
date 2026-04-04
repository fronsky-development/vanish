/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.logic.module;

import nl.fronsky.vanish.logic.module.interfaces.IModule;
import nl.fronsky.vanish.logic.utils.Result;
import nl.fronsky.vanish.logic.utils.Status;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private final Map<Class<? extends IModule>, Module> modules = new HashMap<>();
    private Status moduleStatus = Status.IDLE;

    /**
     * Prepares a module for use.
     *
     * @param module the module to be prepared
     * @throws NullPointerException if the {@code module} parameter is {@code null}
     */
    public void prepare(Module module) {
        if (!modules.containsKey(module.getClass())) {
            modules.put(module.getClass(), module);
        }
    }

    /**
     * Loads all modules managed by this ModuleManager.
     *
     * @throws IllegalStateException if the ModuleManager is not in an idle state
     * @throws Exception             if any module fails to load
     */
    public void load() throws Exception {
        if (!moduleStatus.equals(Status.IDLE)) {
            throw new IllegalStateException("The modules can't be loaded because the ModuleManager is not idle.");
        }

        moduleStatus = Status.LOADING;
        for (Module module : modules.values()) {
            Result<String> result = module.load();
            if (!result.Success()) {
                throw result.Exception();
            }
        }
        moduleStatus = Status.LOADED;
    }

    /**
     * Enables all modules managed by this ModuleManager.
     *
     * @throws IllegalStateException if the ModuleManager is not in a loaded state
     * @throws Exception             if any module fails to enable
     */
    public void enable() throws Exception {
        if (!moduleStatus.equals(Status.LOADED)) {
            throw new IllegalStateException("The modules can't be enabled because the ModuleManager is not loaded.");
        }

        moduleStatus = Status.ENABLING;
        for (Module module : modules.values()) {
            Result<String> result = module.enable();
            if (!result.Success()) {
                throw result.Exception();
            }
        }
        moduleStatus = Status.ENABLED;
    }

    /**
     * Disables all modules managed by this ModuleManager.
     *
     * @throws IllegalStateException if the ModuleManager is not in an enabled state
     * @throws Exception             if any module fails to disable
     */
    public void disable() throws Exception {
        if (!moduleStatus.equals(Status.ENABLED)) {
            throw new IllegalStateException("The modules can't be disabled because the ModuleManager is not enabled.");
        }

        moduleStatus = Status.DISABLING;
        for (Module module : modules.values()) {
            Result<String> result = module.disable();
            if (!result.Success()) {
                throw result.Exception();
            }
        }
        moduleStatus = Status.DISABLED;
    }
}
