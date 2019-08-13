package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

/**
 * The background provider will be called for every slot in the menu that is not occupied by
 * an {@link IElement}. Returning null for a {@link SlotPos} will clear that slot.
 * Background providers are only used for GUI menus.
 */
public interface BackgroundProvider extends Tickable {

    /** A positional render method, called on every slot, that was not filled with
     * any other element in any way.<br>
     * Since the background provider has no {@link IMenu} parent field the state objects
     * for the menu and viewer are passed.
     * @param position the SlotPos to fill within the menu
     * @param stateObject the global state for this menu
     * @param viewer the viewers state for this menu
     * @return the icon that will be drawn at the specified position
     */
    IIcon drawAt(SlotPos position, StateObject stateObject, StateObject viewer);

    /**
     * A standard Background Provider that does not fill empty slots
     */
    BackgroundProvider BACKGROUND_DEFAULT = (pos,g,v)->null;

    /**
     * The icon returned by {@literal BACKGROUND_GRAYPANE}
     */
    IIcon GRAY_PANE_IICON = IIcon.of(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.GRAY).add(Keys.DISPLAY_NAME, Text.EMPTY).build());
    /**
     * A standard Background Provider that fills empty slots with gray stained glass panes
     */
    BackgroundProvider BACKGROUND_GRAYPANE = (pos,g,v)->GRAY_PANE_IICON;

    @Override
    default boolean tick(int ms) {
        return false;
    }
}
