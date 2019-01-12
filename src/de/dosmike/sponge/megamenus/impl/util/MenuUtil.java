package de.dosmike.sponge.megamenus.impl.util;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.ISizeable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.SlotPos;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MenuUtil {

    /** tries to retrieve the {@link IMenu} this player is currently looking at. */
    public static Optional<IMenu> findFor(Player player) {
        return player.getOpenInventory().flatMap(inv->
                inv.getInventoryProperty(LinkedMenuProperty.class)
        ).map(AbstractInventoryProperty::getValue);
    }

    public static Set<IElement> getElementsAtPosition(IMenu menu, int page, SlotPos pos) {
        Collection<IElement> pageElements = menu.getPageElements(page);
        return pageElements.stream()
                .filter(e -> ((e instanceof ISizeable) && ((ISizeable)e).containsPosition(pos)) || e.getPosition().equals(pos))
                .collect(Collectors.toSet());
    }

}
