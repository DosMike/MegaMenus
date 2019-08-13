package de.dosmike.sponge.megamenus.compat.events;

import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.UUID;

public class ContextKeys {

    public static final EventContextKey<UUID> menuID = EventContextKey.builder(UUID.class)
            .name("MenuID")
            .id("megamenus:context_menuid")
            .build();

    public static final EventContextKey<Integer> menuColumn = EventContextKey.builder(Integer.class)
            .name("MenuColumn")
            .id("megamenus:context_menucolumn")
            .build();

    public static final EventContextKey<Integer> menuRow = EventContextKey.builder(Integer.class)
            .name("MenuRow")
            .id("megamenus:context_menurow")
            .build();

    public static final EventContextKey<Integer> mouseButton = EventContextKey.builder(Integer.class)
            .name("MouseButton")
            .id("megamenus:context_mousebutton")
            .build();

    public static final EventContextKey<Boolean> shiftHeld = EventContextKey.builder(Boolean.class)
            .name("ShiftPressed")
            .id("megamenus:context_shiftpressed")
            .build();

    public static final EventContextKey<ItemStackSnapshot> stackTaken = EventContextKey.builder(ItemStackSnapshot.class)
            .name("StackTaken")
            .id("megamenus:context_stacktaken")
            .build();

    public static final EventContextKey<ItemStackSnapshot> stackInsert = EventContextKey.builder(ItemStackSnapshot.class)
            .name("StackInserted")
            .id("megamenus:context_stackinserted")
            .build();

    public static final EventContextKey<Integer> targetValue = EventContextKey.builder(Integer.class)
            .name("TargetValue")
            .id("megamenus:context_targetvalue")
            .build();

}
