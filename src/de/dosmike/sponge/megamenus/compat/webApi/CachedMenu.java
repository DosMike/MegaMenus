package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.IMenu;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.util.Constants;

/** WebAPI cached IMenu */
public class CachedMenu extends CachedObject<IMenu> {



    public CachedMenu() {
        super(null);
    }

    @Override
    public String getLink() {
        return Constants.BASE_PATH + "/megamenus/menu/{ID}";
    }


}
