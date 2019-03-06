package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;

public class MenuRendererEvent implements Event {

    protected MenuRenderer renderer;

    private Cause cause;

    private MenuRendererEvent(Cause cause){
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public static class Open extends MenuRendererEvent implements TargetPlayerEvent {
        private Player target;
        public Open(MenuRenderer renderer, Player viewer, Cause cause) {
            super(cause);
            this.renderer = renderer;
            this.target = viewer;
        }
        @Override
        public Player getTargetEntity() {
            return target;
        }
    }
    public static class Close extends MenuRendererEvent implements TargetPlayerEvent {
        private Player target;
        public Close(MenuRenderer renderer, Player viewer, Cause cause) {
            super(cause);
            this.renderer = renderer;
            this.target = viewer;
        }
        @Override
        public Player getTargetEntity() {
            return target;
        }
    }
    public static class Pause extends MenuRendererEvent {
        public Pause(MenuRenderer renderer, Cause cause) {
            super(cause);
            this.renderer = renderer;
        }
    }
    public static class Resume extends MenuRendererEvent {
        public Resume(MenuRenderer renderer, Cause cause) {
            super(cause);
            this.renderer = renderer;
        }
    }

}
