package de.dosmike.sponge.megamenus;

import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;

public class EventListeners {

//    @Listener
//    public void onEvent(Event event) {
//        if (event instanceof InteractInventoryEvent) {
//            MegaMenus.l("Inventory Event: " + event.getClass().getSimpleName());
//        } else if (!(event instanceof TargetInventoryEvent) &&
//                !(event instanceof MoveEntityEvent) &&
//                !(event instanceof ChangeStatisticEvent) &&
//                !(event instanceof TickBlockEvent) &&
//                !(event instanceof TargetWorldEvent) &&
//                !(event instanceof TargetChunkEvent) &&
//                !(event instanceof CollideEvent)) {
//            MegaMenus.l("Event triggered: " + event.getClass().getSimpleName());
//        }
//    }

    public void onCloseInventory(InteractInventoryEvent.Close event) {
        event.getCause().first(Player.class).ifPresent(RenderManager::kickFromAll);
    }
    public void onInteractBlock(InteractBlockEvent event) {
        event.getCause().first(Player.class).ifPresent(RenderManager::kickFromAll);
    }
    public void onInteractEntity(InteractBlockEvent event) {
        event.getCause().first(Player.class).ifPresent(RenderManager::kickFromAll);
    }

}
