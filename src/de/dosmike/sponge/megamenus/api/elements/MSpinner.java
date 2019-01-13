package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.LinkedList;
import java.util.List;

/** This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with. */
final public class MSpinner extends IElementImpl implements IClickable, IValueChangeable<Integer> {

    private List<IIcon> icons = new LinkedList<>();
    private int index=0;
    private OnClickListener clickListener = null;
    private OnChangeListener<Integer> changeListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

    /** performs the internal progress fo the cyclic element and calls external listener */
    private OnClickListener internalClickListener = (e, v, l) -> {
        int pre = getValue();
        setValue(pre >= maxValue() ? 0 : (pre+1));
        RenderManager.getRenderFor(v).ifPresent(MenuRenderer::invalidate);
        if (clickListener!=null) {
            clickListener.onClick(e, v, l);
        }
        if (changeListener!=null)
            changeListener.onValueChange(pre, getValue(), MSpinner.this, v);
    };

    public int getValue() {
        return index;
    }

    public int maxValue() {
        return icons.size()-1;
    }

    public void setValue(int value) {
        if (value < 0 || value >= icons.size())
            throw new IllegalArgumentException("Cyclic value out of range (0.."+icons.size());
        index = value;
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
        return icons.get(index);
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
        if (defaultLore.size() != icons.size())
            return defaultLore;
        else {
            List<Text> coloredCopy = new LinkedList<>();
            for (int i = 0; i< defaultLore.size(); i++)
                coloredCopy.add(Text.of(index == i
                        ? TextColors.YELLOW
                        : TextColors.DARK_GRAY,
                        defaultLore.get(i).toPlain()));
            return coloredCopy;
        }
    }

    /** set the name for this element */
    public void setName(Text name) {
        defaultName = name;
    }
    /** set the lore for this element */
    public void setLore(List<Text> lore) {
        defaultLore = new LinkedList<>(lore);
    }

    @Override
    public void setOnChangeListener(OnChangeListener<Integer> listener) {
        changeListener = listener;
    }

    public MSpinner() {}

    //Region builder
    public static class Builder {
        MSpinner element = new MSpinner();
        private Builder() {
        }

        public Builder setPosition(SlotPos position) {
            element.setPosition(position);
            return this;
        }

        public Builder addValue(IIcon icon) {
            element.icons.add(icon);
            return this;
        }
        public Builder addValue(ItemStackSnapshot icon) {
            element.icons.add(IIcon.of(icon));
            return this;
        }
        public Builder addValue(ItemStack icon) {
            element.icons.add(IIcon.of(icon));
            return this;
        }
        public Builder addValue(ItemType icon) {
            element.icons.add(IIcon.of(icon));
            return this;
        }
        public Builder clearValues() {
            element.icons.clear();
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

        public MSpinner build() {
            MSpinner copy = element.copy();
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion


    @SuppressWarnings("unchecked")
    @Override
    public MSpinner copy() {
        MSpinner copy = new MSpinner();
        copy.icons.addAll(icons);
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultLore = new LinkedList<>(defaultLore);
        copy.clickListener = clickListener;
        copy.changeListener = changeListener;
        return copy;
    }
}
