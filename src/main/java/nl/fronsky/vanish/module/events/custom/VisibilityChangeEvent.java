/**
 * Copyright (c) by Fronsky.
 */

package nl.fronsky.vanish.module.events.custom;

import lombok.Getter;
import lombok.NonNull;
import nl.fronsky.vanish.module.VanishModule;
import nl.fronsky.vanish.module.enums.State;
import nl.fronsky.vanish.module.models.VanishPlayer;
import nl.fronsky.vanish.module.utils.Data;
import nl.fronsky.vanish.module.utils.MetaData;
import nl.fronsky.vanish.module.utils.ProtocolLib;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class VisibilityChangeEvent extends Event {
    private static final HandlerList handlers;

    static {
        handlers = new HandlerList();
    }

    private final VanishPlayer vanishPlayer;
    private final boolean visible, action;
    private final Data data;
    private final ProtocolLib protocolLib;

    public VisibilityChangeEvent(VanishPlayer vanishPlayer, boolean action) {
        this.vanishPlayer = vanishPlayer;
        this.action = action;
        visible = MetaData.getVanishState(vanishPlayer.getPlayer(), VanishModule.getData()).equals(State.VISIBLE);
        data = VanishModule.getData();
        protocolLib = data.getProtocolLib();
    }

    /**
     * Retrieves the handler list for PlayerVisibilityChangeEvent.
     *
     * @return the handler list for PlayerVisibilityChangeEvent
     */
    public static HandlerList getHandlerList() {
        return VisibilityChangeEvent.handlers;
    }

    /**
     * Checks if ProtocolLib plugin is active.
     *
     * @return {@code true} if ProtocolLib is active, otherwise {@code false}
     */
    public boolean isProtocolLibActive() {
        return this.protocolLib != null;
    }

    /**
     * Retrieves the list of event handlers for PlayerVisibilityChangeEvent.
     *
     * @return the list of event handlers for PlayerVisibilityChangeEvent
     */
    @NonNull
    public HandlerList getHandlers() {
        return VisibilityChangeEvent.handlers;
    }
}
