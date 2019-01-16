package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class HookValueChangeEvent<V, T extends IValueChangeable<V> & IElement>  implements Event {

    T element;
    IMenu menu;
    Player viewer;

    StateObject globalState;
    StateObject viewerState;

    V oldValue, newValue;

    public HookValueChangeEvent(V oldValue, V newValue, T element, Player viewer) {
        this.viewer = viewer;
        this.menu = element.getParent();
        globalState = this.menu.getState();
        viewerState = this.menu.getPlayerState(viewer.getUniqueId());
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public Cause getCause() {
        return Sponge.getCauseStackManager().getCurrentCause();
    }
}
