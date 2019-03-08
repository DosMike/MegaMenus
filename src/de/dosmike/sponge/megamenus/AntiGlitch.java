package de.dosmike.sponge.megamenus;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import valandur.webapi.shadow.com.ctc.wstx.util.TextBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is supposed to detect players that try to duplicate items.<br>
 * While extensive testing through the use of macros did not allow me to duplicate
 * items it may be of interest for the server admin to know people that try to
 * cheat the system anyways.<br>
 * Detection is done on APS base, where clicking the inventory and closing the
 * inventory (crucial factors in duping) are tracked.
 */
final public class AntiGlitch {

    public static final DataQuery inject = DataQuery.of("UnsafeData", "MegaMenus");
    public static final DataQuery meta_1_12 = DataQuery.of("UnsafeDamage");

    private static boolean enabled = true;
    private static boolean notifAdmins = true;
    private static boolean verbose = true;

    /**
     * Takes configuration values to set up the Anti Glitch system
     * @param enabled true if this system shall be used
     * @param notifyAdmins notify admins in chat when glitch is detected
     * @param verbose print additional information on detection
     * */
    public static void setup(boolean enabled, boolean notifyAdmins, boolean verbose) {
        AntiGlitch.enabled = enabled;
        AntiGlitch.notifAdmins = notifyAdmins;
        AntiGlitch.verbose = verbose;
    }

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
     * Check if the given stack contains the injected NBT tag, if so call out player.
     * @param player the player to block on detection
     * @param stack the stack to check for injected NBT
     * @return true if a glitched stack was detected in order to cancel events, ect */
    public static boolean checkItemStack(Player player, ItemStack stack) {
        return checkItemStack(player, stack.toContainer());
    }
    /**
     * Check if the given stack contains the injected NBT tag, if so call out player.
     * @param player the player to block on detection
     * @param stack the stack to check for injected NBT
     * @return true if a glitched stack was detected in order to cancel events, ect */
    public static boolean checkItemStack(Player player, ItemStackSnapshot stack) {
        return checkItemStack(player, stack.toContainer());
    }
    private static boolean checkItemStack(Player player, DataContainer stack) {
        boolean result = stack.get(inject).isPresent();
        if (result && enabled) calloutGlitcher(player);
        return result;
    }

    /**
     * Scan this players inventory for itemstacks with injected NBT.
     * Every slot that is found containing prohibited items is cleared and
     * the player is called out.
     * @param player the players inventory to scan
     */
    public static void scanInventory(Player player) {
        List<ItemStackSnapshot> hits = new LinkedList<>();
        for (Inventory i : player.getInventory().slots()) {
            if (checkItemStack(player, i.peek().orElse(ItemStack.empty()))) {
                hits.add(i.peek().get().createSnapshot());
                i.clear();
            }
        }
        if (hits.size()>0)
            log(player, hits);
    }

    /**
     * Tells the console and admins that the player glitched the specified items
     */
    public static void log(Player player, Collection<ItemStackSnapshot> items) {
        Text message;
        {
            Text.Builder builder = Text.builder();
            builder.append(Text.of("Player ", player.getName(), "(", player.getUniqueId().toString(), ") acquired ", items.size(), " glitched items"));
            if (verbose) {
                boolean first = true;

                Set<Text> asText = items.stream().map(i-> {
                    Optional<Text> d = i.get(Keys.DISPLAY_NAME);
                    Optional<Integer> damage = i.toContainer()
                            .get(meta_1_12)
                            .map(o->(Integer)o);

                    Text.Builder item = Text.builder();
                    item.append(Text.of(i.getQuantity(), "x "));
                    if (d.isPresent()) {
                        if (damage.isPresent())
                            item.append(d.get(), Text.of('[', i.getType().getId(), ':', damage.get(), ']'));
                        else
                            item.append(d.get(), Text.of('[', i.getType().getId(), ']'));
                    } else {
                        if (damage.isPresent())
                            item.append(Text.of(i.getType().getId(), ':', damage.get()));
                        else
                            item.append(Text.of(i.getType().getId()));
                    }
                    return item.build();
                }).collect(Collectors.toSet());
                for (Text t : asText) {
                    if (first) {
                        first=false;
                        builder.append(Text.of(": "));
                    } else
                        builder.append(Text.of(", "));
                    builder.append(t);
                }
            }
            builder.color(TextColors.RED);
            message = builder.build();
        }
        MegaMenus.getLogger().error(message.toPlain());
        if (notifAdmins) {
            Sponge.getServer().getOnlinePlayers().stream()
                    .filter(p->p.hasPermission("megamenus.antiglitch.notify"))
                    .forEach(p->p.sendMessage(message));
        }
    }

}
