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

/**
 * This menu implementation shares values and elements between every renderer and viewer accessing it
 */
public class BaseMenuImpl implements IMenu {

    protected Text title = Text.EMPTY;
    protected BackgroundProvider bg = BackgroundProvider.BACKGROUND_DEFAULT;
    protected PositionProvider positionProvider = PositionProvider.DEFAULT_ROWS;
    protected UUID uuid = UUID.randomUUID();

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Text getTitle() {
        return title;
    }
    @Override
    public void setTitle(Text title) {
        this.title = title;
    }

    @Override
    public BackgroundProvider getBackground() {
        return bg;
    }

    @Override
    public void setBackgroundProvider(BackgroundProvider background) {
        bg = background;
    }

    @Override
    public void setPositionProvider(PositionProvider provider) {
        lastPutPosition.clear();
        this.positionProvider = provider;
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
                    pos = positionProvider.next(lastPutPosition.get(page));
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
            for (Map.Entry<UUID, StateObject> e : playerBoundStates.entrySet()) {
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
    protected StateObject state = new StateObject();
    protected Map<UUID, StateObject> playerBoundStates = new HashMap<>();

    @Override
    public StateObject getState() {
        return state;
    }
    @Override
    public StateObject getPlayerState(UUID playerID) {
        if (!playerBoundStates.containsKey(playerID))
            playerBoundStates.put(playerID, new StateObject());
        return playerBoundStates.get(playerID);
    }
    @Override
    public ImmutableMap<UUID, StateObject> getPlayerStateMapCopy() {
        ImmutableMap.Builder<UUID, StateObject> b = ImmutableMap.builder();
        playerBoundStates.forEach((k,v)-> b.put(k,v.copy()));
        return ImmutableMap.copyOf(b.build());
    }
    @Override
    public void setState(StateObject state) {
        this.state = state;
    }
    @Override
    public void setPlayerState(UUID playerID, StateObject object) {
        playerBoundStates.put(playerID, object);
    }
    @Override
    public void importPlayerStateMap(Map<UUID, StateObject> states) {
        playerBoundStates.putAll(states);
    }
    @Override
    public void clearState() {
        state.clear();
    }
    @Override
    public void clearPlayerState(UUID playerID) {
        playerBoundStates.remove(playerID);
    }
    @Override
    public void clearPlayerStateMap() {
        playerBoundStates.clear();
    }
    //endregion

    //region rendering
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createGuiRenderer(int pageHeight, boolean bound) {
        if (bound) {
            return new BoundMenuImpl(this).createGuiRenderer(pageHeight, false);
        }
        if (pageHeight < 1 || pageHeight > 6)
            throw new ObjectBuilderException("A Gui Menu requires 1 to 6 rows");

        return new GuiRenderer(this, pageHeight);
    }
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createBookRenderer(boolean bound) {
        if (bound) {
            return new BoundMenuImpl(this).createBookRenderer(false);
        }

        return new BookRenderer(this);
    }
    @SuppressWarnings("deprecation")
    @Override
    public MenuRenderer createChatRenderer(int pageHeight, boolean bound) {
        if (bound) {
            return new BoundMenuImpl(this).createChatRenderer(pageHeight, false);
        }
        if (pageHeight < 1)
            throw new ObjectBuilderException("A Chat pagination can't have less than 1 height");

        return new ChatRenderer(this, pageHeight);
    }
    //endregion

    @Override
    public BaseMenuImpl copy() {
        BaseMenuImpl copy = new BaseMenuImpl();
        copy.setTitle(getTitle());
        copy.setState(state.copy());
        copy.playerBoundStates.putAll(getPlayerStateMapCopy());
        for (int p=1;p<=pagecount;p++)
            for (IElement e : getPageElements(p))
                copy.add(p,e.copy());
        return copy;
    }
}
