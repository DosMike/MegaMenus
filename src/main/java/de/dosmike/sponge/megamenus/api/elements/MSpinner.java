package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IPressable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.api.listener.OnKeyListener;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.TextMenuRenderer;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.jetbrains.annotations.Nullable;
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

/**
 * This element displays a cyclic value. Compared to the checkbox this can have more values
 * and custom icons (icons won't display in text UIs)
 */
final public class MSpinner extends IElementImpl implements IClickable<MSpinner>, IPressable<MSpinner>, IValueChangeable<Text, MSpinner> {

    private int index=0;
    private OnClickListener<MSpinner> clickListener = null;
    private OnKeyListener<MSpinner> keyListener = null;
    private OnChangeListener<Text, MSpinner> changeListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<IIcon> defaultIcons = new LinkedList<>();
    private List<Text> defaultValues = new LinkedList<>();

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
    private OnClickListener<MSpinner> internalClickListener = (e, v, b, s) -> {
        Text oldValue = MSpinner.this.getValue();
        if (b == MouseEvent.BUTTON1) {
            int i = MSpinner.this.getSelectedIndex();
            MSpinner.this.setSelectedIndex(i >= MSpinner.this.getMaximumIndex() ? 0 : (i + 1));
            RenderManager.getRenderFor(v).ifPresent(MenuRenderer::invalidate);
        } else if (b == MouseEvent.BUTTON2) {
            int i = MSpinner.this.getSelectedIndex();
            MSpinner.this.setSelectedIndex(i <= 0 ? MSpinner.this.getMaximumIndex() : (i - 1));
            RenderManager.getRenderFor(v).ifPresent(MenuRenderer::invalidate);
        }
        if (clickListener != null) {
            clickListener.onClick(e, v, b, s);
        }
        if (changeListener != null)
            changeListener.onValueChange(oldValue, MSpinner.this.getValue(), MSpinner.this, v);
    };

    @Override
    public void fireKeyEvent(Player viewer, Buttons key, boolean ctrl) {
        if (keyListener != null)
            keyListener.onKeyPress(this, viewer, key, ctrl);
    }

    /**
     * The currently selected index in this spinner as offset to the first value
     * @return the current value
     */
    public int getSelectedIndex() {
        return index;
    }

    /**
     * @return the maximum index as amount of values - 1
     */
    public int getMaximumIndex() {
        return defaultIcons.size()-1;
    }

    /**
     * Updates the spinners current index without invoking events
     * @param value the new index to display
     */
    public void setSelectedIndex(int value) {
        if (value < 0 || value >= defaultIcons.size())
            throw new IllegalArgumentException("Cyclic value out of range (0.."+ defaultIcons.size());
        index = value;
    }

    /**
     * @return the {@link Text} value for the currently selected index
     */
    public Text getValue() {
        return defaultValues.get(getSelectedIndex());
    }
    /**
     * Peeks the next {@link Text} value in the cycle
     * @return the value that will be displayed after the current one
     */
    public Text getNextValue() {
        int i = getSelectedIndex()+1;
        return defaultValues.get(i>getMaximumIndex()?0:i);
    }
    /**
     * Peeks the previous {@link Text} value in the cycle
     * @return the value that was displayed prior to the current one
     */
    public Text getPreviousValue() {
        int i = getSelectedIndex()-1;
        return defaultValues.get(i<0?getMaximumIndex():i);
    }

    @Override
    public OnClickListener<MSpinner> getOnClickListener() {
        return internalClickListener;
    }

    @Override
    public OnChangeListener<Text, MSpinner> getOnChangeListener() {
        return changeListener;
    }

    @Override
    public OnKeyListener<MSpinner> getOnKeyListener() {
        return keyListener;
    }

    @Override
    public void setOnClickListener(OnClickListener<MSpinner> listener) {
        clickListener = listener;
    }

    @Override
    public void setOnChangeListener(OnChangeListener<Text, MSpinner> listener) {
        changeListener = listener;
    }

    @Override
    public void setOnKeyListener(OnKeyListener<MSpinner> listener) {
        keyListener = listener;
    }

    @Override
    public IIcon getIcon(Player viewer) {
        return defaultIcons.get(index);
    }

    @Override
    public Text getName(Player viewer) {
        return defaultName;
    }

    /**
     * The default implementation highlights the line with the same position ans the selected value
     * if the number of lines in this lore equals the number of possible values. Otherwise the set lore
     * is returned unmodified.
     * If you do not want this to style your lore overwrite with <code>return defaultValues;</code>
     */
    @Override
    public List<Text> getLore(Player viewer) {
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

    /**
     * set the name for this element
     * @param name the new display value for this element
     */
    public void setName(Text name) {
        defaultName = name;
    }
    /**
     * set the lore for this element.<br>
     * The number of element should match the amount of icons as the lines double as values
     * @param lore a list containing the lines in the item lore
     */
    public void setLore(List<Text> lore) {
        defaultValues = new LinkedList<>(lore);
    }

    public MSpinner() {}

    //Region builder
    public static class Builder {
        MSpinner element = new MSpinner();
        private Builder() {
        }

        /**
         * Not providing a position or setting the position to null
         * will query a position from the menus {@link PositionProvider}
         * at the moment the element is added to the menu.
         * @param position where this element is supposed to go or null for auto
         */
        public Builder setPosition(@Nullable SlotPos position) {
            element.pos = position;
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
        public Builder setOnClickListener(OnClickListener<MSpinner> listener) {
            element.clickListener = listener;
            return this;
        }
        public Builder setOnChangeListener(OnChangeListener<Text, MSpinner> listener) {
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
    public Text renderTUI(Player viewer) {
        IIcon icon = getIcon(viewer);
        List<Text> lore = getLore(viewer);
        Text display = getName(viewer);
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
                            Text.joinWith(Text.of(Text.NEW_LINE), getLore(viewer))
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
        copy.setParent(getParent());
        copy.pos = pos!=null?new SlotPos(pos.getX(), pos.getY()):null;
        copy.defaultName = defaultName;
        copy.defaultValues = new LinkedList<>(defaultValues);
        copy.clickListener = clickListener;
        copy.changeListener = changeListener;
        return copy;
    }
}
