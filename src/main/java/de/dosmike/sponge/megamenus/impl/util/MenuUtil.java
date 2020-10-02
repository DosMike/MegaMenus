package de.dosmike.sponge.megamenus.impl.util;

import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.ISizeable;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final public class MenuUtil {

    /** Elements in a menu can overlap since the menu does not store or handle positional information other than pages.
     * This method searches on elements on a page that occupy a specific position.#
     * @param menu the menu to search for elements
     * @param page the page to search
     * @param pos the x and y coordinates to search for elements
     * @return all elements that occupy pos on the given page in the menu
     */
    public static Set<IElement> getAllElementsAt(IMenu menu, int page, SlotPos pos) {
        Collection<IElement> pageElements = menu.getPageElements(page);
        return pageElements.stream()
                .filter(e -> ((e instanceof ISizeable) && ((ISizeable)e).containsPosition(pos)) || e.getPosition().equals(pos))
                .collect(Collectors.toSet());
    }

    /** Performs the same lookup as getElementsAtPosition, but only returns the first result in case you know that a
     * position does not contain more than one element at any given time.
     * @param menu the menu to search for elements
     * @param page the page to search
     * @param pos the x and y coordinates to search for elements
     * @return a single element that occupies pos on the given page in the menu, or empty if no element was present
     */
    public static Optional<IElement> getElementAt(IMenu menu, int page, SlotPos pos) {
        Collection<IElement> pageElements = menu.getPageElements(page);
        return pageElements.stream()
                .filter(e -> ((e instanceof ISizeable) && ((ISizeable)e).containsPosition(pos)) || e.getPosition().equals(pos))
                .findFirst();
    }

    /**
     * Closing the menu 1 tick later prevents item duping.<br>
     * In order to close all other views (like books that normally don't have a close-method)
     * a inventory-open is sent prior to closing the inventory.<br>
     * Note: This is not a API method and does not notify, it's to visually close a view on the client.
     * @param player the player to close a open view for
     */
    public static void closeInventory(Player player) {
        Task.builder().delayTicks(1)
                .execute(()->{
                    //open inventory to close book renderers
                    player.openInventory(player.getInventory());
                    player.closeInventory();
                })
                .submit(MegaMenus.getInstance());
    }
    /**
     * Open the menu 1 tick later for consistency.<br>
     * Unlike closeInventory this does not open a inventory before closing, because opening
     * a inventory always overlaps. Technically the close should be obsolete.<br>
     * Note: This is not a API method and does not notify, it's the effective display of an inventory to the client.
     * @param player the player to open a inventory for
     * @param inventory the inventory to display
     */
    public static void openInventory(Player player, Inventory inventory) {
        Task.builder().delayTicks(1)
                .execute(()->{
                    player.closeInventory();
                    player.openInventory(inventory);
                })
                .submit(MegaMenus.getInstance());
    }

    /**
     * looks like inventory properties like slot position etc are not always properly
     * passed down to sub inventories, thus I'll make it work... somehow<br>
     * Retrieves the slot from a SlotPos in the following order:<ul>
     * <li>Query the SlotPos
     * <li>Query the SlotIndex as y*9+x
     * <li>Stream the Slots for the first Slot that contains a correct SlotIndex
     * </ul>
     * @param inventory the inventory to search the slot in
     * @param pos the Grid Position to search
     * @return the first Slot with the matching pos upcast as Inventory as returned by the inventory query
     */
    public static Optional<Inventory> getSlotByAnyMeans(Inventory inventory, SlotPos pos) {
        Inventory test = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(pos));
        if (test.capacity() == 0) {//not found, try index
            int index = pos.getX() + pos.getY() * 9;
            test = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(index)));
            if (test.capacity() == 0) { //not found, classic inventory API
                return StreamSupport.stream(inventory.slots().spliterator(), false).filter(s ->
                        s.getInventoryProperty(SlotIndex.class).filter(i -> i.getValue() != null && i.getValue() == index).isPresent()
                ).findFirst();
            }
        }
        return Optional.of(test.slots().iterator().next());
    }

}
