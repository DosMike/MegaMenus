package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRender;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import de.dosmike.sponge.megamenus.impl.util.LinkedMenuProperty;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Identifiable;

import java.util.*;
import java.util.stream.StreamSupport;

public interface IElement extends Identifiable {

    /**
     * @return a copy of this element with the same values
     */
    <T extends IElement> T copy();

    /** @returns a unique id for this element within the menu.
     * This id will be kept when copying the element to be able to keep track of it in e.g. {@link StateObject}s*/
    UUID getUniqueId();

    /**
     * if you intend to display this element in a inventory menu this should return
     * a IIcon. These are possibly animated {@link ItemStackSnapshot}s. If null is
     * returned the slot will be rendered empty, this means no interaction is possible.<br>
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     * */
    IIcon getIcon(StateObject global, StateObject viewer);
    /**
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     */
    Text getName(StateObject global, StateObject viewer);
    /**
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     */
    List<Text> getLore(StateObject global, StateObject viewer);
    /**
     * This value is only relevant for GUIs. It returns where the element is positioned within
     * the inventory space.
     */
    SlotPos getPosition();
    /**
     * The inventory will always be 9 wide, but the height depends on how the menu was
     * initialized.
     * If this returns null the element will not render. */
    void setPosition(SlotPos position);

    /**
     * This will return the {@link IMenu} the Element was added to
     */
    IMenu getParent();

    /**
     * This field defined whether the items in this slot of the GUI can be taken by a player or
     * placed by one. This returns a logical combination of the GUI_ACCESS_* values.<br>
     * Default value = GUI_ACCESS_NONE
     */
    default int getAccess() {
        return GUI_ACCESS_NONE;
    }

    int GUI_ACCESS_TAKE = 1;
    int GUI_ACCESS_PUT = 2;
    int GUI_ACCESS_NONE = 0;

    /**
     * takes the IICon, adds element specific data and returns it to be rendered in a
     * inventory menu.
     * @param menuState the global {@link StateObject} for this menu
     * @param viewerState the viewer bound {@link StateObject} for this menu
     * @param viewer the actual player requesting this IElement to render
     * @return all affected slots by this element
     */
    default Collection<SlotPos> renderGUI(StateObject menuState, StateObject viewerState, Player viewer) {
        Inventory view = viewer.getOpenInventory().get(); //when is this not present?
        Optional<IMenu> menu = view.getInventoryProperty(LinkedMenuProperty.class).map(AbstractInventoryProperty::getValue);
        if (!menu.isPresent() || !menu.get().equals(getParent())) {
            MegaMenus.w("Menu was closed or changed during render");
            return Collections.emptyList();
        }
        //convert position to index because slots seem to only retain index
        int index = getPosition().getY()*9+getPosition().getX();

        IIcon icon = getIcon(menuState, viewerState);
        if (icon != null) {
//            Inventory slot = view.query(QueryOperationTypes.INVENTORY_PROPERTY.of(getPosition())); // did not work
//            Inventory slot = view.query(getPosition()); // did not work
//            Inventory slot = view.query(QueryOperationTypes.INVENTORY_PROPERTY.of(new SlotIndex(getPosition().getX()+getPosition().getY()*9))); // did not work
            //inventory API working great as always ;D
            Inventory slot = StreamSupport.stream(view.slots().spliterator(), false).filter(s ->
                    s.getInventoryProperty(SlotIndex.class).filter(i -> i.getValue() != null && i.getValue() == index).isPresent()
            ).findFirst().orElse(null);
            if (slot == null || slot.capacity() == 0) {
                MegaMenus.w("No slot matched position %d,%d", getPosition().getX(), getPosition().getY());
            } else {
                slot.set(
                        ItemStack.builder().fromSnapshot(icon.render())
                                .add(Keys.DISPLAY_NAME, getName(menuState, viewerState))
                                .add(Keys.ITEM_LORE, getLore(menuState, viewerState))
                                .build()
                );
            }
        }
        return Collections.singleton(getPosition());
    }

    /**
     * decorated a textual representation with the same Hover-Text as a inventory icon, usage is
     * to call super(Text.of("Implementation Specific Representation")); The default implementation
     * will just pass in the Elements ClassName as Text
     * @param menuState the global {@link StateObject} for this menu
     * @param viewerState the viewer bound {@link StateObject} for this menu
     * @param viewer the actual player requesting this IElement to render
     */
    default Text renderTUI(@NotNull Text visual, StateObject menuState, StateObject viewerState, Player viewer) {
        IIcon icon = getIcon(menuState, viewerState);
        return Text.builder().append(visual).onHover(
                icon != null
                ? TextActions.showItem(ItemStack.builder().fromSnapshot(icon.render())
                        .add(Keys.DISPLAY_NAME, getName(menuState, viewerState))
                        .add(Keys.ITEM_LORE, getLore(menuState, viewerState))
                        .build().createSnapshot())
                : TextActions.showText(Text.of(
                        getName(menuState, viewerState), Text.NEW_LINE, TextColors.GRAY,
                        Text.joinWith(Text.of(Text.NEW_LINE, TextColors.GRAY), getLore(menuState, viewerState))
                        ))
        ).build();
    }

    /**
     * This is an internal method that validates various render related information for this element
     * @throws ObjectBuilderException if an error occurred
     */
    void validateGui(int pageHeight) throws ObjectBuilderException;

    /** Copied from {@link MenuRender}::think<br>
     * this method shall update all animated IElements within the menu.
     * @param animations is a tracker to prevent double frame advancement for shared anim objects
     * @return true if an animation progressed during this think tick and the menu needs to redraw.
     */
    boolean think(AnimationManager animations, StateObject menuState, StateObject viewerState);


    /**
     * A single object that can be registered to implement more complex behaviour.<br>
     * Should get called along with the IIcon animation updated about once a tick.
     */
    void hookThinkTick(Tickable hook);
}
