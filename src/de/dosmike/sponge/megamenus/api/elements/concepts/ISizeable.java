package de.dosmike.sponge.megamenus.api.elements.concepts;

import org.spongepowered.api.item.inventory.property.SlotPos;

/** for Gui Elements that span more than 1 inventory slot.
 * <b>This is currently unused</b> */
public interface ISizeable extends IElement {

    int getWidth();
    int getHeight();

    /** checks if the specified position is inside this ISizeable
     * @return true if pos is inside this element */
    default boolean containsPosition(SlotPos pos) {
        int maxX = getPosition().getX()+getWidth()-1;
        int maxY = getPosition().getY()+getHeight()-1;
        return pos.getX() >= getPosition().getX() && pos.getX() <= maxX &&
                pos.getY() >= getPosition().getY() && pos.getY() <= maxY;
    }

}
