package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.BackgroundProvider;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.elements.MSlot;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.api.state.StateProperties;
import de.dosmike.sponge.megamenus.impl.util.LinkedMenuProperty;
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
import org.spongepowered.api.item.inventory.property.*;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class GuiRender extends AbstractMenuRender {

    private Inventory inventory;
    private int pageHeight;
    /** Please use BaseMenuImpl.createGuiRender() instead for validation */
    @Deprecated
    public GuiRender(IMenu menu, int pageHeight) {
        super(menu);
        this.pageHeight = pageHeight;
        this.inventory = Inventory.builder().of(InventoryArchetypes.MENU_GRID)
                .property(InventoryTitle.of(menu.getTitle()))
                .property(InventoryDimension.of(9, pageHeight))
                .property(LinkedMenuProperty.of(menu))
                .listener(InteractInventoryEvent.class, instanceListener)
                .build(MegaMenus.getInstance());
    }

    Consumer<InteractInventoryEvent> instanceListener = (event)->{
        Inventory inventory = event.getTargetInventory().first();
        Player viewer = event.getCause().first(Player.class).orElse(null);
        if (viewer == null) {
//            MegaMenus.l("No player for inventory interaction ö");
            return;
        }
        if (event instanceof InteractInventoryEvent.Close) {
            closeSilent(viewer);
        } else if (event instanceof ClickInventoryEvent) {
            ClickInventoryEvent ice = (ClickInventoryEvent) event;
            ice.getTransactions().stream()
                    .map(SlotChange::from)
                    .filter(s -> s.getSlot()!=null && s.getSlot().getY() < pageHeight)
                    .forEach(s -> interactHandler(viewer, s, ice));
        }
    };

    private void interactHandler(Player viewer, SlotChange slot, ClickInventoryEvent event) {
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

        if (menu.pages() > 1 && testChange.getSlot().getY() == pageHeight-1 && testChange.getSlot().getX() >= 3 && testChange.getSlot().getX() <= 5) {
            //automatic pagination buttons
            testChange.getTransaction().setValid(false);
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
            event.getCursorTransaction().setValid(false);
            event.setCancelled(true);
        } else for (IElement e : elements) {
            //default actions
            boolean cancelInventory = false;
            if ((e.getAccess() & IElement.GUI_ACCESS_TAKE)==0 && testChange.getItemsTaken().map(i->!i.isEmpty()).orElse(false)) {
                testChange.getTransaction().setValid(false);
                event.getCursorTransaction().setValid(false);
                event.setCancelled(true);
                cancelInventory = true;
            }
            if ((e.getAccess() & IElement.GUI_ACCESS_PUT)==0 && testChange.getItemsGiven().map(i->!i.isEmpty()).orElse(false)) {
                testChange.getTransaction().setValid(false);
                event.getCursorTransaction().setValid(false);
                event.setCancelled(true);
                cancelInventory = true;
            }

            if (e instanceof IClickable) {
                OnClickListener listener = ((IClickable)e).getOnClickListerner();
                if (listener != null)
                    listener.onClick((IClickable)e, viewer);
            }
            if (!cancelInventory && e instanceof IInventory) {
                if (e instanceof MSlot) {
//                    MegaMenus.l("Updateing MSlot to %d %s", slot.getTransaction().getFinal().getQuantity(), slot.getTransaction().getFinal().getTranslation().get());
                    ((MSlot)e).setItemStack(slot.getTransaction().getFinal());
                }
                OnSlotChangeListener listener = ((IInventory)e).getSlotChangeListener();
                if (listener != null)
                    listener.onSlotChange(slot, e, viewer);
            }
        }
    }

    @Override
    void render(Player viewer) {
//        viewer.getOpenInventory().ifPresent(container -> MegaMenus.l("Viewing %s(%s,%s)",
//                container.getArchetype().getId(),
//                container.getClass().getSimpleName(),
//                container.getInventoryProperty(InventoryDimension.class).map(d -> d.getRows() + "x" + d.getColumns()).orElse("?x?")));
        Optional<IMenu> openMenu = viewer.getOpenInventory().flatMap(i->i.getInventoryProperty(LinkedMenuProperty.class)).map(AbstractInventoryProperty::getValue);
        if (!openMenu.isPresent()) {
            //open the inventory to the player if it's not already open
            viewer.openInventory(inventory).ifPresent(i->
                MegaMenus.getSyncExecutor().execute(()->
                    redraw(viewer)
                )
            );
        } else if (!openMenu.get().equals(menu)) {
            closeSilent(viewer); //they left us :< - different menu is open
        } else {
            MegaMenus.getSyncExecutor().execute(() ->
                redraw(viewer)
            );
        }

    }
    void redraw(Player viewer) {
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
                            element.renderGUI(
                                    menu.getState(),
                                    menu.getPlayerState(viewer.getUniqueId()),
                                    viewer
                            )
                    );
                } catch (Exception e) {
                    new RuntimeException("Unable to render Element "+element.getUniqueId().toString(), e).printStackTrace();
                }
            });

        Inventory view = viewer.getOpenInventory().get(); //when is this not present?
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
        BackgroundProvider provider = menu.getBackground();
        if (provider == null) provider = BackgroundProvider.BACKGROUND_DEFAULT;
        for (SlotPos p : paintTracker) {
            IIcon at = provider.drawAt(p, menu.getState(), menu.getPlayerState(viewer.getUniqueId()));
            if (at == null)
                view.query(p).clear();
            else
                view.query(p).set(at.render().createStack());
        }
    }

//    @Override
//    public boolean isViewedSlot(Slot slot) {
//        Inventory container = slot.parent();
//        MegaMenus.l("Slot part of %s(%s,%s)",
//                container.getArchetype().getId(),
//                container.getClass().getSimpleName(),
//                container.getInventoryProperty(InventoryDimension.class).map(d -> d.getRows() + "x" + d.getColumns()).orElse("?x?"));
//        return false;
//    }
}
