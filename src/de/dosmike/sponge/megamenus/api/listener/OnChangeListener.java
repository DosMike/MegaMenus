package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import org.spongepowered.api.entity.living.player.Player;

/** this is a MegaMenus API event called, when a Changeable Element was interacted with. */
@FunctionalInterface
public interface OnChangeListener<V> {

    /** you can get the menu instance and states via the element parameter */
    void onValueChange(V oldValue, V newValue, IValueChangeable<V> element, Player viewer);

}
