package de.dosmike.sponge.megamenus.api;

import de.dosmike.sponge.megamenus.api.listener.OnRenderStateListener;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Set;

/**
 * The Menu Render is responsible for managing viewers of a menu related
 * to a certain display style, like for example Inventory rendering.
 * If a menu is displayed as Inventory, a new GUI renderer is created
 * drawing the elements into a inventory for all currently observing players.
 * Invalidation of elements and menus are usually propagated up to the renderer
 * through the {@link de.dosmike.sponge.megamenus.impl.RenderManager} in order
 * to redraw the menu.
 */
public interface MenuRenderer {

    /**
     * will cause this menu to redraw in the near future
     */
    void invalidate();

    /**
     * this method may be called at any arbitrary moment to notify the renderer to
     * redraw the menu for all viewers if the menu was invalidated
     */
    void revalidate();

    /**
     * @return all players currently observing the menu through this renderer
     */
    Set<Player> getViewers();
    /**
     * @return true if one or more players are viewing the menu through this renderer
     */
    boolean hasViewers();

    /**
     * Closes the current render for the viewer if any is present and
     * opens this render instead. This render has a link back to the
     * previous render and once closed will open the previous menu again.<br>
     * will notify the {@link OnRenderStateListener} if present
     * @param viewer the player to open this renderer to
     */
    void open(Player viewer);
    /**
     * Closes the current render for the viewer if any is present and
     * opens this render instead. If parent is true this render will get
     * a link back to the previous render and once that's closed it will
     * open the previous menu again.<br>
     * will notify the {@link OnRenderStateListener} if present
     * @param viewer the player to open this renderer to
     * @param parent if true, closing this renderer will reopen the renderer that was open prior to this renderer
     */
    void open(Player viewer, boolean parent);
    /**
     * will notify the {@link OnRenderStateListener} if present
     * @param viewer the player to close out of this renderer
     */
    void close(Player viewer);
    /**
     * to be called from inventory events - won't close the actual inventory,
     * but will untrack this player. will notify the {@link OnRenderStateListener} if present
     * @param viewer the player that left/closed this renderer
     */
    void closeSilent(Player viewer);
    /**
     * close this menu for all viewers
     */
    void closeAll();

    /**
     * @return the menu this {@link MenuRenderer} is currently drawing
     */
    IMenu getMenu();

    /** @return true if the renderer was closed by the api and should prevent anti-glitch
     *          triggers if another menu was opened while this menu rendered */
    boolean isClosedByAPI(Player player);

    /**
     * this method shall update all animated IElements within the menu.
     * @param animations is a tracker to prevent double frame advancement for shared anim objects
     */
    void think(AnimationManager animations);

    /**
     * Set the {@link OnRenderStateListener} to this renderer.
     * This can be used to initialize values on the menu.
     * @param listener the new render state listener to use
     */
    void setRenderListener(OnRenderStateListener listener);
    /**
     * Retrieve the {@link OnRenderStateListener} for this renderer.
     * This can be used to initialize values on the menu.
     * @return the current render state listener
     */
    OnRenderStateListener getRenderListener();

}
