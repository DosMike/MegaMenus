package de.dosmike.sponge.megamenus.api.listener;

import de.dosmike.sponge.megamenus.api.elements.concepts.IClickable;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.entity.living.player.Player;

import java.awt.event.MouseEvent;

/**
 * Listener that triggers when a player clicks an {@link IClickable} menu element<br>
 * Copy of method description:<br>
 * You can get the menu instance and states via the element parameter<br>
 * param <b>element</b> - the element that was clicked<br>
 * param <b>player</b> - the player that clicked the element<br>
 * param <b>button</b> - the index of the mouse button used, one of {@code MouseEvent.NOBUTTON}, {@code MouseEvent.BUTTON1}, {@code MouseEvent.BUTTON2} or {@code MouseEvent.BUTTON3}<br>
 * param <b>sift</b> - true if shift was held during this click. will always be false for {@code MouseEvent.BUTTON3}
 */
@FunctionalInterface
public interface OnClickListener {

    /**
     * You can get the menu instance and states via the element parameter
     * @param element the element that was clicked
     * @param player the player that clicked the element
     * @param button the index of the mouse button used, one of {@code MouseEvent.NOBUTTON}, {@code MouseEvent.BUTTON1}, {@code MouseEvent.BUTTON2} or {@code MouseEvent.BUTTON3}
     * @param shift true if shift was held during this click. will always be false for {@code MouseEvent.BUTTON3}
     */
    void onClick(IElement element,
                 Player player,
                 @MagicConstant(intValues = {MouseEvent.NOBUTTON, MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3}) int button,
                 boolean shift);

}
