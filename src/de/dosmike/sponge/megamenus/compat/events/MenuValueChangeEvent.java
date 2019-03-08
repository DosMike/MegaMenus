package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class MenuValueChangeEvent<V, T extends IValueChangeable<V> & IElement>  implements Event {

    private T element;
    private IMenu menu;
    private Player viewer;

    private V oldValue, newValue;

    private Cause cause;

    public MenuValueChangeEvent(V oldValue, V newValue, T element, Player viewer, Cause cause) {
        this.viewer = viewer;
        this.menu = element.getParent();
        this.element = element;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public T getElement() {
        return element;
    }

    public IMenu getMenu() {
        return menu;
    }

    public Player getViewer() {
        return viewer;
    }

    public V getOldValue() {
        return oldValue;
    }

    public V getNewValue() {
        return newValue;
    }
}
