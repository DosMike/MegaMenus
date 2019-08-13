package de.dosmike.sponge.megamenus.api.util;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.impl.GuiRenderer;

@FunctionalInterface
/**
 * Tickable is an interface used by the Animation manager to keep track of animated elements within a frame.
 * This is important in case e.g. IIcons are recycled though multiple IElements and/or IMenus.
 */
public interface Tickable {

    /**
     * Notify this Tickable that a certain time has passed.<br>
     * The {@link IIcon} implementation returns true if the icon changed due to animation
     * and requires the {@link GuiRenderer} to redraw the {@link IMenu}
     * @return true if the time passed caused a change in this objects state
     */
    boolean tick(int ms);

}
