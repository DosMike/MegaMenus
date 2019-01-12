package de.dosmike.sponge.megamenus.api.elements.concepts;

import de.dosmike.sponge.megamenus.api.listener.OnChangeListener;

public interface IValueChangeable<V> {

    void setOnChangeListener(OnChangeListener<V> listener);

    OnChangeListener<V> getOnChangeListener();

}
