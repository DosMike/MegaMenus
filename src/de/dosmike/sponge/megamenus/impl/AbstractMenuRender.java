package de.dosmike.sponge.megamenus.impl;

import com.google.common.collect.ImmutableSet;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRender;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.listener.OnRenderStateChangeListener;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbstractMenuRender implements MenuRender {

    protected Set<Player> viewers = new HashSet<>();
    protected boolean valid = true;
    protected IMenu menu;

    public AbstractMenuRender(IMenu menu) {
        this.menu = menu;
        RenderManager.register(this);
    }

    @Override
    public IMenu getMenu() {
        return menu;
    }

    /** will cause this menu to redraw in the near future */
    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public void revalidate() {
        if (!valid) {
            render();
        }
    }

    void render() {
        if (!valid) {
            viewers.forEach(this::render);
            valid = true;
        }
    }
    abstract void render(Player viewer);

    @Override
    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

    @Override
    public boolean hasViewers() {
        return !viewers.isEmpty();
    }

    @Override
    public void open(Player viewer) {
        RenderManager.register(this);
        viewers.add(viewer);
        if (renderListener != null)
            renderListener.opened(this, menu, viewer);
        render(viewer);
    }

    @Override
    public void close(Player viewer) {
        if (viewers.remove(viewer)) {
            //close inventory
            viewer.closeInventory();
            if (renderListener != null)
                renderListener.closed(this, menu, viewer);
        }
    }
    /** to be called from inventory events - won't close the actual inventory, but will untrack this player*/
    public void closeSilent(Player viewer) {
       if (viewers.remove(viewer)) {
           if (renderListener != null)
               renderListener.closed(this, menu, viewer);
        }
    }

    @Override
    public void closeAll() {
        List<Player> copy = new LinkedList<>(viewers);
        for (Player viewer : copy) {
            close(viewer);
        }
    }

    @Override
    public void think(AnimationManager animations) {
        List<IElement> elements = new LinkedList<>();
        for (int i = 1; i <= menu.pages(); i++) {
            elements.addAll(menu.getPageElements(i));
        }
        //now rendering can do anything to the menu itself without cme
        boolean changed=false;
        for (IElement e : elements) {
            for (Player p : viewers) {
                changed |= e.think(animations, menu.getState(), menu.getPlayerState(p.getUniqueId()));
            }
        }
        if (changed) valid = false;
    }

    OnRenderStateChangeListener renderListener;
    @Override
    public void setRenderListener(OnRenderStateChangeListener listener) {
        renderListener = listener;
    }
    @Override
    public OnRenderStateChangeListener getRenderListener() {
        return renderListener;
    }

}
