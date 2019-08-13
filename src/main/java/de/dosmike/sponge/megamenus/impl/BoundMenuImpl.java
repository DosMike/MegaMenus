package de.dosmike.sponge.megamenus.impl;

import com.google.common.collect.ImmutableMap;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.PositionProvider;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.state.StateProperties;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A bound menu implementation allows for menu {@link IElement} to not only render differently
 * for each viewer, but also to store different values for each element by creating functional
 * duplicates. (Every element gets copied).<br>
 * Changes to the {@link BoundMenuImpl} IElements are <b>NOT</b> reflected onto the {@link BaseMenuImpl} and vice versa.<br>
 * The menu {@link StateObject}s are still shared with the BaseMenuImpl.
 * @see IMenu
 */
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

    @Override
    public void setPositionProvider(PositionProvider provider) {
        menu.setPositionProvider(provider);
    }

    //region menu elements
    protected int pagecount = 1;
    protected Map<Integer, List<IElement>> pageelements = new HashMap<>();

    @Override
    public int pages() {
        return pagecount;
    }

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

    protected Map<Integer, SlotPos> lastPutPosition = new HashMap<>();
    /**
     * internal shared implementation to put elements on a page. check must already have happened
     * @param page the target page
     * @param element the elements to put on the page
     */
    private void putOnPage(Integer page, Collection<? extends IElement> element) {
        if (page > pagecount) pagecount = page;
        List<IElement> pe = pageelements.getOrDefault(page, new LinkedList<>());
        for (IElement elem : element)
            if (elem instanceof IElementImpl) {
                //bind the element to this menu
                ((IElementImpl) elem).setParent(this);
                //create a position, if missing
                SlotPos pos = elem.getPosition();
                if (pos == null) {
                    pos = menu.positionProvider.next(lastPutPosition.get(page));
                    elem.setPosition(pos);
                }
                lastPutPosition.put(page, pos);
            }
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
    }
    @Override
    public void removePage(int page) {
        clearPage(page);
        for (int i = page+1; i <= pagecount; i++) { //pull pages down
            List<IElement> pre = pageelements.remove(i);
            if (pre != null) pageelements.put(i-1, pre);
        }
        //adjust paginations
        if (page > 1) {
            if (page == pagecount) {
                pagecount--;
            }
            for (Map.Entry<UUID, StateObject> e : menu.playerBoundStates.entrySet()) {
                Optional<Integer> viewerpage = e.getValue().getInt(StateProperties.PAGE);
                if (viewerpage.isPresent() && viewerpage.get() >= page) {
                    e.getValue().set(StateProperties.PAGE, viewerpage.get() - 1);
                }
            }
        }
        //fix position tracking
        List<Integer> keys = new LinkedList<>(lastPutPosition.keySet());
        keys.removeIf(p->p<=page);
        Collections.sort(keys);
        for (Integer k : keys) {
            //ok, as long as k goes up, since we move elements down
            SlotPos pre = lastPutPosition.remove(k);
            if (pre != null) lastPutPosition.put(k-1, pre);
        }
    }
    @Override
    public void clearPage(int page) {
        Optional.ofNullable(pageelements.remove(page)) //if a page could be removed here
                .ifPresent(list->list.forEach(element->{ //unbind all element in the page from this menu
                    if (element instanceof IElementImpl)
                        ((IElementImpl)element).setParent(null);
                }));
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
     * @param bound not applicable, has to be false
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
     * @param bound not applicable, has to be false
     */
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createBookRenderer(boolean bound) {
        if (bound)
            throw new IllegalArgumentException("Can't create a bound menu instance from a bound menu");

        return new BookRenderer(this);
    }
    /**
     * @param bound not applicable, has to be false
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
     * @return a new {@link BoundMenuImpl} with the same {@link BaseMenuImpl} as
     * source
     */
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
