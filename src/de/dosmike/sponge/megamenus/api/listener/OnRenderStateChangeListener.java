package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRender;
import org.spongepowered.api.entity.living.player.Player;

public interface OnRenderStateChangeListener {

    /** called by a render, when the open method was called */
    void opened(MenuRender render, IMenu menu, Player viewer);
    /** called by a render, when a player closed the menu. #
     * this could be due to actually closing it or delayed in case
     * another menu opened over this menu (will prevent the event
     * from being received) */
    void closed(MenuRender render, IMenu menu, Player viewer);

    /**
     * called by the render manager once all viewers closed the render.
     * if no other plugin hold on the render it should now be GCable.
     */
    void paused(MenuRender render, IMenu menu);

    /**
     * called by the render if menu was opened and there's now one viewer
     * meaning this is the first viewer on the menu.<br>
     * The menu will be registered in the render manager for animation.
     */
    void resumed(MenuRender render, IMenu menu);

}
