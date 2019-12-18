package de.dosmike.sponge.megamenus;

import de.dosmike.sponge.megamenus.impl.BookRenderer;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;

final public class EventListeners {

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event) {
        event.getCause().first(Player.class).ifPresent(player->{
            RenderManager.getRenderFor(player).ifPresent(render-> {
                render.close(player);
                cleanInv(player); // (mainly) AntiGlitch Measure
            });
        });
    }
    @Listener
    public void onInteractBlock(InteractBlockEvent event) {
        Player player = event.getCause().first(Player.class).orElse(null);
        if (player == null) return;
        RenderManager.kickFromAll(player);
        // AntiGlitch Measure
        if (AntiGlitch.checkItemStack(player, player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty())) ||
            AntiGlitch.checkItemStack(player, player.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStack.empty()))) {
            event.setCancelled(true);
            cleanInv(player);
        }
    }
    @Listener
    public void onInteractEntity(InteractBlockEvent event) {
        Player player = event.getCause().first(Player.class).orElse(null);
        if (player == null) return;
        RenderManager.kickFromAll(player);
        // AntiGlitch Measure
        if (AntiGlitch.checkItemStack(player, player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty())) ||
            AntiGlitch.checkItemStack(player, player.getItemInHand(HandTypes.OFF_HAND).orElse(ItemStack.empty()))) {
            event.setCancelled(true);
            cleanInv(player);
        }
    }
    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        RenderManager.kickFromAll(event.getTargetEntity());
    }

    //check if book was closed and close the renderer
    @Listener
    public void onLookOrMoveEvent(MoveEntityEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player p = (Player)event.getTargetEntity();
            RenderManager.getRenderFor(p).ifPresent(r->{
                if (r instanceof BookRenderer)
                    r.close(p);
            });
        }
    }

    // AntiGlitch Measure
    // not necessary anymore - inv scan on close
//    @Listener(order = Order.EARLY)
//    public void onDropItem(DropItemEvent.Pre event) {
//        Player player = event.getCause().first(Player.class).orElse(null);
//        if (player == null) return;
//        boolean result = false;
//        for (ItemStackSnapshot s : event.getOriginalDroppedItems()) {
//            result |= AntiGlitch.checkItemStack(player, s);
//        }
//        if (result) {
//            // "allow" the item to be dropped, but drop into nothing
//            event.getDroppedItems().clear();
//            AntiGlitch.log(player, event.getOriginalDroppedItems());
//        }
//    }

    private void cleanInv(Player player) {
        Task.builder()
                .delayTicks(1)
                .execute(()->AntiGlitch.scanInventory(player))
                .submit(MegaMenus.getInstance());
    }

}
