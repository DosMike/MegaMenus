package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.entity.living.player.Player;

import java.awt.event.MouseEvent;

/** Interface to be implemented by IElements that shall react to inventory clicks */
public interface IClickable<X extends IClickable> {

    /** Replace the current {@link OnClickListener} with the specified one.
     * @param listener the listener that shall react to click interactions with this
     *                  IElement.
     */
    void setOnClickListener(OnClickListener<X> listener);

    /** Return the current {@link OnClickListener} associated with this element
     * that will be used whenever fireClickEvent is invoked.
     * @return The current OnClickListener, or null if no listener was set.
     */
    OnClickListener<X> getOnClickListerner();

    /** This method will be called by the {@link MenuRenderer} when the element is clicked.
     * @param viewer the {@link Player} instance that clicked the element in the assigned renderer.
     * @param button the int value of the mouse button equal to the AWT values in {@link MouseEvent}.
     * @param shift true if the player held the shift-modifier key while clicking. /
     */
    void fireClickEvent(
            Player viewer,
            @MagicConstant(intValues = {MouseEvent.NOBUTTON, MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3}) int button,
            boolean shift
    );

}
