package de.dosmike.sponge.megamenus.compat.events;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;

public class HookRendererEvent implements Event {

    protected MenuRenderer renderer;

    private HookRendererEvent(){}

    @Override
    public Cause getCause() {
        return Sponge.getCauseStackManager().getCurrentCause();
    }

    public static class Open extends HookRendererEvent implements TargetPlayerEvent {
        private Player target;
        public Open(MenuRenderer renderer, Player viewer) {
            this.renderer = renderer;
            this.target = viewer;
        }
        @Override
        public Player getTargetEntity() {
            return target;
        }
    }
    public static class Close extends HookRendererEvent implements TargetPlayerEvent {
        private Player target;
        public Close(MenuRenderer renderer, Player viewer) {
            this.renderer = renderer;
            this.target = viewer;
        }
        @Override
        public Player getTargetEntity() {
            return target;
        }
    }
    public static class Pause extends HookRendererEvent {
        public Pause(MenuRenderer renderer) {
            this.renderer = renderer;
        }
    }
    public static class Resume extends HookRendererEvent {
        public Resume(MenuRenderer renderer) {
            this.renderer = renderer;
        }
    }

}
