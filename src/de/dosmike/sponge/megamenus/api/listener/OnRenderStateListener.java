package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import org.spongepowered.api.entity.living.player.Player;

/**
 * All methods have an empty default body so that implementations don't
 * need to provide all unused methods as well.
 */
public interface OnRenderStateListener {

    /** called by a render, when the open method was called */
    default void opened(MenuRenderer render, IMenu menu, Player viewer){}
    /** called by a render, when a player closed the menu. #
     * this could be due to actually closing it or delayed in case
     * another menu opened over this menu (will prevent the event
     * from being received)
     * @return true if you're scheduling to open any other gui to prevent
     *         a eventual parent menu to interfere with you*/
    default boolean closed(MenuRenderer render, IMenu menu, Player viewer){return false;}

    /**
     * called by the render manager once all viewers closed the render.
     * if no other plugin hold on the render it should now be GCable.
     */
    default void paused(MenuRenderer render, IMenu menu){}

    /**
     * called by the render if menu was opened and there's now one viewer
     * meaning this is the first viewer on the menu.<br>
     * The menu will be registered in the render manager for animation.
     */
    default void resumed(MenuRenderer render, IMenu menu){}

    /**
     * called after all elements in the rendered menu were ticked.
     * @return true if you changed elements on the menu
     */
    default boolean tick(int ms, MenuRenderer render, IMenu menu){ return false; }

}
