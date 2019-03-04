package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.api.util.Tickable;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to manage animations of {@link IIcon}s or more specifically
 * all {@link Tickable} objects.
 */
final public class AnimationManager {

    private Set<Tickable> ticked = new HashSet<>();
    private long lastTick = System.currentTimeMillis();
    private Integer deltaTime = null;
    /**
     * @return true if the {@link Tickable} was not already ticked and
     * reported a change in state
     */
    public boolean singleTick(Tickable object) {
        return ticked.add(object) && object.tick(getDeltaTime());
    }
    /**
     * This method has to be called after one render cycle in order
     * to free all {@link Tickable} object, in order for them to be
     * tickable again in the next cycle
     */
    public void finishTick() {
        ticked.clear();
        lastTick = System.currentTimeMillis();
        deltaTime = null;
    }

    /**
     * Repeatably callable method to get the time passed since the last render cycle.
     * @return the time in ms since the last render cycle
     */
    public Integer getDeltaTime() {
        if (deltaTime == null) {
            deltaTime = (int)(System.currentTimeMillis() - lastTick);
        }
        return deltaTime;
    }
}
