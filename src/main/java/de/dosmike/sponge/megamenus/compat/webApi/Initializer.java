package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.MegaMenus;
import org.spongepowered.api.Sponge;
import valandur.webapi.servlet.base.ServletService;

public class Initializer {

    public void init(MegaMenus instance) {

        ServletService service = Sponge.getServiceManager().provideUnchecked(ServletService.class);
        service.registerServlet(MenuServlet.class);

    }

}
