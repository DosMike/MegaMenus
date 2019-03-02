package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Identifiable;

import java.util.*;

public interface IElement extends Identifiable {

    /**
     * @return a copy of this element with the same values
     */
    <T extends IElement> T copy();

    /** @returns a unique id for this element within the menu.
     * This id will be updated when copying the element */
    UUID getUniqueId();

    /**
     * if you intend to display this element in a inventory menu this should return
     * a IIcon. These are possibly animated {@link ItemStackSnapshot}s. If null is
     * returned the slot will be rendered empty, this means no interaction is possible.<br>
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     * */
    IIcon getIcon(Player viewer);
    /**
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     * @return the Text to display instead of the Item name, or null, if the icon shall specify
     */
    Text getName(Player viewer);
    /**
     * The state parameters are presented to allow dynamic rendering depending on the global menu state
     * or the players state.
     * The default implementations usually ignore states, but you're free to override.
     * @return the Lore to display instead if the items one, or null, if the icon shall specify
     */
    List<Text> getLore(Player viewer);
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
     * @param viewer the actual player requesting this IElement to render
     * @return all affected slots by this element
     */
    Collection<SlotPos> renderGUI(Player viewer);

    /**
     * decorated a textual representation with the same Hover-Text as a inventory icon, usage is
     * to call super(Text.of("Implementation Specific Representation")); The default implementation
     * will just pass in the Elements ClassName as Text
     * @param viewer the actual player requesting this IElement to render
     */
    default Text renderTUI(Player viewer) {
        IIcon icon = getIcon(viewer);
        List<Text> lore = getLore(viewer);
        Text display = getName(viewer);
        if (lore.isEmpty()) {
            return display;
        } else {
            List<Text> sublore = lore.size()>1 ? lore.subList(1,lore.size()) : Collections.EMPTY_LIST;
            return Text.builder().append(display).onHover(
                    icon != null
                    ? TextActions.showItem(ItemStack.builder().fromSnapshot(icon.render())
                            .add(Keys.DISPLAY_NAME, lore.get(0))
                            .add(Keys.ITEM_LORE, sublore)
                            .build().createSnapshot())
                    : TextActions.showText(Text.of(
                            Text.joinWith(Text.of(Text.NEW_LINE), getLore(viewer))
                    ))
            ).build();
        }
    }

    /**
     * This is an internal method that validates various render related information for this element
     * @throws ObjectBuilderException if an error occurred
     */
    void validateGui(int pageHeight) throws ObjectBuilderException;

    /** Copied from {@link MenuRenderer}::think<br>
     * this method shall update all animated IElements within the menu.
     * @param animations is a tracker to prevent double frame advancement for shared anim objects
     * @return true if an animation progressed during this think tick and the menu needs to redraw.
     */
    boolean think(AnimationManager animations, Player viewer);


    /**
     * A single object that can be registered to implement more complex behaviour.<br>
     * Should get called along with the IIcon animation updated about once a tick.
     */
    void hookThinkTick(Tickable hook);

    /**
     * Invalidates this elements Menu for all Renderer through the RenderManager.
     * This will cause a redraw in the near future (usually next tick).
     */
    default void invalidate() {
        if (getParent() != null)
            RenderManager.getRenderFor(getParent()).forEach(MenuRenderer::invalidate);
    }

    /**
     * Invalidates the Renderer matching this elements menu and viewed by the specified
     * player, resulting in an inventory update for that player in the near future
     * (usually next tick).
     */
    default void invalidate(Player viewer) {
        if (getParent() != null)
            RenderManager.getRenderFor(getParent()).stream()
                    .filter(r->r.getMenu().equals(getParent()) && r.getViewers().contains(viewer))
                    .findFirst().ifPresent(MenuRenderer::invalidate);
    }
}
