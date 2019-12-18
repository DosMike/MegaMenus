package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IPressable;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Listener that triggers when a player one of a limited set of buttons while hovering
 * over a {@link IPressable} menu element<br>
 * Copy of method description:<br>
 * You can get the menu instance and states via the element parameter<br>
 * param <b>element</b> - the element that was clicked<br>
 * param <b>player</b> - the player that clicked the element<br>
 * param <b>key</b> - A limited set of available buttons that can actually be detected, see {@link IPressable.Buttons}
 * param <b>sift</b> - true if shift was held and detectable during this key press. will always be false for HOTBAR_ keys
 */
@FunctionalInterface
public interface OnKeyListener<X extends IPressable> {

    /**
     * You can get the menu instance and states via the element parameter
     * @param element the element that was clicked
     * @param player the player that clicked the element
     * @param key A limited set of available buttons that can actually be detected, see {@link IPressable.Buttons}
     * @param ctrl true if ctrl was held and detectable during this key press. will always be false for HOTBAR_ keys
     */
    void onKeyPress(X element,
                    Player player,
                    @MagicConstant(valuesFromClass = IPressable.Buttons.class) IPressable.Buttons key,
                    boolean ctrl);

}
