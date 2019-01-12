package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.MenuRender;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/** This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with. */
public class MCheckbox extends IElementImpl implements IClickable, IValueChangeable<Integer> {

    private List<IIcon> icons = Arrays.asList(
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.GRAY).build()),
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.MAGENTA).build()),
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.LIME).build())
    );
    private int value =0;
    private OnClickListener clickListener = null;
    private OnChangeListener<Integer> changeListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

    /** performs the internal progress fo the cyclic element and calls external listener */
    private OnClickListener internalClickListener = (e, v) -> {
        int pre = getValue();
        setValue(pre == 1 ? 0 : 1);
        RenderManager.getRenderFor(v).ifPresent(MenuRender::invalidate);
        if (clickListener!=null) {
            clickListener.onClick(e, v);
        }
        if (changeListener!=null)
            changeListener.onValueChange(pre, getValue(), MCheckbox.this, v);
    };

    /**
     * Values: 1 selected, 0 not selected, -1 tri-state
     */
    public int getValue() {
        return value;
    }

    /**
     * Values: 1 selected, 0 not selected, -1 tri-state
     */
    public void setValue(int value) {
        if (value < -1 || value > 1)
            throw new IllegalArgumentException("Checkbox value out of range (-1,0,1)");
        this.value = value;
    }

    /**
     * Invoking this manually will cause the cyclic element to progress and call the change listener as well
     */
    @Override
    public OnClickListener getOnClickListerner() {
        return internalClickListener;
    }

    @Override
    public OnChangeListener<Integer> getOnChangeListener() {
        return changeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public IIcon getIcon(StateObject menuState, StateObject viewerState) {
        return icons.get(value+1);
    }

    @Override
    public Text getName(StateObject menuState, StateObject viewerState) {
        return defaultName;
    }

    /**
     * The default implementation highlights the line with the same position ans the selected value
     * if the number of lines in this lore equals the number of possible values. Otherwise the set lore
     * is returned unmodified.
     * If you do not want this to style your lore overwrite with <code>return defaultLore;</code>
     */
    @Override
    public List<Text> getLore(StateObject menuState, StateObject viewerState) {
        Text t = getValue()==0
                ? Text.of(TextColors.RED, "Deactivated")
                : (getValue()==1
                ? Text.of(TextColors.GREEN, "Activated")
                : Text.of(TextColors.GRAY, "Unknown")
        );
        List<Text> lore = new LinkedList<>();
        lore.add(t);
        for (Text elem : defaultLore)
            lore.add(Text.of(TextColors.GRAY, elem));
        return lore;
    }

    @Override
    public void setOnChangeListener(OnChangeListener<Integer> listener) {
        changeListener = listener;
    }

    private MCheckbox() {}

    //Region builder
    public static class Builder {
        MCheckbox element = new MCheckbox();
        private Builder() {
        }

        public Builder setPosition(SlotPos position) {
            element.setPosition(position);
            return this;
        }

        public Builder setValue(int initial) {
            element.setValue(initial);
            return this;
        }
        public Builder setName(Text name) {
            element.defaultName = name;
            return this;
        }
        public Builder setLore(List<Text> lore) {
            element.defaultLore.clear();
            element.defaultLore.addAll(lore);
            return this;
        }
        public Builder setOnClickListener(OnClickListener listener) {
            element.clickListener = listener;
            return this;
        }
        public Builder setOnChangeListener(OnChangeListener<Integer> listener) {
            element.changeListener = listener;
            return this;
        }

        public MCheckbox build() {
            MCheckbox copy = element.copy();
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion


    @SuppressWarnings("unchecked")
    @Override
    public MCheckbox copy() {
        MCheckbox copy = new MCheckbox();
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultLore = new LinkedList<>(defaultLore);
        copy.clickListener = clickListener;
        copy.changeListener = changeListener;
        return copy;
    }
}
