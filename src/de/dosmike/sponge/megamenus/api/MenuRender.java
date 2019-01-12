package de.dosmike.sponge.megamenus.api;

import de.dosmike.sponge.megamenus.api.listener.OnRenderStateChangeListener;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Set;

public interface MenuRender {

    /** will cause this menu to redraw in the near future */
    void invalidate();

    /** this method may be called at any arbitrary moment to notify the renderer to
     * redraw the menu for all viewers if the menu was invalidated */
    void revalidate();

    Set<Player> getViewers();
    boolean hasViewers();

    /**
     * will notify the {@link OnRenderStateChangeListener} if present
     */
    void open(Player viewer);
    /**
     * will notify the {@link OnRenderStateChangeListener} if present
     */
    void close(Player viewer);
    /** to be called from inventory events - won't close the actual inventory,
     * but will untrack this player. will notify the {@link OnRenderStateChangeListener} if present */
    void closeSilent(Player viewer);
    /** close this menu for all viewers */
    void closeAll();

    IMenu getMenu();

    /** this method shall update all animated IElements within the menu.
     * @param animations is a tracker to prevent double frame advancement for shared anim objects */
    void think(AnimationManager animations);

    /**
     * Set the {@link OnRenderStateChangeListener} to this renderer.
     * This can be used to initialize values on the menu.
     */
    void setRenderListener(OnRenderStateChangeListener listener);
    /**
     * Retrieve the {@link OnRenderStateChangeListener} for this renderer.
     * This can be used to initialize values on the menu.
     */
    OnRenderStateChangeListener getRenderListener();

}
