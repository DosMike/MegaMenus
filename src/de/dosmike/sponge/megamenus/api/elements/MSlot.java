package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import de.dosmike.sponge.megamenus.impl.util.SlotChange;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with.
 */
final public class MSlot extends IElementImpl implements IInventory {

    private ItemStack holding;
    private OnSlotChangeListener listener = null;
    private int slotAccess = GUI_ACCESS_PUT|GUI_ACCESS_TAKE;

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

    /**
     * @return the mutable {@link ItemStack} held in this slot
     */
    public Optional<ItemStack> getItemStack() {
        return Optional.ofNullable(holding.isEmpty()?null:holding);
    }
    /**
     * replaces the currently displayed {@link ItemStack} with the provided item
     * @param snapshot the new item to display
     */
    public void setItemStack(ItemStackSnapshot snapshot) {
        holding = snapshot==null?ItemStack.empty():snapshot.createStack();
    }
    /**
     * replaces the currently displayed itemstack with the provided item
     * @param item the new item to display
     */
    public void setItemStack(ItemStack item) {
        holding = item==null?ItemStack.empty():item;
    }
    /**
     * replaces the currently displayed itemstack with the provided item
     * @param type the new item to display
     */
    public void setItemStack(ItemType type) {
        holding = type==null?ItemStack.empty():ItemStack.of(type,1);
    }

    @Override
    public void fireSlotChangeEvent(Player viewer, SlotChange change) {
        if (listener != null)
            listener.onSlotChange(change, this, viewer);
    }

    @Override
    public void setSlotChangeListener(OnSlotChangeListener listener) {
        this.listener = listener;
    }
    @Override
    public OnSlotChangeListener getSlotChangeListener() {
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
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.slotAccess = slotAccess;
        return copy;
    }

    //Region builder
    public static class Builder {
        OnSlotChangeListener listener = null;
        ItemStack initial = ItemStack.empty();
        SlotPos pos = new SlotPos(0,0);
        int access = GUI_ACCESS_PUT|GUI_ACCESS_TAKE;

        private Builder() {
        }

        public Builder setPosition(SlotPos position) {
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

        public Builder setOnSlotChangeListener(OnSlotChangeListener listener) {
            Builder.this.listener = listener;
            return this;
        }

        public MSlot build() {
            MSlot slot = new MSlot(initial);
            slot.listener = listener;
            slot.setPosition(pos);
            slot.setAccess(access);
            return slot;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion
}
