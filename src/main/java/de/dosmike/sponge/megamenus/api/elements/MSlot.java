package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with.
 */
final public class MSlot extends IElementImpl implements IInventory<MSlot> {

    private ItemStack holding;
    private OnSlotChangeListener<MSlot> listener = null;
    private int slotAccess = GUI_ACCESS_PUT|GUI_ACCESS_TAKE;
    private Predicate<ItemStackSnapshot> guiPutFilter, guiTakeFilter;

    /**
     * Create a new MSlot, that contains the specified item as ItemStack.<br>
     * Don't forget to set a Position!
     */
    public MSlot(ItemStackSnapshot defaultItem) {
        holding = defaultItem==null?ItemStack.empty():defaultItem.createStack();
    }
    /**
     * Create a new MSlot, that contains the specified item as ItemStack.<br>
     * Don't forget to set a Position!
     */
    public MSlot(ItemStack defaultItem) {
        holding = defaultItem==null?ItemStack.empty():defaultItem;
    }
    /**
     * Create a new MSlot, that contains the specified item as ItemStack.<br>
     * Don't forget to set a Position!
     */
    public MSlot(ItemType defaultItem) {
        holding = defaultItem==null?ItemStack.empty():ItemStack.of(defaultItem);
    }
    /**
     * Create a new MSlot, that does not contain any item.<br>
     * Don't forget to set a Position!
     */
    public MSlot() {
        holding = ItemStack.empty();
    }

    @Override
    public Optional<ItemStack> getItemStack() {
        return Optional.ofNullable(holding.isEmpty()?null:holding);
    }
    @Override
    public void setItemStack(ItemStackSnapshot snapshot) {
        holding = snapshot==null?ItemStack.empty():snapshot.createStack();
    }
    @Override
    public void setItemStack(ItemStack item) {
        holding = item==null?ItemStack.empty():item;
    }
    @Override
    public void setItemStack(ItemType type) {
        holding = type==null?ItemStack.empty():ItemStack.of(type,1);
    }

    @Override
    public void fireSlotChangeEvent(Player viewer, SlotChange change) {
        if (listener != null)
            listener.onSlotChange(change, this, viewer);
    }

    @Override
    public void setSlotChangeListener(OnSlotChangeListener<MSlot> listener) {
        this.listener = listener;
    }
    @Override
    public OnSlotChangeListener<MSlot> getSlotChangeListener() {
        return listener;
    }

    /**
     * This field defined whether the items in this slot of the GUI can be taken by a player or
     * placed by one. This returns a logical combination of the GUI_ACCESS_* values.<br>
     * Default value for InventoryElements = GUI_ACCESS_PUT|GUI_ACCESS_TAKE, otherwise GUI_ACCESS_NONE
     */
    @Override
    public int getAccess() {
        return slotAccess;
    }
    /**
     * Set the access to this slot according to a logic combination of GUI_ACCESS_* values.<br>
     * See getAccess() for more information.
     * @param slotAccess new added access permissions
     */
    public void setAccess(@MagicConstant(intValues = {0,1,2,3})  int slotAccess) {
        this.slotAccess = slotAccess;
    }

    /**
     * Can be used to automatically filter what items can be put into MSlots that have the
     * GUI_ACCESS_PUT flag set.
     * If this value is set to null, it acts identical to (x)-&gt;true.
     * @param filter return true to allow the passed item to be placed here.
     */
    public void setAccessFilterPut(Predicate<ItemStackSnapshot> filter) {
        this.guiPutFilter = filter;
    }
    /**
     * Can be used to automatically filter what items can be taken out of this MSlots if the
     * GUI_ACCESS_TAKE flag is set.
     * If this value is set to null, it acts identical to (x)-&gt;true.
     * @param filter return true to allow this item to be taken out from here.
     */
    public void setAccessFilterTake(Predicate<ItemStackSnapshot> filter) {
        this.guiTakeFilter = filter;
    }

    @Override
    public boolean testAccessPut(ItemStackSnapshot subject) {
        return (slotAccess & GUI_ACCESS_PUT)!=0 && (guiPutFilter==null || guiPutFilter.test(subject));
    }
    @Override
    public boolean testAccessTake(ItemStackSnapshot subject) {
        return (slotAccess & GUI_ACCESS_TAKE)!=0 && (guiTakeFilter==null || guiTakeFilter.test(subject));
    }

    @Override
    public IIcon getIcon(Player viewer) {
        return IIcon.of(holding);
    }

    @Override
    public Text getName(Player viewer) {
        return null;
    }

    @Override
    public List<Text> getLore(Player viewer) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MSlot copy() {
        MSlot copy = new MSlot();
        copy.setItemStack(getItemStack().orElse(ItemStack.empty()));
        copy.setParent(getParent());
        copy.pos = pos!=null?new SlotPos(pos.getX(), pos.getY()):null;
        copy.slotAccess = slotAccess;
        copy.listener = listener;
        copy.guiPutFilter = guiPutFilter;
        copy.guiTakeFilter = guiTakeFilter;
        return copy;
    }

    //Region builder
    public static class Builder {
        OnSlotChangeListener<MSlot> listener = null;
        ItemStack initial = ItemStack.empty();
        SlotPos pos = new SlotPos(0,0);
        @MagicConstant(intValues = {0,1,2,3})
        int access = GUI_ACCESS_PUT|GUI_ACCESS_TAKE;
        Predicate<ItemStackSnapshot> filterPut=null, filterTake=null;

        private Builder() {
        }

        /**
         * Not providing a position or setting the position to null
         * will query a position from the menus {@link PositionProvider}
         * at the moment the element is added to the menu.
         * @param position where this element is supposed to go or null for auto
         */
        public Builder setPosition(@Nullable SlotPos position) {
            pos = position;
            return this;
        }

        public Builder setItemStack(ItemStack initial) {
            Builder.this.initial = initial;
            return this;
        }
        /** Set a access permission as combination of IElement.GUI_ACCESS_* */
        public Builder setAccess(@MagicConstant(intValues = {0,1,2,3}) int access) {
            Builder.this.access = access;
            return this;
        }

        public Builder setOnSlotChangeListener(OnSlotChangeListener<MSlot> listener) {
            Builder.this.listener = listener;
            return this;
        }

        /** @see MSlot#setAccessFilterPut(Predicate)  */
        public Builder setAccessFilterPut(Predicate<ItemStackSnapshot> filter) {
            Builder.this.filterPut = filter;
            return this;
        }

        /** @see MSlot#setAccessFilterTake(Predicate)  */
        public Builder setAccessFilterTake(Predicate<ItemStackSnapshot> filter) {
            Builder.this.filterTake = filter;
            return this;
        }

        public MSlot build() {
            MSlot slot = new MSlot(initial);
            slot.listener = listener;
            slot.pos = pos;
            slot.setAccess(access);
            slot.setAccessFilterPut(filterPut);
            slot.setAccessFilterTake(filterTake);
            return slot;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion
}
