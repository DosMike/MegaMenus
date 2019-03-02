package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
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
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** This element acts like a button, it can be clicked at and performs an action. */
final public class MButton extends IElementImpl implements IClickable {

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
    public void fireClickEvent(Player viewer, int button, boolean shift) {
        if (clickListener != null)
            clickListener.onClick(this, viewer, button, shift);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public IIcon getIcon(Player viewer) {
        return defaultIcon;
    }

    @Override
    public Text getName(Player viewer) {
        return defaultName;
    }

    /**
     * The default implementation highlights the line with the same position ans the selected value
     * if the number of lines in this lore equals the number of possible values. Otherwise the set lore
     * is returned unmodified.
     * If you do not want this to style your lore overwrite with <code>return defaultLore;</code>
     */
    @Override
    public List<Text> getLore(Player viewer) {
        return defaultLore;
    }

    /** set the icon for this element */
    public void setIcon(IIcon icon) {
        defaultIcon = icon;
    }
    /** set the name for this element */
    public void setName(Text name) {
        defaultName = name;
    }
    /** set the lore for this element */
    public void setLore(List<Text> lore) {
        defaultLore = new LinkedList<>(lore);
    }

    public MButton() {}

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
        public Builder setIcon(ItemStackSnapshot icon) {
            element.defaultIcon = IIcon.of(icon);
            return this;
        }
        public Builder setIcon(ItemStack icon) {
            element.defaultIcon = IIcon.of(icon);
            return this;
        }
        public Builder setIcon(ItemType icon) {
            element.defaultIcon = IIcon.of(icon);
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

    @Override
    public Text renderTUI(Player viewer) {
        IIcon icon = getIcon(viewer);
        List<Text> lore = getLore(viewer);
        Text display = getName(viewer);
        display = Text.builder()
                .append(display)
                .style(TextStyles.of(TextStyles.ITALIC, TextStyles.UNDERLINE))
                .build();
        if (lore.isEmpty()) {
            return Text.builder().append(display)
            .onClick(TextActions.executeCallback((src)->{
                RenderManager.getRenderFor((Player)src)
                        .filter(r->(r instanceof TextMenuRenderer))
                        .ifPresent(r->((TextMenuRenderer)r).delegateClickEvent(MButton.this, (Player)src));
            }))
            .build();
        } else {
            List<Text> sublore = lore.size()>1 ? lore.subList(1,lore.size()) : Collections.EMPTY_LIST;
            return Text.builder().append(display).onHover(
                    icon != null
                    ? TextActions.showItem(ItemStack.builder().fromSnapshot(icon.render())
                            .add(Keys.DISPLAY_NAME, lore.get(0))
                            .add(Keys.ITEM_LORE, sublore)
                            .build().createSnapshot())
                    : TextActions.showText(Text.of(
                            Text.joinWith(Text.of(Text.NEW_LINE), lore)
                    ))
            ).onClick(TextActions.executeCallback((src)->{
                RenderManager.getRenderFor((Player)src)
                        .filter(r->(r instanceof TextMenuRenderer))
                        .ifPresent(r->((TextMenuRenderer)r).delegateClickEvent(MButton.this, (Player)src));
            }))
            .build();
        }
    }

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
