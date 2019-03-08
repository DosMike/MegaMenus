package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.impl.GuiRenderer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.cache.player.CachedPlayer;
import valandur.webapi.serialize.JsonDetails;
import valandur.webapi.shadow.io.swagger.annotations.ApiModel;
import valandur.webapi.shadow.io.swagger.annotations.ApiModelProperty;
import valandur.webapi.util.Constants;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApiModel("MegaMenusRenderer")
public class CachedRenderer extends CachedObject<GuiRenderer> {

    private GuiRenderer renderer;

    private UUID menu;
    private Set<UUID> viewer;
    private int height;

    /** creating a renderer with specified height */
    public CachedRenderer() {
        super(null);
    }

    public CachedRenderer(GuiRenderer renderer) {
        super(renderer);

        this.renderer = renderer;

        menu = renderer.getMenu().getUniqueId();
        height = renderer.getPageHeight();
        update();
    }

    @ApiModelProperty(value = "The id for the menu this renderer is currently displaying")
    @JsonDetails
    public UUID getMenu() {
        return menu;
    }

    @ApiModelProperty(value = "All players currently observing this menu")
    @JsonDetails
    public Set<UUID> getViewer() {
        return viewer;
    }

    @ApiModelProperty(value = "The rendering height for this menu in inventory rows")
    @JsonDetails
    public int getHeight() {
        return height;
    }

    @Override
    public Optional<GuiRenderer> getLive() {
        return Optional.ofNullable(renderer);
    }

    public void update() {
        viewer = renderer.getViewers().stream()
                .map(Identifiable::getUniqueId)
                .collect(Collectors.toSet());
    }

    @Override
    public String getLink() {
        return String.format("%s/megamenus/render/%s",
                Constants.BASE_PATH,
                menu.toString()
        );
    }


}

