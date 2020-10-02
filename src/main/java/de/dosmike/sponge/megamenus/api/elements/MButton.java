package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IPressable;
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
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This element acts like a button, it can be clicked at and performs an action.
 */
final public class MButton extends IElementImpl implements IClickable<MButton>, IPressable<MButton> {

    private IIcon defaultIcon = null;
    private OnClickListener<MButton> clickListener = null;
    private OnKeyListener<MButton> keyListener = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

    @Override
    public OnClickListener<MButton> getOnClickListener() {
        return clickListener;
    }

    @Override
    public void fireClickEvent(Player viewer, int button, boolean shift) {
        if (clickListener != null)
            clickListener.onClick(this, viewer, button, shift);
    }

    @Override
    public void setOnClickListener(OnClickListener<MButton> listener) {
        clickListener = listener;
    }

    @Override
    public OnKeyListener<MButton> getOnKeyListener() {
        return keyListener;
    }

    @Override
    public void fireKeyEvent(Player viewer, Buttons key, boolean ctrl) {
        if (keyListener != null)
            keyListener.onKeyPress(this, viewer, key, ctrl);
    }

    @Override
    public void setOnKeyListener(OnKeyListener<MButton> listener) {
        keyListener = listener;
    }

    @Override
    public IIcon getIcon(Player viewer) {
        return defaultIcon;
    }

    @Override
    public Text getName(Player viewer) {
        return defaultName;
    }

    @Override
    public List<Text> getLore(Player viewer) {
        return defaultLore;
    }

    /**
     * set the icon for this element
     * @param icon the new {@link IIcon} to display
     */
    public void setIcon(IIcon icon) {
        defaultIcon = icon;
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

    public MButton() {}

    //Region builder
    public static class Builder {
        MButton element = new MButton();
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

        public Builder setOnClickListener(OnClickListener<MButton> listener) {
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
        copy.setParent(getParent());
        copy.pos = pos!=null?new SlotPos(pos.getX(), pos.getY()):null;
        copy.defaultName = defaultName;
        copy.defaultIcon = defaultIcon;
        copy.defaultLore = new LinkedList<>(defaultLore);
        copy.clickListener = clickListener;
        return copy;
    }
}
