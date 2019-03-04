package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Default interface to be implemented by elements that can have more
 * than once state, such as checkboxes, spinner, etc
 */
public interface IValueChangeable<V> {

    /**
     * Set the listener object that will be informed when the value of this element changes.
     * @param listener the new Listener object
     */
    void setOnChangeListener(OnChangeListener<V> listener);

    /**
     * @return the listener currently set to this element
     */
    OnChangeListener<V> getOnChangeListener();

    /**
     * Called by the Renderer when this element was interacted with.
     * @param viewer the player responsible for the change
     * @param oldValue the value this element had before it was changed
     * @param newValue the value this element now has
     */
    void fireChangeListener(Player viewer, V oldValue, V newValue);

}
