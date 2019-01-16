package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Elements that represent a inventory of some kind an can have a slot change
 */
public interface IInventory {

    void setSlotChangeListener(OnSlotChangeListener listener);
    OnSlotChangeListener getSlotChangeListener();

    void fireSlotChangeEvent(Player viewer, SlotChange change);

}
