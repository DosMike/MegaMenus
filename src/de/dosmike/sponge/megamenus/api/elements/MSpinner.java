package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.TextMenuRenderer;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** This element displays a cyclic value. Compared to the checkbox this can have more values
 * and custom icons (icons won't display in text UIs) */
final public class MSpinner extends IElementImpl implements IClickable, IValueChangeable<Text> {

    private int index=0;
    private OnClickListener clickListener = null;
    private OnChangeListener<Text> changeListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<IIcon> defaultIcons = new LinkedList<>();
    private List<Text> defaultValues = new LinkedList<>();

    /** performs the internal progress fo the cyclic element and calls external listener */
    @Override
    public void fireClickEvent(Player viewer, int button, boolean shift) {
        Text oldValue = getValue();
        if (button==MouseEvent.BUTTON1) {
            int i = getSelectedIndex();
            setSelectedIndex(i >= getMaximumIndex() ? 0 : (i + 1));
            invalidate(viewer);
        } else if (button==MouseEvent.BUTTON2) {
            int i = getSelectedIndex();
            setSelectedIndex(i <= 0 ? getMaximumIndex() : (i - 1));
            invalidate(viewer);
        }
        if (clickListener != null) {
            clickListener.onClick(this, viewer, button, shift);
        }
        fireChangeListener(viewer, oldValue, getValue());
    }
    @Override
    public void fireChangeListener(Player viewer, Text oldValue, Text newValue) {
        if (changeListener!=null)
            changeListener.onValueChange(oldValue, newValue, this, viewer);
    }
    private OnClickListener internalClickListener = (e, v, b, s) -> {
        Text oldValue = getValue();
        if (b==MouseEvent.BUTTON1) {
            int i = getSelectedIndex();
            setSelectedIndex(i >= getMaximumIndex() ? 0 : (i + 1));
            RenderManager.getRenderFor(v).ifPresent(MenuRenderer::invalidate);
        } else if (b==MouseEvent.BUTTON2) {
            int i = getSelectedIndex();
            setSelectedIndex(i <= 0 ? getMaximumIndex() : (i - 1));
            RenderManager.getRenderFor(v).ifPresent(MenuRenderer::invalidate);
        }
        if (clickListener != null) {
            clickListener.onClick(e, v, b, s);
        }
        if (changeListener != null)
            changeListener.onValueChange(oldValue, getValue(), MSpinner.this, v);
    };

    public int getSelectedIndex() {
        return index;
    }

    public int getMaximumIndex() {
        return defaultIcons.size()-1;
    }

    public void setSelectedIndex(int value) {
        if (value < 0 || value >= defaultIcons.size())
            throw new IllegalArgumentException("Cyclic value out of range (0.."+ defaultIcons.size());
        index = value;
    }

    public Text getValue() {
        return defaultValues.get(getSelectedIndex());
    }
    public Text getNextValue() {
        int i = getSelectedIndex()+1;
        return defaultValues.get(i>getMaximumIndex()?0:i);
    }
    public Text getPreviousValue() {
        int i = getSelectedIndex()-1;
        return defaultValues.get(i<0?getMaximumIndex():i);
    }

    @Override
    public OnClickListener getOnClickListerner() {
        return internalClickListener;
    }

    @Override
    public OnChangeListener<Text> getOnChangeListener() {
        return changeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public IIcon getIcon(StateObject menuState, StateObject viewerState) {
        return defaultIcons.get(index);
    }

    @Override
    public Text getName(StateObject menuState, StateObject viewerState) {
        return defaultName;
    }

    /**
     * The default implementation highlights the line with the same position ans the selected value
     * if the number of lines in this lore equals the number of possible values. Otherwise the set lore
     * is returned unmodified.
     * If you do not want this to style your lore overwrite with <code>return defaultValues;</code>
     */
    @Override
    public List<Text> getLore(StateObject menuState, StateObject viewerState) {
        if (defaultValues.size() != defaultIcons.size())
            return defaultValues;
        else {
            List<Text> coloredCopy = new LinkedList<>();
            for (int i = 0; i< defaultValues.size(); i++)
                coloredCopy.add(Text.of(index == i
                        ? TextColors.YELLOW
                        : TextColors.DARK_GRAY,
                        defaultValues.get(i).toPlain()));
            return coloredCopy;
        }
    }

    /** set the name for this element */
    public void setName(Text name) {
        defaultName = name;
    }
    /** set the lore for this element */
    public void setLore(List<Text> lore) {
        defaultValues = new LinkedList<>(lore);
    }

    @Override
    public void setOnChangeListener(OnChangeListener<Text> listener) {
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

        public Builder addValue(IIcon icon, Text name) {
            element.defaultIcons.add(icon);
            element.defaultValues.add(name);
            return this;
        }
        public Builder addValue(ItemStackSnapshot icon, Text name) {
            element.defaultIcons.add(IIcon.of(icon));
            element.defaultValues.add(name);
            return this;
        }
        public Builder addValue(ItemStack icon, Text name) {
            element.defaultIcons.add(IIcon.of(icon));
            element.defaultValues.add(name);
            return this;
        }
        public Builder addValue(ItemType icon, Text name) {
            element.defaultIcons.add(IIcon.of(icon));
            element.defaultValues.add(name);
            return this;
        }
        public Builder clearValues() {
            element.defaultIcons.clear();
            element.defaultValues.clear();
            return this;
        }
        public Builder setName(Text name) {
            element.defaultName = name;
            return this;
        }
        public Builder setOnClickListener(OnClickListener listener) {
            element.clickListener = listener;
            return this;
        }
        public Builder setOnChangeListener(OnChangeListener<Text> listener) {
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

    @Override
    public Text renderTUI(StateObject menuState, StateObject viewerState, Player viewer) {
        IIcon icon = getIcon(menuState, viewerState);
        List<Text> lore = getLore(menuState, viewerState);
        Text display = getName(menuState, viewerState);
        display = Text.builder()
                .append(Text.of("► ",display))
                .style(TextStyles.of(TextStyles.ITALIC))
                .build();
        if (lore.isEmpty()) {
            return display;
        } else {
            List<Text> sublore = lore.size()>1 ? lore.subList(1,lore.size()) : Collections.EMPTY_LIST;
            return Text.builder().append(display).onHover(
                    icon != null
                        ? TextActions.showItem(ItemStack.builder().fromSnapshot(icon.render())
                            .add(Keys.DISPLAY_NAME, lore.get(0))
                            .add(Keys.ITEM_LORE, sublore)
                            .build().createSnapshot())
                        : TextActions.showText(Text.of(
                            Text.joinWith(Text.of(Text.NEW_LINE), getLore(menuState, viewerState))
                    ))
            ).onClick(TextActions.executeCallback((src)->{
                RenderManager.getRenderFor((Player)src)
                        .filter(r->(r instanceof TextMenuRenderer))
                        .ifPresent(r->((TextMenuRenderer)r).delegateClickEvent(MSpinner.this, (Player)src));
            }))
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MSpinner copy() {
        MSpinner copy = new MSpinner();
        copy.defaultIcons.addAll(defaultIcons);
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultValues = new LinkedList<>(defaultValues);
        copy.clickListener = clickListener;
        copy.changeListener = changeListener;
        return copy;
    }
}
