package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

/** the background provider will be called for every slot in the menu that is not occupied by
 * an {@link IElement}. Returning null for a {@link SlotPos} will clear that slot.
 * Background providers are only used for GUI menus. */
public interface BackgroundProvider extends Tickable {

    IIcon drawAt(SlotPos position, StateObject stateObject, StateObject viewer);

    BackgroundProvider BACKGROUND_DEFAULT = (pos,g,v)->null;

    IIcon GRAY_PANE_IICON = IIcon.of(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.GRAY).add(Keys.DISPLAY_NAME, Text.EMPTY).build());
    BackgroundProvider BACKGROUND_GRAYPANE = (pos,g,v)->GRAY_PANE_IICON;

    @Override
    default boolean tick(int ms) {
        return false;
    }
}
