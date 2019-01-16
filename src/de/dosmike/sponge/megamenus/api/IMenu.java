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
     * @return et all elements for one page
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
     * completely remove the page other pages
     */
    void removePage(int page);
    //endregion

    //region states
    StateObject getState();
    StateObject getPlayerState(UUID playerID);

    ImmutableMap<UUID, StateObject> getPlayerStateMapCopy();

    void setState(StateObject state);

    void setPlayerState(UUID playerID, StateObject object);

    void importPlayerStateMap(Map<UUID, StateObject> states);

    void clearState();

    void clearPlayerState(UUID playerID);

    void clearPlayerStateMap();
    //endregion

    //region rendering
    /**
     * Create a new {@link GuiRenderer} that will handle events and inventory updates.
     * @param pageHeight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRenderer createGuiRenderer(int pageHeight, boolean bound);

    /**
     * Create a new {@link BookRenderer} that will handle events and book view updates.<br>
     * A book has a limited line-count of 15!
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRenderer createBookRenderer(boolean bound);

    /**
     * Create a new {@link ChatRenderer} that will handle events and book view updates.<br>
     * @param pageHeight is the number of lines per page in the chat pagination including.
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu
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

    IMenu copy();
}
