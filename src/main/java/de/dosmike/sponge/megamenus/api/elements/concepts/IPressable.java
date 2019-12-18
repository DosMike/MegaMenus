package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.listener.OnKeyListener;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

/** Interface to be implemented by IElements that shall react to keys being pressed.
 * It might be important to note, that this feature will only work in GUI-Menus */
public interface IPressable<X extends IPressable> {

    enum Buttons {
        HOTBAR_1,
        HOTBAR_2,
        HOTBAR_3,
        HOTBAR_4,
        HOTBAR_5,
        HOTBAR_6,
        HOTBAR_7,
        HOTBAR_8,
        HOTBAR_9,
        DROP,
        ;
        /** for conversion from {@link ClickInventoryEvent.NumberPress}
         * @return the corresponding Keys for the number
         * @param number the event value
         * @throws IllegalArgumentException if the number was invalid*/
        public static Buttons fromNumberPress(int number) {
            if (number < 0 || number > 8)
                throw new IllegalArgumentException("Invalid NumberPress number");
            return values()[number];
        }
    };

    /** Replace the current {@link OnKeyListener} with the specified one.
     * @param listener the listener that shall react to keys being pressed with this
     *                  IElement.
     */
    void setOnKeyListener(OnKeyListener<X> listener);

    /** Return the current {@link OnKeyListener} associated with this element
     * that will be used whenever fireKeyEvent is invoked.
     * @return The current OnKeyListener, or null if no listener was set.
     */
    OnKeyListener<X> getOnKeyListener();

    /** This method will be called by the {@link MenuRenderer} when a button is pressed, hovering this element.
     * @param viewer the {@link Player} instance that pressed a key over this element in the assigned renderer.
     * @param key an enum value from {@link Buttons} representing one of the view detectable keys.
     * @param ctrl true if the player held the ctrl-modifier key while pressing buttons for applicable keys.
     */
    void fireKeyEvent(
            Player viewer,
            @MagicConstant(valuesFromClass = Buttons.class) Buttons key,
            boolean ctrl
    );

}
