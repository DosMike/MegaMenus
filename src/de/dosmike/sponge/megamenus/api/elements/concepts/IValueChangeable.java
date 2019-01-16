package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;
import org.spongepowered.api.entity.living.player.Player;

public interface IValueChangeable<V> {

    void setOnChangeListener(OnChangeListener<V> listener);

    OnChangeListener<V> getOnChangeListener();

    void fireChangeListener(Player viewer, V oldValue, V newValue);

}
