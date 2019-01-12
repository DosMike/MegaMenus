package de.dosmike.sponge.megamenus.impl.elements;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.state.StateObject;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import de.dosmike.sponge.megamenus.exception.ObjectBuilderException;
import de.dosmike.sponge.megamenus.impl.AnimationManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.item.inventory.property.SlotPos;

import java.util.UUID;

public abstract class IElementImpl implements IElement {

    private SlotPos pos = new SlotPos(0,0);
    private IMenu parent;
    protected UUID uiid = UUID.randomUUID();

    @Override
    public UUID getUniqueId() {
        return uiid;
    }

    @Override
    public SlotPos getPosition() {
        return pos;
    }

    @Override
    public void setPosition(@NotNull SlotPos position) {
        this.pos = position;
    }

    public void setParent(IMenu menu) {
        if (this.parent != null)
            throw new ObjectBuilderException("This Element is already bound to a menu");
        this.parent = menu;
    }

    @Override
    public IMenu getParent() {
        return parent;
    }

    @Override
    public void validateGui(int pageHeight) throws ObjectBuilderException {
        if (pos.getX()<0 || pos.getX()>=9 ||
            pos.getY()<0 || pos.getY()>pageHeight-1 ||
            (parent.pages()>1 && pos.getY()==pageHeight-1 && //if paginated the last row's center elements are off limits
                pos.getX()>=4 && pos.getX() <= 6)) {
            throw new ObjectBuilderException("Element outside of visible area");
        }
    }

    private Tickable thinkHook = null;
    public void hookThinkTick(Tickable hook) {
        this.thinkHook = hook;
    }

    @Override
    public boolean think(AnimationManager animations, StateObject menuState, StateObject viewerState) {
        boolean hookChange = (thinkHook != null) && animations.singleTick(thinkHook);
        IIcon icon = getIcon(menuState, viewerState);
        return (icon != null && animations.singleTick(icon)) || hookChange;
    }
}
