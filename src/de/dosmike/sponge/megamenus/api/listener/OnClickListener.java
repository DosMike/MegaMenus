package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import org.spongepowered.api.entity.living.player.Player;

@FunctionalInterface
public interface OnClickListener {

    /** you can get the menu instance and states via the element parameter */
    void onClick(IClickable element, Player player);

}
