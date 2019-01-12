package de.dosmike.sponge.megamenus.api.util;

@FunctionalInterface
public interface Tickable {

    /**
     * Notify this Tickable that a certain time has passed
     * @return true if the time passed caused a change in this objects state
     */
    boolean tick(int ms);

}
