package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import org.spongepowered.api.entity.living.player.Player;

@FunctionalInterface
public interface OnClickListener {

    /**
     * you can get the menu instance and states via the element parameter
     * @param element the element that was clicked
     * @param player the player that clicked the element
     * @param left true if this click was performed with the primary mousebutton
     */
    void onClick(IClickable element, Player player, boolean left);

}
