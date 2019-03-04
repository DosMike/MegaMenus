package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.AntiGlitch;
import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.elements.MSlot;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.api.state.StateProperties;
import de.dosmike.sponge.megamenus.impl.util.MenuUtil;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This renderer draws the menu into a GUI of type Menu Grid.
 * If more than 1 page is specified the bottom, center 3 inventory slots
 * are automatically reserved for pagination elements.
 * @see MenuRenderer
 */
public class GuiRenderer extends AbstractMenuRenderer {

    private int pageHeight;
    /**
     * Please use BaseMenuImpl.createGuiRender() instead for validation
     */
    @Deprecated
    public GuiRenderer(IMenu menu, int pageHeight) {
        super(menu);
        this.pageHeight = pageHeight;
    }

    private void soon(Runnable r) {
        Task.builder().delayTicks(1).execute(r).submit(MegaMenus.getInstance());
    }

    private Function<Integer,Inventory> unlinkedInventoryProvider = new Function<Integer, Inventory>() {
        Consumer<InteractInventoryEvent> instanceListener = (event)->{
            Player viewer = event.getCause().first(Player.class).orElse(null);
            if (viewer == null) {
                return;
            }

            if (event instanceof ClickInventoryEvent.Double) {
                event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
                event.getCursorTransaction().setValid(false);
                ((ClickInventoryEvent.Double)event)
                        .getTransactions()
                        .forEach(t->t.setValid(false));
                event.setCancelled(true);
            }
            if (event instanceof ClickInventoryEvent) {
                AntiGlitch.quickInteractionTrack(viewer);
                RenderManager.getRenderFor(viewer)
                        .filter(r->r instanceof GuiRenderer)
                        .map(r->(GuiRenderer)r)
                        .ifPresent(render->{
                    ClickInventoryEvent ice = (ClickInventoryEvent) event;
                    ice.getTransactions().stream()
                            .map(SlotChange::from)
                            .filter(s -> s.getSlot()!=null && s.getSlot().getY() < render.pageHeight)
                            .forEach(s -> render.interactHandler(viewer, s, ice));
                });
            }
        };

        @Override
        public Inventory apply(Integer integer) {
            return Inventory.builder().of(InventoryArchetypes.MENU_GRID)
                    .property(InventoryDimension.of(9, integer))
                    .property(InventoryTitle.of(menu.getTitle()))
                    .listener(InteractInventoryEvent.class, instanceListener)
                    .build(MegaMenus.getInstance());
        }
    };

    private synchronized void interactHandler(Player viewer, SlotChange slot, ClickInventoryEvent event) {
        GuiRenderer render = (GuiRenderer)RenderManager.getRenderFor(viewer).orElse(null);
        if (render == null) return;
        //shadow local menu, because interaction handler should always use the menu from the open render
        IMenu menu = render.getMenu();
        //get the element
        Set<IElement> elements = MenuUtil.getElementsAtPosition(
                menu,
                menu.getPlayerState(viewer.getUniqueId()).getInt(StateProperties.PAGE).orElse(1),
                slot.getSlot());

        SlotChange testChange = slot;
        // copied items onto cursor
        if (event instanceof ClickInventoryEvent.Middle && !slot.getItemsTaken().isPresent()) {
            testChange = new SlotChange(event.getCursorTransaction().getFinal(), slot.getItemsGiven().orElse(null), slot.getSlot(), slot.getTransaction());
        }

        if (rendering.get()) {
            //prevent events during redraw
            testChange.getTransaction().setValid(false);
            event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
            event.getCursorTransaction().setValid(false);
            event.setCancelled(true);
        } else if (menu.pages() > 1 && testChange.getSlot().getY() == pageHeight-1 && testChange.getSlot().getX() >= 3 && testChange.getSlot().getX() <= 5) {
            //automatic pagination buttons
            testChange.getTransaction().setValid(false);
            event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
            event.getCursorTransaction().setValid(false);
            event.setCancelled(true);
            int page = menu
                    .getPlayerState(viewer.getUniqueId())
                    .getInt(StateProperties.PAGE)
                    .orElse(1);
            if (testChange.getSlot().getX() == 3 && page > 1) {
                menu.getPlayerState(viewer.getUniqueId()).set(StateProperties.PAGE, page-1);
                invalidate();
            } else if (testChange.getSlot().getX() == 5 && page < menu.pages()) {
                menu.getPlayerState(viewer.getUniqueId()).set(StateProperties.PAGE, page+1);
                invalidate();
            }
        } else if (elements.isEmpty()) {
            //prevent putting items into empty slots
            testChange.getTransaction().setValid(false);
            event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
            event.getCursorTransaction().setValid(false);
            event.setCancelled(true);
        } else for (IElement e : elements) {
            //default actions
            boolean cancelInventory = false;
            if ((e.getAccess() & IElement.GUI_ACCESS_TAKE)==0 && testChange.getItemsTaken().map(i->!i.isEmpty()).orElse(false)) {
                testChange.getTransaction().setValid(false);
                event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
                event.getCursorTransaction().setValid(false);
                event.setCancelled(true);
                cancelInventory = true;
            }
            if ((e.getAccess() & IElement.GUI_ACCESS_PUT)==0 && testChange.getItemsGiven().map(i->!i.isEmpty()).orElse(false)) {
                testChange.getTransaction().setValid(false);
                event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
                event.getCursorTransaction().setValid(false);
                event.setCancelled(true);
                cancelInventory = true;
            }

            if (e instanceof IClickable) {
                int button = MouseEvent.NOBUTTON;
                if (event instanceof ClickInventoryEvent.Primary) button = MouseEvent.BUTTON1;
                else if (event instanceof ClickInventoryEvent.Secondary) button = MouseEvent.BUTTON2;
                else if (event instanceof ClickInventoryEvent.Middle) button = MouseEvent.BUTTON3;
                ((IClickable)e).fireClickEvent(viewer, button, (event instanceof ClickInventoryEvent.Shift));
            }
            if (!cancelInventory && e instanceof IInventory) {
                if (e instanceof MSlot) {
                    ((MSlot)e).setItemStack(slot.getTransaction().getFinal());
                }
                ((IInventory)e).fireSlotChangeEvent(viewer, slot);
            }
        }
    }

    @Override
    synchronized void render(Player viewer) {
        boolean inventoryPresent = viewer.getOpenInventory().get().first().getPlugin().getId().equals(MegaMenus.getInstance().asContainer().getId());
        Optional<MenuRenderer> render = RenderManager.getRenderFor(viewer);
        if (!render.isPresent()) {
            //open the inventory to the player if no menu was already open
            viewer.openInventory(unlinkedInventoryProvider.apply(pageHeight)).ifPresent(i->
                soon(()->
                    redraw(viewer)
                )
            );
        } else if (!inventoryPresent) {
            render.get().closeSilent(viewer);
            viewer.openInventory(unlinkedInventoryProvider.apply(pageHeight)).ifPresent(i->
                    soon(()->
                            redraw(viewer)
                    )
            );
        } else if (!render.get().equals(this)) { // a different menu is open
            //if a critical factor changed (one that can't be updated but requires a new menu)
            if (!(render.get() instanceof GuiRenderer) || //different type of renderer was used
                    ((GuiRenderer) render.get()).pageHeight != pageHeight || //different dimensions were used
                    !render.get().getMenu().getTitle().equals(menu.getTitle())) { //different title was used
                //then create a new menu that matches the specified data
                viewer.openInventory(unlinkedInventoryProvider.apply(pageHeight)).ifPresent(i ->
                        soon(()->redraw(viewer))
                );
            } else {//a matching inventory was used
                render.get().closeSilent(viewer);
                soon(()->render(viewer));
            }
        } else { //menu did not change, just redraw
            redraw(viewer);
        }

    }
    private AtomicBoolean rendering = new AtomicBoolean(false);
    void redraw(Player viewer) {
        if (!viewer.getOpenInventory().get().first().getPlugin().getId().equals(MegaMenus.getInstance().asContainer().getId())) { //menu closed early
            return;
        }
        rendering.set(true);
        List<SlotPos> paintTracker = new LinkedList<>();
        for (int y=0; y<pageHeight; y++)
            for (int x=0; x<9; x++)
                paintTracker.add(SlotPos.of(x,y));

        int page = getMenu()
                .getPlayerState(viewer.getUniqueId())
                .getInt(StateProperties.PAGE)
                .orElse(1);
        menu.getPageElements(page)
            .forEach(element-> {
                try {
                    element.validateGui(pageHeight);
                    paintTracker.removeAll(
                            element.renderGUI(viewer)
                    );
                } catch (Exception e) {
                    rendering.set(false);
                    new RuntimeException("Unable to render Element "+element.getUniqueId().toString(), e).printStackTrace();
                }
            });
        if (!RenderManager.getRenderFor(viewer).map(MenuRenderer::getMenu).filter(m->m.equals(menu)).isPresent()) {
            AntiGlitch.quickInteractionTrack(viewer);
            rendering.set(false);
            return;
        }

        //pagination
        Inventory view = viewer.getOpenInventory().get().first(); //when is this not present?
        if (menu.pages()>1) {
            int pagination = (pageHeight-1)*9+3;
            if (page > 1) {
                view.query(SlotIndex.of(pagination)).set(ItemStack.builder().itemType(ItemTypes.ARROW).add(Keys.DISPLAY_NAME, Text.of("< Back")).build());
            } else {
                view.query(SlotIndex.of(pagination)).clear();
            }
            view.query(SlotIndex.of(pagination+1)).set(ItemStack.builder().itemType(ItemTypes.PAPER).add(Keys.DISPLAY_NAME, Text.of("Page ",page,"/",menu.pages())).build());
            if (page < menu.pages()) {
                view.query(SlotIndex.of(pagination+2)).set(ItemStack.builder().itemType(ItemTypes.ARROW).add(Keys.DISPLAY_NAME, Text.of("Next >")).build());
            } else {
                view.query(SlotIndex.of(pagination+2)).clear();
            }
            paintTracker.remove(SlotPos.of(3,pageHeight-1));
            paintTracker.remove(SlotPos.of(4,pageHeight-1));
            paintTracker.remove(SlotPos.of(5,pageHeight-1));
        }
        //background
        BackgroundProvider provider = menu.getBackground();
        if (provider == null) provider = BackgroundProvider.BACKGROUND_DEFAULT;
        for (SlotPos p : paintTracker) {
            if (!RenderManager.getRenderFor(viewer).map(MenuRenderer::getMenu).filter(m->m.equals(menu)).isPresent()) {
                AntiGlitch.quickInteractionTrack(viewer);
                break;
            }
            IIcon at = provider.drawAt(p, menu.getState(), menu.getPlayerState(viewer.getUniqueId()));
            if (at == null)
                view.query(p).clear();
            else
                view.query(p).set(at.render().createStack());
        }
        rendering.set(false);
    }
}
