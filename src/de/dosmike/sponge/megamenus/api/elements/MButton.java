package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;

/** This is a menu element that behaves like a regular inventory slot.
 * Depending on some rules this slot may ba taken by the player or something may be put
 * into this slot.<br>
 * In Book UIs this element can not be interacted with. */
public class MButton extends IElementImpl implements IClickable {

    private IIcon defaultIcon = null;
    private OnClickListener clickListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

    /**
     * Invoking this manually will cause the cyclic element to progress and call the change listener as well
     */
    @Override
    public OnClickListener getOnClickListerner() {
        return clickListener;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public IIcon getIcon(StateObject menuState, StateObject viewerState) {
        return defaultIcon;
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
        return defaultLore;
    }

    private MButton() {}

    //Region builder
    public static class Builder {
        MButton element = new MButton();
        private Builder() {
        }

        public Builder setPosition(SlotPos position) {
            element.setPosition(position);
            return this;
        }

        public Builder setIcon(IIcon icon) {
            element.defaultIcon = icon;
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

        public MButton build() {
            MButton copy = element.copy();
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion


    @SuppressWarnings("unchecked")
    @Override
    public MButton copy() {
        MButton copy = new MButton();
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultIcon = defaultIcon;
        copy.defaultLore = new LinkedList<>(defaultLore);
        copy.clickListener = clickListener;
        return copy;
    }
}
