package de.dosmike.sponge.megamenus.api;

import com.google.common.collect.ImmutableMap;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.BookRenderer;
import de.dosmike.sponge.megamenus.impl.ChatRenderer;
import de.dosmike.sponge.megamenus.impl.GuiRenderer;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * The menu is a collection of {@link IElement}s that are bound to the menu and spread
 * across pages. In addition to elements the menu also contains {@link StateObject}s for
 * the menu itself and each player that might interact with the menu.<br>
 * The rendering mechanism is not important to the menu itself, it only provides the
 * structure and functionality for the menu.<br>
 * Copying a menu will copy every element and state, while creating a bound instance will
 * only copy the elements and still reference the base menus states.
 */
public interface IMenu extends Identifiable {

    /** @return the current title for this menu */
    Text getTitle();
    /** Set a new title for this menu.<br>
     * <b>Note:</b> The title will not life update in the renderer
     * @param title the new title text
     */
    void setTitle(Text title);
    /**
     * Every non-occupied slot in a {@link GuiRenderer} can be populated
     * by a {@link BackgroundProvider}
     * @return the {@link BackgroundProvider} for this menu
     */
    BackgroundProvider getBackground();
    /**
     * Sets the {@link BackgroundProvider} for this menu.
     * The background should update once the {@link GuiRenderer} revalidates the menu.
     * @param background a {@link BackgroundProvider} implementation providing {@link IIcon}s for empty slots
     */
    void setBackgroundProvider(BackgroundProvider background);

    //region menu elements

    /**
     * This value will automatically be set by the highest page an element was added to.
     * @return the amount of pages.
     */
    int pages();
    /**
     * @return get all elements for one page
     */
    Collection<IElement> getPageElements(int page);

    /**
     * Add an {@link IElement} to page one of this menu
     * @param element an IElement implementation
     */
    void add(IElement element);
    /**
     * Add all {@link IElement}s the first page of this menu
     * @param elements a collection of IElement implementation
     */
    void addAll(Collection<? extends IElement> elements);
    /**
     * Add an {@link IElement} to the specified page<br>
     * Adding alements to a page beyond the first (page 1) will
     * automatically enable pagination.
     * @param page the page to add the element to (1 based)
     * @param element the IElement implemetation to add
     */
    void add(int page, IElement element);
    /**
     * Add all {@link IElement}s to the specified page.<br>
     * Adding alements to a page beyond the first (page 1) will
     * automatically enable pagination.
     * @param page the page to add the element to (1 based)
     * @param elements the collection of IElement implementation to add
     */
    void addAll(int page, Collection<? extends IElement> elements);

    /**
     * remove an element for the page and slot position if present
     * @param page the page to remove elements from (1 based)
     * @param x the column in the gui to remove the element from (0 based)
     * @param y the row in the gui to remove the element from (0 based)
     */
    void remove(int page, int x, int y);
    /**
     * remove an element for the page and slot position if present
     * @param page the page to remove elements from (1 based)
     * @param pos the 2 dimensional position of the element on the gui page
     */
    void remove(int page, SlotPos pos);
    /**
     * completely remove the page. other pages where the page number &gt;
     * page will move up with their page being reduced by 1.<br>
     * this allows clearing all pages with <pre>while (pages()&gt;0) removePage(1);</pre>
     * @param page the page to remove
     */
    void removePage(int page);
    //endregion

    //region states
    /**
     * Menu states are copied into copies and children, meaning base values are
     * present in copies, but changes within the states of copies are not
     * reflected in the base state
     * @return the global State object for this menu
     */
    StateObject getState();
    /**
     * Menu states are copied into copies and children, meaning base values are
     * present in copies, but changes within the states of copies are not
     * reflected in the base state
     * @return the player specific State object for this menu
     */
    StateObject getPlayerState(UUID playerID);
    /**
     * Convenience method for getPlayerState(Player::getUniqueId())
     * Menu states are copied into copies and children, meaning base values are
     * present in copies, but changes within the states of copies are not
     * reflected in the base state
     * @return the player specific State object for this menu
     */
    default StateObject getPlayerState(Player player) {
        return getPlayerState(player.getUniqueId());
    }

    /**
     * @return a immutable copy of the player states mapped to player ids
     */
    ImmutableMap<UUID, StateObject> getPlayerStateMapCopy();

    /**
     * replace the global state object for this menu
     * @param state the new state object
     */
    void setState(StateObject state);
    /**
     * replace a players state object for this menu
     * @param playerID the player to replace the state for
     * @param object the new state object for the player
     */
    void setPlayerState(UUID playerID, StateObject object);

    /**
     * Batch-replaces all given player state objects
     * @param states a map with all player state objects to replace
     */
    void importPlayerStateMap(Map<UUID, StateObject> states);

    /**
     * Remove all entries from the current global state object
     */
    void clearState();

    /**
     * Remove all entries from the current players state object
     * @param playerID the player to remove all entries for
     */
    void clearPlayerState(UUID playerID);

    /**
     * Remove all entries from all player state objects
     */
    void clearPlayerStateMap();
    //endregion

    //region rendering
    /**
     * Create a new {@link GuiRenderer} that will handle events and inventory updates.
     * @param pageHeight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu. If you create a menu dynamically for
     *              a player it's better to not create a bound renderer.
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRenderer createGuiRenderer(int pageHeight, boolean bound);

    /**
     * Create a new {@link BookRenderer} that will handle events and view updates.<br>
     * A book has a fixed page height of 15 lines!
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu. If you create a menu dynamically for
     *              a player it's better to not create a bound renderer.
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRenderer createBookRenderer(boolean bound);

    /**
     * Create a new {@link GuiRenderer} that will handle events and inventory updates.
     * @param pageHeight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu. If you create a menu dynamically for
     *              a player it's better to not create a bound renderer.
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRenderer createChatRenderer(int pageHeight, boolean bound);

    /**
     * Fetches all renderer for this menu from RenderManager and invalidates them.
     * This will cause them to redraw in the near future (usually next tick).
     */
    default void invalidate() {
        RenderManager.getRenderFor(this).forEach(MenuRenderer::invalidate);
    }
    /**
     * Invalidates this menu for the specified player in the currently opened MenuRenderer
     * through the RenderManager. Will cause the menu to redraw in the near future
     * (usually next tick).
     */
    default void invalidate(Player player) {
        RenderManager.getRenderFor(this).stream()
                .filter(r->r.getViewers().contains(player))
                .findFirst().ifPresent(MenuRenderer::invalidate);
    }
    //endregion

    /**
     * Creates a copy of this menu, copying all states, elements and properties into a new menu.<br>
     * All {@link IElement} implementations will usually change IDs after copying.
     * @return a copy of this menu with equal elements, states and title
     */
    IMenu copy();
}
