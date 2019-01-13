package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.util.Tickable;

import java.util.HashSet;
import java.util.Set;

final public class AnimationManager {

    private Set<Tickable> ticked = new HashSet<>();
    private long lastTick = System.currentTimeMillis();
    private Integer deltaTime = null;
    /** @return true if the {@link Tickable} was not already ticked and
     * reported a change in state */
    public boolean singleTick(Tickable object) {
        return ticked.add(object) && object.tick(getDeltaTime());
    }
    public void finishTick() {
        ticked.clear();
        lastTick = System.currentTimeMillis();
        deltaTime = null;
    }

    public Integer getDeltaTime() {
        if (deltaTime == null) {
            deltaTime = (int)(System.currentTimeMillis() - lastTick);
        }
        return deltaTime;
    }
}
