package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a raw element that has no function other than drawing an icon with text.<br>
 * Other than MLabel this will render as {@link ItemStack} on hover in a Text UI.<br>
 * For GUI renders it behaves exactly like an {@link MLabel}
 */
final public class MIcon extends IElementImpl {

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

    public MIcon() {}

    //Region builder
    public static class Builder {
        MIcon element = new MIcon();
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

        public MIcon build() {
            MIcon copy = element.copy();
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion


    @SuppressWarnings("unchecked")
    @Override
    public MIcon copy() {
        MIcon copy = new MIcon();
        copy.setPosition(getPosition());
        copy.setParent(getParent());
        copy.defaultName = defaultName;
        copy.defaultIcon = defaultIcon;
        copy.defaultLore = new LinkedList<>(defaultLore);
        return copy;
    }
}
