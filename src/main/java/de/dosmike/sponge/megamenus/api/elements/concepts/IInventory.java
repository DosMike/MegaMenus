package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Elements that represent a inventory of some kind an can have a slot change
 */
public interface IInventory<X extends IInventory> {

    /**
     * Replace the current listener for this object.<br>
     * Note: Access control should not be performed by the listener, but instead
     * through setting the proper access value for the {@link IElement}
     * @param listener the new slot change listener
     */
    void setSlotChangeListener(OnSlotChangeListener<X> listener);
    /**
     * @return The currently assigned slot change listener used when firing a slot change, or null if unset
     */
    OnSlotChangeListener<X> getSlotChangeListener();

    /**
     * Called by the renderer to inform the element that the slot has changed.<br>
     * This method has to be implemented by the {@link IElement} implementation.
     * @param viewer the viewer that caused the slot change / is currently viewing this element
     * @param change the simplified {@link SlotChange} object containing more information
     */
    void fireSlotChangeEvent(Player viewer, SlotChange change);

    /** Function to test, whether the specified item can be put into this slot
     * @return true if the item can be put in */
    boolean testAccessPut(ItemStackSnapshot subject);

    /** Function to test, whether the specified item can be taken out of this slot
     * @return true if the item can be taken out */
    boolean testAccessTake(ItemStackSnapshot subject);

    /**
     * @return the mutable {@link ItemStack} held in this slot
     */
    Optional<ItemStack> getItemStack();
    /**
     * replaces the currently displayed {@link ItemStack} with the provided item
     * @param snapshot the new item to display
     */
    void setItemStack(ItemStackSnapshot snapshot);
    /**
     * replaces the currently displayed itemstack with the provided item
     * @param item the new item to display
     */
    void setItemStack(ItemStack item);
    /**
     * replaces the currently displayed itemstack with the provided item
     * @param type the new item to display
     */
    void setItemStack(ItemType type);

}
