package de.dosmike.sponge.megamenus;

import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

final public class EventListeners {

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event) {
        event.getCause().first(Player.class).ifPresent(player->{
            RenderManager.getRenderFor(player).ifPresent(render-> {
                AntiGlitch.quickInteractionTrack(player);
                render.close(player);
            });
        });
    }
    @Listener
    public void onInteractBlock(InteractBlockEvent event) {
        event.getCause().first(Player.class).ifPresent(RenderManager::kickFromAll);
    }
    @Listener
    public void onInteractEntity(InteractBlockEvent event) {
        event.getCause().first(Player.class).ifPresent(RenderManager::kickFromAll);
    }
    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        AntiGlitch.glitchUntrack(event.getTargetEntity());
        RenderManager.kickFromAll(event.getTargetEntity());
    }

}
