package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IValueChangeable;
import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.TextMenuRenderer;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This element displays a tri-state that toggles when clicking on it.
 * A Player can only toggle between true and false.
 */
final public class MCheckbox extends IElementImpl implements IClickable<MCheckbox>, IValueChangeable<Integer, MCheckbox> {

    private List<IIcon> icons = Arrays.asList(
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.GRAY).build()),
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.MAGENTA).build()),
            IIcon.of(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.LIME).build())
    );
    private int value =0;
    private OnClickListener<MCheckbox> clickListener = null;
    private OnChangeListener<Integer, MCheckbox> changeListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

    @Override
    public void fireClickEvent(Player viewer, int button, boolean shift) {
        int pre = getValue();
        setValue(pre == 1 ? 0 : 1);
        invalidate(viewer);
        if (clickListener!=null) {
            clickListener.onClick(this, viewer, button, shift);
        }
        fireChangeListener(viewer, pre, getValue());
    }
    @Override
    public void fireChangeListener(Player viewer, Integer oldValue, Integer newValue) {
        if (changeListener!=null)
            changeListener.onValueChange(oldValue, newValue, this, viewer);
    }

    /**
     * Values: 1 selected, 0 not selected, -1 tri-state
     * @return the current value for this element
     */
    public int getValue() {
        return value;
    }

    /**
     * Values: 1 selected, 0 not selected, -1 tri-state<br>
     * Changes the value without notifying listener
     * @param value the new value for this element
     */
    public void setValue(int value) {
        if (value < -1 || value > 1)
            throw new IllegalArgumentException("Checkbox value out of range (-1,0,1)");
        this.value = value;
    }

    @Override
    public OnClickListener<MCheckbox> getOnClickListerner() {
        return clickListener;
    }

    @Override
    public OnChangeListener<Integer, MCheckbox> getOnChangeListener() {
        return changeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener<MCheckbox> listener) {
        clickListener = listener;
    }

    @Override
    public IIcon getIcon(Player viewer) {
        return icons.get(value+1);
    }

    @Override
    public Text getName(Player viewer) {
        return defaultName;
    }

    /**
     * Highlights the line with the same position ans the selected value if the number of lines
     * in this lore equals the number of possible values. Otherwise the set lore is returned unmodified.
     */
    @Override
    public List<Text> getLore(Player viewer) {
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

    /**
     * set the name for this element
     * @param name the new display value for this element
     */
    public void setName(Text name) {
        defaultName = name;
    }
    /**
     * set the lore for this element
     * @param lore a list containing the lines in the item lore
     */
    public void setLore(List<Text> lore) {
        defaultLore = new LinkedList<>(lore);
    }

    @Override
    public void setOnChangeListener(OnChangeListener<Integer, MCheckbox> listener) {
        changeListener = listener;
    }

    public MCheckbox() {}

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
        public Builder setOnClickListener(OnClickListener<MCheckbox> listener) {
            element.clickListener = listener;
            return this;
        }
        public Builder setOnChangeListener(OnChangeListener<Integer, MCheckbox> listener) {
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

    @Override
    public Text renderTUI(Player viewer) {
        IIcon icon = getIcon(viewer);
        List<Text> lore = getLore(viewer);
        Text display = getName(viewer);
        int val = getValue();
        display = Text.builder()
                .append(Text.of("["+(val==1?"\u2611":(val==0?"\u2612":"\u2610"))+"] ",display))
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
                        .ifPresent(r->((TextMenuRenderer)r).delegateClickEvent(MCheckbox.this, (Player)src));
            }))
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MCheckbox copy() {
        MCheckbox copy = new MCheckbox();
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.value = value;
        copy.defaultName = defaultName;
        copy.defaultLore = new LinkedList<>(defaultLore);
        copy.clickListener = clickListener;
        copy.changeListener = changeListener;
        return copy;
    }
}
