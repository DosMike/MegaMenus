package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnClickListener;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.entity.living.player.Player;

import java.awt.event.MouseEvent;

public interface IClickable {

    void setOnClickListener(OnClickListener listener);

    OnClickListener getOnClickListerner();

    void fireClickEvent(
            Player viewer,
            @MagicConstant(intValues = {MouseEvent.NOBUTTON, MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3}) int button,
            boolean shift
    );

}
