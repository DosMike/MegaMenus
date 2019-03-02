package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** this map tries to keep book of all active MenuRenderer instances */
final public class RenderManager {

    private static AnimationManager animations = new AnimationManager();
    private static Set<MenuRenderer> renders = new HashSet<>();
    public static void register(MenuRenderer menuRenderer) {
        if (renders.add(menuRenderer)) {
            if (renders.size() == 1) //currently first and only render
                animations.finishTick(); //reset animation timer
            if (menuRenderer.getRenderListener() != null)
                menuRenderer.getRenderListener().resumed(menuRenderer, menuRenderer.getMenu());
        }
    }
    public static Optional<MenuRenderer> getRenderFor(Player viewer) {
        for (MenuRenderer r : renders)
            if (r.getViewers().contains(viewer))
                return Optional.of(r);
        return Optional.empty();
    }
    /** this might return a render for each viewer or one render for all viewers depending on
     * whether the menu is implemented as shared menu or single viewer menu.
     * @return all renderer, where the rendered menu equals the passed menu, or
     *         (where the rendered menu is a bound instance) the passed menu is the base menu */
    public static Collection<MenuRenderer> getRenderFor(IMenu menu) {
        return renders.stream().filter(r->
                r.getMenu().equals(menu) ||
                ((r instanceof BoundMenuImpl) && ((BoundMenuImpl) r).getBaseMenu().equals(menu))
        ).collect(Collectors.toSet());
    }
    /** When a player changes the target render, they have to be kicked out of all other renders.
     * This will use closeSilent in order to prevent the new targetRender from closing. */
    public static void notifyRenderChange(Player viewer, MenuRenderer targetRender) {
        renders.stream()
                .filter(r->!r.equals(targetRender))
                .forEach(r->r.closeSilent(viewer));
    }
    /** since InteractInventoryEvent.Close is only fired if the PLAYER closes the inventory
     * we need to untrack closing the inventory through monitoring other events and calling this.<br>
     * Note that it is assumed that the calling event is expected to require the menu to
     * already be closed */
    public static void kickFromAll(Player viewer) {
        renders.forEach(r->r.closeSilent(viewer));
    }

    /** this method will invoke automatic refreshing for all menus with animated elements */
    public static void tickRendering() {
        //remove stubbed renders
        renders.removeIf(r->{
            if (!r.hasViewers()) {
                if (r.getRenderListener() != null)
                    r.getRenderListener().paused(r, r.getMenu());
                return true;
            } else return false;
        });
        for (MenuRenderer render : renders)
            render.think(animations);
        if (animations!=null)
            animations.finishTick();
        for (MenuRenderer render : renders)
            render.revalidate();
    }

}
