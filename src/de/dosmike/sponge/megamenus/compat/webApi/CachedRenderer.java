package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.IMenu;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.util.Constants;

/** WebAPI cached Renderer */
public class CachedRenderer extends CachedObject<IMenu> {

    public CachedRenderer() {
        super(null);
    }

    @Override
    public String getLink() {
        return Constants.BASE_PATH + "/megamenus/renderer/{ID}";
    }


}
