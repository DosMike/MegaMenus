package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a raw element that has no function other than drawing an icon with text.<br>
 * This is a special element for Text UIs that won't render as {@link ItemStack} on hover.<br>
 * For GUI renders it behaves exactly like an {@link MIcon}
 */
final public class MLabel extends IElementImpl {

    private IIcon defaultIcon = null;
    private Text defaultName = Text.of(getClass().getSimpleName());
    private List<Text> defaultLore = new LinkedList<>();

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

    public MLabel() {}

    //Region builder
    public static class Builder {
        MLabel element = new MLabel();
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

        public MLabel build() {
            MLabel copy = element.copy();
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion


    @Override
    public Text renderTUI(Player viewer) {
        if (defaultLore.isEmpty())
            return defaultName;
        else
            return Text.builder()
                    .append(defaultName)
                    .onHover(TextActions.showText(Text.joinWith(Text.NEW_LINE, defaultLore)))
                    .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MLabel copy() {
        MLabel copy = new MLabel();
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultIcon = defaultIcon;
        copy.defaultLore = new LinkedList<>(defaultLore);
        return copy;
    }
}
