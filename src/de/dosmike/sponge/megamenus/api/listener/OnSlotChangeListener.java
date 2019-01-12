package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.entity.living.player.Player;

@FunctionalInterface
public interface OnSlotChangeListener {

    void onSlotChange(SlotChange change, IElement element, Player viewer);

}
