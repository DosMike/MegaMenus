package de.dosmike.sponge.megamenus;

import org.spongepowered.api.entity.living.player.Player;

import java.util.*;
import java.util.stream.Collectors;

final public class AntiGlitch {

    //most people seem to agree that 8 CPS is already heck a fast without accelerators
    //default 10 cps (a bit more generous)
    private static int maxCPS = 10;
    //observation period for cps, exceed maxCPS within this period to trigger.
    //for < 1000 ms this of course means that less than maxCPS clicks are required.
    //default 5 ticks (= 250ms)
    private static int obervationPeriod = 250;
    //so standard configuration banns with 2.5(=3) clicks within 5 ticks

    private static Map<UUID, List<Long>> detections = new HashMap<>();
    private static Set<UUID> glitchers = new HashSet<>();
    public static void calloutGlitcher(Player player) {
        if (!glitchers.add(player.getUniqueId())) return;
        MegaMenus.getLogger().error(String.format("%s(%s) Tried to glitch a menu and was automatically banned from using menus until the server restarts.", player.getName(), player.getUniqueId().toString()));
    }
    public static boolean isGlitcher(Player player) {
        return glitchers.contains(player.getUniqueId());
    }
    public static boolean pardonGlitcher(Player player) {
        return glitchers.remove(player.getUniqueId());
    }
    public static void glitchUntrack(Player player) {
        detections.remove(player.getUniqueId());
    }
    public static void quickInteractionTrack(Player player) {
        List<Long> recent = detections.getOrDefault(player.getUniqueId(), new LinkedList<>()).stream().filter(l -> System.currentTimeMillis() - l <= obervationPeriod).collect(Collectors.toCollection(LinkedList::new));
        recent.add(System.currentTimeMillis());
        detections.put(player.getUniqueId(), recent);
        int cps = recent.size()*1000/obervationPeriod; //actions per second
        if (cps > maxCPS)
            calloutGlitcher(player);
    }

}
