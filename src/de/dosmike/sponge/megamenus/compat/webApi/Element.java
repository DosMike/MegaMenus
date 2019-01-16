package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.IMenu;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.util.Constants;

/** WebAPI cached IElement */
public class Element extends CachedObject<IMenu> {

    public Element() {
        super(null);
    }

    @Override
    public String getLink() {
        return Constants.BASE_PATH + "/megamenus/menu/{ID}/{page}/{x}/{y}";
    }


}
