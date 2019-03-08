package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.MButton;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class MenuButtonClickEvent implements Event {

    private MButton element;
    private IMenu menu;
    private Player viewer;

    private int button;
    private boolean shift;

    private Cause cause;

    public MenuButtonClickEvent(MButton element, Player viewer, int button, boolean shift, Cause cause) {
        this.element = element;
        this.viewer = viewer;
        this.menu = element.getParent();
        this.button = button;
        this.shift = shift;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public MButton getElement() {
        return element;
    }

    public IMenu getMenu() {
        return menu;
    }

    public Player getViewer() {
        return viewer;
    }

    public int getButton() {
        return button;
    }

    public boolean isShift() {
        return shift;
    }
}
