package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.IMenu;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.util.Constants;

/** WebAPI cached IElement */
public class Renderer extends CachedObject<IMenu> {

    public Renderer() {
        super(null);
    }

    @Override
    public String getLink() {
        return Constants.BASE_PATH + "/megamenus/renderer/{ID}";
    }


}
