package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.MSlot;
import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Triggered when a player interacts with {@link IInventory} menu elements<br>
 * Copy of method description:<br>
 * Event specifically for tracking changes in {@link MSlot} elements.<br>
 * param <b>change</b> - a wrapper for the SlotTransaction that triggered this event<br>
 * param <b>element</b> - the element that was interacted with<br>
 * param <b>viewer</b> - the player that took or put items from/into this element
 */
@FunctionalInterface
public interface OnSlotChangeListener<X extends IInventory> {

    /**
     * Event specifically for tracking changes in {@link MSlot} elements.<br>
     * @param change a wrapper for the SlotTransaction that triggered this event
     * @param element the element that was interacted with
     * @param viewer the player that took or put items from/into this element
     */
    void onSlotChange(SlotChange change, X element, Player viewer);

}
