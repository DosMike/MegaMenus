package de.dosmike.sponge.megamenus;

import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

/**
 * This class is supposed to detect players that try to duplicate items.<br>
 * While extensive testing through the use of macros did not allow me to duplicate
 * items it may be of interest for the server admin to know people that try to
 * cheat the system anyways.<br>
 * Detection is done on APS base, where clicking the inventory and closing the
 * inventory (crucial factors in duping) are tracked.
 */
final public class AntiGlitch {

    private static boolean enabled = true;
    //most people seem to agree that 8 CPS is already heck a fast without accelerators
    //default 10 cps (a bit more generous)
    private static int maxAPS = 10;
    //observation period for cps, exceed maxAPS within this period to trigger.
    //for < 1000 ms this of course means that less than maxAPS clicks are required.
    //default 5 ticks (= 250ms)
    private static int obervationPeriod = 250;
    //so standard configuration banns with 2.5(=3) actions within 5 ticks

    /** Takes configuration values to set up the Anti Glitch system
     * @param enabled true if this system shall be used
     * @param maxAPS  the maximum Actions Per Second allowed
     * @param observationPeriod the timespan to observe */
    public static void setup(boolean enabled, int maxAPS, int observationPeriod) {
        AntiGlitch.enabled = enabled;
        AntiGlitch.maxAPS = maxAPS;
        AntiGlitch.obervationPeriod = observationPeriod;
    }

    private static Map<UUID, List<Long>> detections = new HashMap<>();
    private static Set<UUID> glitchers = new HashSet<>();
    /**
     * Marks this player as glitcher and locks them out from usage.
     */
    public static void calloutGlitcher(Player player) {
        if (!glitchers.add(player.getUniqueId())) return;
        MegaMenus.getLogger().error(String.format("%s(%s) Tried to glitch a menu and was automatically banned from using menus until the server restarts.", player.getName(), player.getUniqueId().toString()));
    }
    /**
     * @param player the player to check
     * @return true if this player was marked as glitcher
     */
    public static boolean isGlitcher(Player player) {
        if (!enabled) return false;
        return glitchers.contains(player.getUniqueId());
    }
    /**
     * Allows the player to access menus again
     * @param player the player to pardon
     * @return true if the player was blocked
     */
    public static boolean pardonGlitcher(Player player) {
        return glitchers.remove(player.getUniqueId());
    }
    /**
     * Cleanup method to be called when a player disconnects
     * @param player the player to clean up
     */
    public static void glitchUntrack(Player player) {
        detections.remove(player.getUniqueId());
    }
    /**
     * Method that tracks APS, call where appropriate.<br>
     * Checks the players APS and calls out the glitcher if the limit is exceeded.
     * @param player the player to track
     */
    public static void quickInteractionTrack(Player player) {
        if (!enabled) return;
        List<Long> recent = detections
                .getOrDefault(player.getUniqueId(), new LinkedList<>());
        recent.add(System.currentTimeMillis());
        recent.removeIf(l -> System.currentTimeMillis() - l > obervationPeriod);
        detections.put(player.getUniqueId(), recent);
        int aps = recent.size()*1000/obervationPeriod; //actions per second
        if (aps > maxAPS)
            calloutGlitcher(player);
    }

}
