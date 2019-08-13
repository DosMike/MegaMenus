package de.dosmike.sponge.megamenus.impl;

import com.google.common.collect.ImmutableSet;
import de.dosmike.sponge.megamenus.AntiGlitch;
import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.listener.OnRenderStateListener;
import de.dosmike.sponge.megamenus.impl.util.MenuUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * Base Implementation for {@link MenuRenderer}
 * @see MenuRenderer
 */
public abstract class AbstractMenuRenderer implements MenuRenderer {

    protected Set<Player> viewers = new HashSet<>();
    protected boolean valid = true;
    protected IMenu menu;
    /** flag to notify rendering that closing this menu was done via API, and thus
     * shall not lead in a anti-glitch trigger */
    protected Set<UUID> apiClose = new HashSet<>();

    /**
     * Constructor to set parent
     */
    public AbstractMenuRenderer(IMenu menu) {
        this.menu = menu;
    }

    @Override
    public IMenu getMenu() {
        return menu;
    }

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

    /**
     * Refresh/redraw this menu for all current viewers
     */
    synchronized void render() {
        if (!valid) {
            viewers.forEach(this::render);
            valid = true;
        }
    }
    /**
     * Invokes this renderer to refresh/redraw the menu for this viewer
     * @param viewer the player to redraw this menu for
     */
    abstract void render(Player viewer);

    @Override
    public Set<Player> getViewers() {
        return ImmutableSet.copyOf(viewers);
    }

    @Override
    public boolean hasViewers() {
        return !viewers.isEmpty();
    }

    private MenuRenderer parent = null;

    @Override
    public synchronized void open(Player viewer) {
        open(viewer, false);
    }
    @Override
    public synchronized void open(Player viewer, boolean doparent) {
        apiClose.remove(viewer.getUniqueId());
        if (AntiGlitch.isGlitcher(viewer)) {
            viewer.sendMessage(Text.of(TextColors.RED, "You've triggered the anti glitch system. Notify an admin to pardon you for mega menus or wait until the server restarts."));
            MenuUtil.closeInventory(viewer);
            return;
        }
        //if coming from a child this will be absent since the v-- render removed this viewer
        Optional<MenuRenderer> oldRender = RenderManager.getRenderFor(viewer).filter(r->r!=this);

        RenderManager.register(this);
        render(viewer);

        oldRender.ifPresent(previous->{
            previous.closeSilent(viewer);
            parent = doparent?previous:null;
        });
        viewers.add(viewer);

        if (renderListener != null)
            renderListener.opened(this, menu, viewer);
    }

    @Override
    public synchronized void close(Player viewer) {
        apiClose.add(viewer.getUniqueId());
        //notify listeners
        if (renderListener != null && viewers.contains(viewer))
            if (renderListener.closed(this, menu, viewer))
                return; //custom parent redirect
        //close/delegate inventory
        if (parent != null) {
            Task.builder().delayTicks(1).execute(()->{
                parent.open(viewer, false); //open parent view if present
            }).submit(MegaMenus.getInstance());
        } else {
            viewers.remove(viewer);
            MenuUtil.closeInventory(viewer);
        }
    }
    @Override
    public synchronized void closeSilent(Player viewer) {
        apiClose.add(viewer.getUniqueId());
        viewers.remove(viewer);
        if (renderListener != null && viewers.contains(viewer)) {
            renderListener.closed(this, menu, viewer);
        }
    }

    @Override
    public synchronized void closeAll() {
        List<Player> copy = new LinkedList<>(viewers);
        for (Player viewer : copy) {
            close(viewer);
        }
    }

    public boolean isClosedByAPI(Player player) {
        return apiClose.contains(player.getUniqueId());
    }

    @Override
    public synchronized void think(AnimationManager animations) {
        List<IElement> elements = new LinkedList<>();
        for (int i = 1; i <= menu.pages(); i++) {
            elements.addAll(menu.getPageElements(i));
        }
        //now rendering can do anything to the menu itself without cme
        boolean changed=false;
        for (IElement e : elements) {
            for (Player p : viewers) {
                changed |= e.think(animations, p);
            }
        }
        if (menu.getBackground() != null)
            changed |= animations.singleTick(menu.getBackground());
        if (renderListener != null)
            changed |= renderListener.tick(animations.getDeltaTime(), this, menu);
        if (changed)
            valid = false;
    }

    private OnRenderStateListener renderListener = null;
    @Override
    public void setRenderListener(OnRenderStateListener listener) {
        renderListener = listener;
    }
    @Override
    public OnRenderStateListener getRenderListener() {
        return renderListener;
    }

}
