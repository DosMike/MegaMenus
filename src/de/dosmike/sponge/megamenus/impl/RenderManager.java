package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRender;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** this map tries to keep book of all active MenuRender instances */
public class RenderManager {

    private static AnimationManager animations;
    private static Set<MenuRender> renders = new HashSet<>();
    public static void register(MenuRender menuRender) {
        if (renders.add(menuRender)) {
            if (menuRender.getRenderListener() != null)
                menuRender.getRenderListener().resumed(menuRender, menuRender.getMenu());
        }
    }
    public static Optional<MenuRender> getRenderFor(Player viewer) {
        for (MenuRender r : renders)
            if (r.getViewers().contains(viewer))
                return Optional.of(r);
        return Optional.empty();
    }
    /** this might return a render for each viewer or one render for all viewers depending on
     * whether the menu is implemented as shared menu or single viewer menu */
    public static Collection<MenuRender> getRenderFor(IMenu menu) {
        return renders.stream().filter(r->r.getMenu().equals(menu)).collect(Collectors.toSet());
    }
    /** When a player changes the target render, they have to be kicked out of all other renders.
     * This will use closeSilent in order to prevent the new targetRender from closing. */
    public static void notifyRenderChange(Player viewer, MenuRender targetRender) {
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
        if (renders.removeIf(r->{
            if (!r.hasViewers()) {
                if (r.getRenderListener() != null)
                    r.getRenderListener().resumed(r, r.getMenu());
                return true;
            } else return false;
        }) && renders.size() == 0) {
            //no more renders are open, animationManager will freeze
            animations = null;
            return;
        } else if (animations == null && renders.size() > 0) {
            //let animation run again as soon as a renderer was opened again.
            animations = new AnimationManager();
        }
        for (MenuRender render : renders)
            render.think(animations);
        if (animations!=null)
            animations.finishTick();
        for (MenuRender render : renders)
            render.revalidate();
    }
}
