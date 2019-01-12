package de.dosmike.sponge.megamenus.api;

import com.google.common.collect.ImmutableMap;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.listener.OnRenderStateChangeListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.GuiRender;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface IMenu extends Identifiable {

    Text getTitle();
    void setTitle(Text title);
    /**
     * @return the {@link BackgroundProvider} for this menu
     */
    BackgroundProvider getBackground();
    /**
     * Sets the {@link BackgroundProvider} for this menu
     */
    void setBackgroundProvider(BackgroundProvider background);

    //region menu elements

    /**
     * returns the amount of pages.
     * this value will automatically be set by the highest page an element was added to.
     */
    int pages();
    /**
     * Get all elements for one page
     */
    Collection<IElement> getPageElements(int page);

    void add(IElement element);
    void addAll(Collection<? extends IElement> elements);
    void add(int page, IElement element);
    void addAll(int page, Collection<? extends IElement> elements);

    void remove(int page, int x, int y);
    void remove(int page, SlotPos pos);
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
     * Create a new {@link GuiRender} that will handle events and inventory updates.
     * @param pageheight is the number of rows per page in the inventory including the
     *                   pagination row.
     * @throws ObjectBuilderException when elements are placed below the displayable area.
     */
    MenuRender createGuiRenderer(int pageheight);
    //endregion

    IMenu copy();
}
