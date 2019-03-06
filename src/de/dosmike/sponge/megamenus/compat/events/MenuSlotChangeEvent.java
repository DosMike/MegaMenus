package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.MSlot;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class MenuSlotChangeEvent implements Event {

    private MSlot element;
    private IMenu menu;
    private Player viewer;

    private SlotChange change;

    private Cause cause;

    public MenuSlotChangeEvent(MSlot element, SlotChange change, Player viewer, Cause cause) {
        this.element = element;
        this.viewer = viewer;
        this.menu = element.getParent();
        this.change = change;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public MSlot getElement() {
        return element;
    }

    public IMenu getMenu() {
        return menu;
    }

    public Player getViewer() {
        return viewer;
    }

    public SlotChange getChange() {
        return change;
    }
}