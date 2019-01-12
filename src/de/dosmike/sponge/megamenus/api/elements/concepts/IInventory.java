package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;

/**
 * Elements that represent a inventory of some kind an can have a slot change
 */
public interface IInventory {

    void setSlotChangeListener(OnSlotChangeListener listener);
    OnSlotChangeListener getSlotChangeListener();

}
