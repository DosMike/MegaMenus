package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.spongepowered.api.entity.living.player.Player;

import java.awt.event.MouseEvent;

/**
 * Shared implementation for all textual rendering
 * @see de.dosmike.sponge.megamenus.api.MenuRenderer
 */
public abstract class TextMenuRenderer extends AbstractMenuRenderer {

    @Deprecated
    public TextMenuRenderer(IMenu menu) {
        super(menu);
    }

    /** text menus are not able to listen for clicks on other buttons
     * and can't read if shift was held */
    public void delegateClickEvent(IElement element, Player viewer) {
        if (element instanceof IClickable) {
            ((IClickable)element).fireClickEvent(viewer, MouseEvent.BUTTON1, false);
        }
    }

}
