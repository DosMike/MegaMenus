package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IInventory;
import de.dosmike.sponge.megamenus.api.listener.OnSlotChangeListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with. */
public class MSlot extends IElementImpl implements IInventory {

    private ItemStack holding;
    private OnSlotChangeListener listener;
    private int slotAccess = GUI_ACCESS_PUT|GUI_ACCESS_TAKE;

    public MSlot(ItemStackSnapshot defaultItem) {
        holding = defaultItem.createStack();
    }
    public MSlot(ItemStack defaultItem) {
        holding = defaultItem;
    }
    public MSlot(ItemType defaultItem) {
        holding = ItemStack.of(defaultItem);
    }

    /** returns the mutable itemstack held in this slot */
    public Optional<ItemStack> getItemStack() {
        return Optional.ofNullable(holding.isEmpty()?null:holding);
    }
    /** replaces the currently displayed itemstack with the provided item */
    public void setItemStack(ItemStackSnapshot snapshot) {
        holding = snapshot.createStack();
    }
    /** replaces the currently displayed itemstack with the provided item */
    public void setItemStack(ItemStack item) {
        holding = item;
    }
    /** replaces the currently displayed itemstack with the provided item */
    public void setItemStack(ItemType type) {
        holding = ItemStack.of(type,1);
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
    /** Set the access to this slot according to a logic combination of GUI_ACCESS_* values.
     * See getAccess() for more information. */
    public void setAccess(@MagicConstant(intValues = {0,1,2,3})  int slotAccess) {
        this.slotAccess = slotAccess;
    }

    @Override
    public IIcon getIcon(StateObject menuState, StateObject viewerState) {
        return IIcon.of(holding);
    }

    @Override
    public Text getName(StateObject menuState, StateObject viewerState) {
        return holding.get(Keys.DISPLAY_NAME).orElse(Text.of(holding.getTranslation().get()));
    }

    @Override
    public List<Text> getLore(StateObject menuState, StateObject viewerState) {
        return holding.get(Keys.ITEM_LORE).orElseGet(LinkedList::new);
    }

//    @Override
//    public Collection<SlotPos> renderGUI(StateObject menuState, StateObject viewerState, Player viewer) {
//        return Collections.singletonList(getPosition());
//    }

    @SuppressWarnings("unchecked")
    @Override
    public MSlot copy() {
        MSlot copy = new MSlot(holding.createSnapshot());
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        return copy;
    }

    //Region builder
    public static class Builder {
        OnSlotChangeListener listener = null;
        ItemStack initial;
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
        /** Set a logic combination of IElement.GUI_ACCESS_* */
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
