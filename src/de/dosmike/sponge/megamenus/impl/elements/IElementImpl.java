package de.dosmike.sponge.megamenus.impl.elements;

import de.dosmike.sponge.megamenus.AntiGlitch;
import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.util.MenuUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public abstract class IElementImpl implements IElement {

    private SlotPos pos = new SlotPos(0,0);
    private IMenu parent;
    protected UUID uiid = UUID.randomUUID();

    @Override
    public UUID getUniqueId() {
        return uiid;
    }

    @Override
    public SlotPos getPosition() {
        return pos;
    }

    @Override
    public void setPosition(@NotNull SlotPos position) {
        this.pos = position;
    }

    public void setParent(IMenu menu) {
        if (this.parent != null && menu != null)
            throw new ObjectBuilderException("This Element is already bound to a menu");
        this.parent = menu;
    }

    @Override
    public IMenu getParent() {
        return parent;
    }

    @Override
    public Collection<SlotPos> renderGUI(StateObject menuState, StateObject viewerState, Player viewer)  {
        Inventory view = viewer.getOpenInventory().get(); //when is this not present?
        /* about openInventoroy().first()
         * This will return the top most inventory, if the user has one of our menus open
         * it will be the menu we created and thus have out plugin id bound to it.
         * if it's the player inventory or some other menu/mod inventory this will 100% be
         * missing and thus is probably the most reliable way in API 7 to tell, if we're
         * currently inside out inventory.
         * This check is requried in order to prevent "rendering" the menu into the player
         * menu once the renderer is closed since the player inventory slots will ALSO start
         * from 0, so the slot alone is insufficient.
         * Since the custom inventory does not seem to properly provide slot positions it'll
         * only be used for plugin id compares.
         */
        if (!view.first().getPlugin().getId().equals(MegaMenus.getInstance().asContainer().getId())) {
            return Collections.emptyList();
        }
        Optional<IMenu> menu = RenderManager.getRenderFor(viewer).map(MenuRenderer::getMenu);
        if (!menu.isPresent() || !menu.get().equals(getParent())) {
            MegaMenus.w("Menu was closed or changed during render");
            AntiGlitch.calloutGlitcher(viewer);
            return Collections.emptyList();
        }
        //convert position to index because slots seem to only retain index

        IIcon icon = getIcon(menuState, viewerState);
        if (icon != null) {
            Inventory slot = MenuUtil.getSlotByAnyMeans(view, getPosition()).orElse(null);
            if (slot == null || slot.capacity() == 0) {
                MegaMenus.w("No slot matched position %d,%d", getPosition().getX(), getPosition().getY());
            } else {
                ItemStack render = ItemStack.builder().fromSnapshot(icon.render())
                        .add(Keys.DISPLAY_NAME, getName(menuState, viewerState))
                        .add(Keys.ITEM_LORE, getLore(menuState, viewerState))
                        .build();
                if (!exequalitemstack(slot.peek().orElse(ItemStack.empty()),render)) {//stack did change
                    // ignore this if unchanged in order to save network
                    // - less slot transaction are more! :D
                    if (render.getQuantity() == 0 ||
                            render.getType().equals(ItemTypes.AIR)) {
                        slot.clear(); //because setting air / empty stacks does nothing
                    } else {
                        slot.set(render);
                    }
                }
            }
        }
        return Collections.singleton(getPosition());
    }
    private boolean exequalitemstack(ItemStack a, ItemStack b) {
        if (a.getQuantity() == 0 && b.getQuantity() == 0) return true;
        return a.equalTo(b);
    }

    @Override
    public void validateGui(int pageHeight) throws ObjectBuilderException {
        if (pos.getX()<0 || pos.getX()>=9 ||
            pos.getY()<0 || pos.getY()>pageHeight-1 ||
            (parent.pages()>1 && pos.getY()==pageHeight-1 && //if paginated the last row's center elements are off limits
                pos.getX()>=4 && pos.getX() <= 6)) {
            throw new ObjectBuilderException("Element outside of visible area");
        }
    }

    private Tickable thinkHook = null;
    public void hookThinkTick(Tickable hook) {
        this.thinkHook = hook;
    }

    @Override
    public boolean think(AnimationManager animations, StateObject menuState, StateObject viewerState) {
        boolean hookChange = false;
        if (thinkHook != null) hookChange = animations.singleTick(thinkHook);
        IIcon icon = getIcon(menuState, viewerState);
        if (icon != null) hookChange |= animations.singleTick(icon);
        return hookChange;
    }
}
