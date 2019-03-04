package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import org.spongepowered.api.entity.living.player.Player;

/** This is a MegaMenus API event called, when a Changeable Element was interacted with.<br>
 * Copy of method description:<br>
 * You can get the menu instance and states via the element parameter<br>
 * param <b>oldValue</b> - the previous value for this element<br>
 * param <b>newValue</b> - the value the element now holds<br>
 * param <b>element</b> - the element that was clicked itself<br>
 * param <b>viewer</b> - the player that clicked the element
 */
@FunctionalInterface
public interface OnChangeListener<V> {

    /**
     * You can get the menu instance and states via the element parameter
     * @param oldValue the previous value for this element
     * @param newValue the value the element now holds
     * @param element the element that was clicked itself
     * @param viewer the player that clicked the element
     */
    void onValueChange(V oldValue, V newValue, IValueChangeable<V> element, Player viewer);

}
