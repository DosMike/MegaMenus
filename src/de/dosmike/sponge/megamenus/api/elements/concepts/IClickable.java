package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnClickListener;

public interface IClickable {

    void setOnClickListener(OnClickListener listener);

    OnClickListener getOnClickListerner();

}
