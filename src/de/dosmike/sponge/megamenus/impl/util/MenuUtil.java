package de.dosmike.sponge.megamenus.impl.util;

import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.ISizeable;
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

    public static Set<IElement> getElementsAtPosition(IMenu menu, int page, SlotPos pos) {
        Collection<IElement> pageElements = menu.getPageElements(page);
        return pageElements.stream()
                .filter(e -> ((e instanceof ISizeable) && ((ISizeable)e).containsPosition(pos)) || e.getPosition().equals(pos))
                .collect(Collectors.toSet());
    }

    public static void closeInventory(Player player) {
        Task.builder().delayTicks(1)
                .execute(()->{
                    //open inventory to close book renderers
                    player.openInventory(player.getInventory());
                    player.closeInventory();
                })
                .submit(MegaMenus.getInstance());
    }
    public static void openInventory(Player player, Inventory inventory) {
        Task.builder().delayTicks(1)
                .execute(()->{
                    player.closeInventory();
                    player.openInventory(inventory);
                })
                .submit(MegaMenus.getInstance());
    }

    /** looks like inventory properties like slot position etc are not always properly
     * passed down to sub inventories, thus I'll make it work... somehow */
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
