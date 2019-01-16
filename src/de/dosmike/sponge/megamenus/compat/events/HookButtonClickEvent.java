package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.MButton;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class HookButtonClickEvent implements Event {

    MButton element;
    IMenu menu;
    Player viewer;

    StateObject globalState;
    StateObject viewerState;

    int button;
    boolean shift;

    public HookButtonClickEvent(MButton element, Player viewer, int button, boolean shift) {
        this.element = element;
        this.viewer = viewer;
        this.menu = element.getParent();
        globalState = this.menu.getState();
        viewerState = this.menu.getPlayerState(viewer.getUniqueId());
        this.button = button;
        this.shift = shift;
    }

    @Override
    public Cause getCause() {
        return Sponge.getCauseStackManager().getCurrentCause();
    }
}
