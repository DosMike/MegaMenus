package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.serialize.JsonDetails;
import valandur.webapi.shadow.io.swagger.annotations.ApiModel;
import valandur.webapi.shadow.io.swagger.annotations.ApiModelProperty;
import valandur.webapi.util.Constants;

import java.util.*;

@ApiModel("MenuMenusMenu")
public class CachedMenu extends CachedObject<IMenu> {

    //actual menu reference
    private IMenu menu;

    private String title;
    private Map<String, CachedElement> elements = new HashMap<>();
    private int pages;

    /** for setting/changing the title of menus */
    public CachedMenu() {
        super(null);
    }

    public CachedMenu(IMenu menu) {
        super(menu);
        this.menu = menu;

        update();
    }

    @ApiModelProperty(value = "Unique identifier for this Menu",
        hidden = true
    )
    public UUID getId() {
        return menu.getUniqueId();
    }

    @ApiModelProperty(value = "The title of this menu")
    @JsonDetails
    public String getTitle() {
        return title;
    }

    @ApiModelProperty(value = "Flattened list of all element in this menu")
    @JsonDetails
    public Map<String, CachedElement> getElements() {
        return elements;
    }

    @ApiModelProperty(value = "Get the amount of pages for this menu")
    @JsonDetails
    public Integer getPages() {
        return pages;
    }

    @Override
    public Optional<IMenu> getLive() {
        return Optional.ofNullable(menu);
    }

    /** refresh cached element from live object */
    public void update() {
        elements.clear();
        for (int p = 1; p <= menu.pages(); p++) {
            for (IElement e : menu.getPageElements(p)) {
                elements.put(String.format("%d/%d/%d", p, e.getPosition().getY(), e.getPosition().getX()), new CachedElement(e, p));
            }
        }
        title = TextSerializers.FORMATTING_CODE.serialize(menu.getTitle());
        pages = menu.pages();
    }

    @Override
    public String getLink() {
        return String.format("%s/megamenus/menu/%s",
                Constants.BASE_PATH,
                getId().toString()
                );
    }


}
