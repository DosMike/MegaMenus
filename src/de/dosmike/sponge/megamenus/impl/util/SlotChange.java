package de.dosmike.sponge.megamenus.impl.util;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.Optional;

/** This class is a delta representation of a sponge slot transaction
 * since i often do not care what the original / result stack was, but what was
 * actually table/put into the slot. */
final public class SlotChange {

    private ItemStackSnapshot take;
    private ItemStackSnapshot put;
    private SlotPos position;
    private SlotTransaction transaction;

    private SlotChange(){}
    public SlotChange(ItemStackSnapshot taken, ItemStackSnapshot given, SlotPos position, SlotTransaction originalTransaction) {
        this.take = taken;
        this.put = given;
        this.position = position;
        this.transaction = originalTransaction;
    }

    /** @return the Type and amount of items taken from the slot if any */
    public Optional<ItemStackSnapshot> getItemsTaken() {
        return Optional.ofNullable(take);
    }
    /** @return the Type and amount of items put into the slot if any */
    public Optional<ItemStackSnapshot> getItemsGiven() {
        return Optional.ofNullable(put);
    }
    /** @return the Slot this interaction occured on */
    public SlotPos getSlot() {
        return position;
    }
    /** @return the slot transaction responsible for this SlotChange for invalidation */
    public SlotTransaction getTransaction() {
        return transaction;
    }

    public static SlotChange from(SlotTransaction transaction) {
        SlotChange change = new SlotChange();

        change.transaction = transaction;
        //change.position = transaction.getSlot().getInventoryProperty(SlotPos.class).orElse(null);
        Integer index = transaction.getSlot().getInventoryProperty(SlotIndex.class).map(AbstractInventoryProperty::getValue).orElse(null);
        if (index != null) {
            change.position = new SlotPos(index % 9, index / 9);
        }
        ItemStackSnapshot slotAfter = transaction.getFinal();
        ItemStackSnapshot slotBefore = transaction.getOriginal();
        if (slotBefore.getType().equals(slotAfter.getType()) ||
                slotBefore.isEmpty() || slotAfter.isEmpty()) {
            int amount = slotAfter.getQuantity()-slotBefore.getQuantity();
            boolean take = amount < 0;
            ItemStackSnapshot deltaStack = ItemStack.builder().fromSnapshot(
                    take?slotBefore:slotAfter
            ).quantity(Math.abs(amount)).build().createSnapshot();
            if (take) {
                change.take = deltaStack;
                change.put = null;
            } else {
                change.take = null;
                change.put = deltaStack;
            }
        } else {
            change.take = slotBefore;
            change.put = slotAfter;
        }

        return change;
    }

}
