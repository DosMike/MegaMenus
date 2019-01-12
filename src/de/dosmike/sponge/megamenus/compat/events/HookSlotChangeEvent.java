package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.MSlot;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class HookSlotChangeEvent implements Event {

    MSlot element;
    IMenu menu;
    Player viewer;

    StateObject globalState;
    StateObject viewerState;

    SlotChange change;

    public HookSlotChangeEvent(SlotChange change, MSlot slot, Player viewer) {
        this.change = change;
        this.element = element;
        this.viewer = viewer;
        this.menu = element.getParent();
        globalState = this.menu.getState();
        viewerState = this.menu.getPlayerState(viewer.getUniqueId());
    }

    @Override
    public Cause getCause() {
        return Sponge.getCauseStackManager().getCurrentCause();
    }
}