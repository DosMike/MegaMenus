package de.dosmike.sponge.megamenus.impl.util;

import de.dosmike.sponge.megamenus.api.IMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;

public class LinkedMenuProperty extends AbstractInventoryProperty<String, IMenu> {

    private LinkedMenuProperty(IMenu menu) {
        super(menu);
    }

    @Override
    public int compareTo(@NotNull Property<?, ?> o) {
        return 0;
    }

    public static LinkedMenuProperty of(IMenu menu) {
        return new LinkedMenuProperty(menu);
    }

}
