package de.dosmike.sponge.megamenus.impl;

import com.google.common.collect.ImmutableMap;
import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.stream.Collectors;

/** a bound menu implementation allows for menu {@link IElement} to not only render differently
 * for each viewer, but also to store different values for each element by creating functional
 * duplicates. (Every element gets copied).<br>
 * Changes to the {@link BoundMenuImpl} IElements are <b>NOT</b> reflected onto the {@link BaseMenuImpl} and vice versa.<br>
 * The menu {@link StateObject}s are still shared with the BaseMenuImpl. */
public class BoundMenuImpl implements IMenu {

    private BaseMenuImpl menu;
    public BoundMenuImpl(BaseMenuImpl baseMenu) {
        this.menu = baseMenu;
        pagecount = menu.pagecount;
        menu.pageelements.forEach((key, value) ->
                pageelements.put(key, value.stream()
                        .map(o -> {
                            IElement copy = o.copy();
                            // re-bind the copied element to this menu
                            if (copy instanceof IElementImpl) {
                                ((IElementImpl) copy).setParent(null);
                                ((IElementImpl) copy).setParent(this);
                            }
                            return copy;
                        })
                        .collect(Collectors.toCollection(LinkedList::new))
                )
        );
    }

    @Override
    public Text getTitle() {
        return menu.getTitle();
    }

    @Override
    public void setTitle(Text title) {
        menu.setTitle(title);
    }

    @Override
    public BackgroundProvider getBackground() {
        return menu.getBackground();
    }

    @Override
    public void setBackgroundProvider(BackgroundProvider background) {
        menu.setBackgroundProvider(background);
    }

    //region menu elements
    protected int pagecount = 1;
    protected Map<Integer, List<IElement>> pageelements = new HashMap<>();

    /**
     * returns the amount of pages.
     * this value will automatically be set by the highest page an element was added to.
     */
    @Override
    public int pages() {
        return pagecount;
    }
    /**
     * Get all elements for one page
     */
    @Override
    public Collection<IElement> getPageElements(int page) {
        return pageelements.getOrDefault(page, new LinkedList<>());
    }

    @Override
    public void add(IElement element) {
        this.putOnPage(1, Collections.singleton(element));
    }
    @Override
    public void addAll(Collection<? extends IElement> elements) {
        this.putOnPage(1, elements);
    }
    @Override
    public void add(int page, IElement element) {
        if (page < 1) throw new IllegalArgumentException("Page can't be smaller 1");
        this.putOnPage(page, Collections.singleton(element));
    }
    @Override
    public void addAll(int page, Collection<? extends IElement> elements) {
        if (page < 1) throw new IllegalArgumentException("Page can't be smaller 1");
        this.putOnPage(page, elements);
    }

    private void putOnPage(Integer page, Collection<? extends IElement> element) {
        if (page > pagecount) pagecount = page;
        List<IElement> pe = pageelements.getOrDefault(page, new LinkedList<>());
        for (IElement elem : element)
            if (elem instanceof IElementImpl) //bind the element to this menu
                ((IElementImpl)elem).setParent(this);
        pe.addAll(element);
        pageelements.put(page, pe);
    }

    @Override
    public void remove(int page, int x, int y) {
        remove(page, new SlotPos(x,y));
    }
    @Override
    public void remove(int page, SlotPos pos) {
        List<IElement> pe = pageelements.getOrDefault(page, new LinkedList<>());
        pe.removeIf(element-> {
            if (pos.equals(element.getPosition())) {
                if (element instanceof IElementImpl)
                    ((IElementImpl)element).setParent(null); //unbind the element from this menu
                return true;
            } else return false;
        });
        pageelements.put(page, pe);

        //remove empty pages
        int lastpage = pagecount;
        while (pageelements.getOrDefault(lastpage, new LinkedList<>()).isEmpty() && lastpage > 1) lastpage--;
        if (lastpage < pagecount)
            for (int d = lastpage+1; d<=pagecount; d++)
                pageelements.remove(d);
    }
    @Override
    public void removePage(int page) {
        Optional.ofNullable(pageelements.remove(page)) //if a page could be removed here
                .ifPresent(list->list.forEach(element->{ //unbind all element in the page from this menu
                    if (element instanceof IElementImpl)
                        ((IElementImpl)element).setParent(null);
                }));
        for (int i = page+1; i <= pagecount; i++) {
            pageelements.put(i-1, pageelements.get(i));
        }
    }
    //endregion

    //region states
    @Override
    public StateObject getState() {
        return menu.getState();
    }

    @Override
    public StateObject getPlayerState(UUID playerID) {
        return menu.getPlayerState(playerID);
    }

    @Override
    public ImmutableMap<UUID, StateObject> getPlayerStateMapCopy() {
        return menu.getPlayerStateMapCopy();
    }

    @Override
    public void setState(StateObject state) {
        menu.setState(state);
    }

    @Override
    public void setPlayerState(UUID playerID, StateObject object) {
        menu.setPlayerState(playerID, object);
    }

    @Override
    public void importPlayerStateMap(Map<UUID, StateObject> states) {
        menu.importPlayerStateMap(states);
    }

    @Override
    public void clearState() {
        menu.clearState();
    }

    @Override
    public void clearPlayerState(UUID playerID) {
        menu.clearPlayerState(playerID);
    }

    @Override
    public void clearPlayerStateMap() {
        menu.clearPlayerStateMap();
    }
    //endregion

    //region rendering
    /**
     * Create a new {@link GuiRenderer} that will handle events and inventory updates.
     * @param pageheight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @param bound not applicable, has to be false
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createGuiRenderer(int pageheight, boolean bound) {
        if (bound)
            throw new IllegalArgumentException("Can't create a bound menu instance from a bound menu");
        if (pageheight < 1 || pageheight > 6)
            throw new ObjectBuilderException("A Gui Menu requires 1 to 6 rows");

        return new GuiRenderer(this, pageheight);
    }
    /**
     * Create a new {@link BookRenderer} that will handle events and view updates.<br>
     * A book has a fixed page height of 15 lines!
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createBookRenderer(boolean bound) {
        if (bound)
            throw new IllegalArgumentException("Can't create a bound menu instance from a bound menu");

        return new BookRenderer(this);
    }
    /**
     * Create a new {@link GuiRenderer} that will handle events and inventory updates.
     * @param pageHeight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @param bound if true this will create a copy of all elements to draw a non-shared,
     *              player-bound instance of this menu
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createChatRenderer(int pageHeight, boolean bound) {
        if (bound)
            throw new IllegalArgumentException("Can't create a bound menu instance from a bound menu");

        if (pageHeight < 1)
            throw new ObjectBuilderException("A Chat pagination can't have less than 1 height");

        return new ChatRenderer(this, pageHeight);
    }
    //endregion

    /**
     * creates a new {@link BoundMenuImpl} with the same {@link BaseMenuImpl} as
     * source */
    @Override
    public IMenu copy() {
        return new BoundMenuImpl(menu);
    }

    /**
     * @return the ui id for this {@link BoundMenuImpl} - not for the base menu
     */
    @Override
    public UUID getUniqueId() {
        return null;
    }

    /**
     * @return the {@link BaseMenuImpl} this {@link BoundMenuImpl} was created with
     */
    public BaseMenuImpl getBaseMenu() {
        return menu;
    }

}
