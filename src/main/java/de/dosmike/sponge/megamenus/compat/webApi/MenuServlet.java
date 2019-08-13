package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRenderer;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.listener.OnRenderStateListener;
import de.dosmike.sponge.megamenus.compat.events.ContextKeys;
import de.dosmike.sponge.megamenus.compat.events.MenuRendererEvent;
import de.dosmike.sponge.megamenus.impl.GuiRenderer;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import de.dosmike.sponge.megamenus.impl.util.MenuUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.serializer.TextSerializers;
import valandur.webapi.WebAPI;
import valandur.webapi.servlet.base.BaseServlet;
import valandur.webapi.servlet.base.Permission;
import valandur.webapi.shadow.io.swagger.annotations.Api;
import valandur.webapi.shadow.io.swagger.annotations.ApiOperation;
import valandur.webapi.shadow.javax.ws.rs.*;
import valandur.webapi.shadow.javax.ws.rs.core.MediaType;
import valandur.webapi.shadow.javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For the purpose of WebAPI a menu is always accompanied with
 * a GuiRenderer, I guess chat and books are more accessible
 * through other servlets, so this should be fine for Inventory
 * only.
 * Also for simplicity there's no option to create bound renderer
 * with this API, menus can easily be copied to create a separate
 * state.
 */
@Path("megamenus")
@Api(value = "Create interactive menus for your players",
    tags = {"Menu", "Inventory", "UI", "MegaMenus"})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class MenuServlet extends BaseServlet {

    private static Map<UUID, IMenu> menus = new HashMap<>(); //all menus owned by WebAPI
    private static Map<UUID, GuiRenderer> renderer = new HashMap<>(); //all renderer created for those menus (same key)

    //region menu
    @GET
    @Path("/menu")
    @Permission({"megamenus","menu","list"})
    @ApiOperation(value = "List menus", notes = "Returns a list of all menus")
    public Collection<CachedMenu> listMenus() {
        return menus.values().stream().map(CachedMenu::new).collect(Collectors.toSet());
    }

    @POST
    @Path("/menu")
    @Permission({"megamenus","menu","create"})
    @ApiOperation(value = "Create menu", notes = "Creates a new menu",
            response = CachedMenu.class
    )
    public Response createMenu(CachedMenu req) throws URISyntaxException {
        if (req == null)
            throw new BadRequestException("Missing request body");

        CachedMenu menu = WebAPI.runOnMain(()-> {
            IMenu instance = MegaMenus.createMenu();
            if (req.getTitle() != null)
                instance.setTitle(TextSerializers.FORMATTING_CODE.deserialize(req.getTitle()));
            menus.put(instance.getUniqueId(), instance);
            return new CachedMenu(instance);
        });
        return Response.created(new URI(null, null, menu.getLink(), null)).entity(menu).build();
    }

    @GET
    @Path("/menu/{mid}")
    @Permission({"megamenus","menu","get"})
    @ApiOperation(value = "Get menu", notes = "Read a menu with all elements")
    public CachedMenu getMenu(@PathParam("mid") UUID mid)
            throws NotFoundException {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        return new CachedMenu(menus.get(mid));
    }

    @PUT
    @Path("/menu/{mid}")
    @Permission({"megamenus","menu","update"})
    @ApiOperation(value = "Update menu", notes = "This will only update the title, elements have to be addressed through the respective endpoints")
    public CachedMenu setMenu(@PathParam("mid") UUID mid, CachedMenu newMenu)  {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        if (newMenu == null)
            throw new BadRequestException("Missing request body");


        //i don't want to iterate through all pages and copy elements :<
        return WebAPI.runOnMain(()->{
            IMenu live = menus.get(mid);
            live.setTitle(TextSerializers.FORMATTING_CODE.deserialize(newMenu.getTitle()));
            return new CachedMenu(live);
        });
    }

    @DELETE
    @Path("/menu/{mid}")
    @Permission({"megamenus","menu","delete"})
    @ApiOperation(value = "Delete menu", notes = "Deletes a menu")
    public CachedMenu deleteMenu(@PathParam("mid") UUID mid)  {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);

        return WebAPI.runOnMain(()-> {
            RenderManager.getRenderFor(menu)
                    .forEach(MenuRenderer::closeAll);
            renderer.remove(mid);
            menus.remove(mid);
            return new CachedMenu(menu);
        });
    }
    //endregion

    //region page
    @GET
    @Path("/menu/{mid}/{page}")
    @Permission({"megamenus","menu","get"})
    @ApiOperation(value = "Reads a single page of elements")
    public Collection<CachedElement> getPage(@PathParam("mid") UUID mid, @PathParam("page") int page) throws NotFoundException {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);
        if (page < 1 || page > menu.pages())
            throw new IllegalArgumentException("Page number out of Bounds");

        return WebAPI.runOnMain(()->
            menu.getPageElements(page).stream()
                    .map(e->new CachedElement(e,page))
                    .collect(Collectors.toList())
        );
    }

    @DELETE
    @Path("/menu/{mid}/{page}")
    @Permission({"megamenus","menu","delete"})
    @ApiOperation(value = "Delete a page of elements")
    public CachedMenu deletePage(@PathParam("mid") UUID mid, @PathParam("page") int page) throws NotFoundException, BadRequestException {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);
        if (page < 1 || page > menu.pages())
            throw new BadRequestException("Page number out of Bounds");

        return WebAPI.runOnMain(()-> {
            menu.removePage(page);
            return new CachedMenu(menu);
        });
    }
    //endregion

    //region element
    @POST
    @Path("/menu/{mid}/{page}/{y}/{x}")
    @Permission({"megamenus","menu","edit"})
    @ApiOperation(value = "Add element", notes = "Adds an element to the menu",
            response = CachedElement.class
    )
    public Response addElement(@PathParam("mid") UUID mid, @PathParam("page") int page, @PathParam("y") int y, @PathParam("x") int x, CachedElement req) throws NotFoundException, BadRequestException, URISyntaxException {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);
        if (page < 1)
            throw new BadRequestException("Page can't be less than 1");
        if (y < 0 || y > 5)
            throw new BadRequestException("Y out of range [0..5]");
        if (x < 0 || x > 8)
            throw new BadRequestException("X out of range [0..8]");
        if (req == null)
            throw new BadRequestException("Request body is required");

        CachedElement element = WebAPI.runOnMain(()->{
            IElement ielement = req.createInstance();
            ielement.setPosition(new SlotPos(x, y));
            menu.add(page, ielement);
            return new CachedElement(ielement, page);
        });

        return Response.created(new URI(null, null, element.getLink(), null)).entity(element).build();
    }

    @GET
    @Path("/menu/{mid}/{page}/{y}/{x}")
    @Permission({"megamenus","menu","edit"})
    @ApiOperation(value = "Get menu", notes = "Read a menu with all elements")
    public CachedElement getElement(@PathParam("mid") UUID mid, @PathParam("page") int page, @PathParam("x") int x, @PathParam("y") int y) throws NotFoundException {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);

        return WebAPI.runOnMain(()->
                MenuUtil.getAllElementsAt(menu, page, new SlotPos(x,y)).stream()
                        .findFirst()
                        .map(e->new CachedElement(e, page))
                        .orElseThrow(()->
                                new NotFoundException("No Element found at specified position"))
        );
    }

    @PUT
    @Path("/menu/{mid}/{page}/{y}/{x}")
    @Permission({"megamenus","menu","edit"})
    @ApiOperation(value = "Update menu", notes = "Update a menu element")
    public CachedElement setElement(@PathParam("mid") UUID mid, @PathParam("page") int page, @PathParam("x") int x, @PathParam("y") int y, CachedElement req)  {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);

        return WebAPI.runOnMain(()->{
            Set<IElement> elements = MenuUtil.getAllElementsAt(menu, page, new SlotPos(x, y));
            if (elements.isEmpty()) //POST should have been used, there's nothing here
                throw new BadRequestException("No element at the specified position");

            menu.remove(page, x, y);
            IElement ielement = req.createInstance();
            ielement.setPosition(new SlotPos(x, y));
            menu.add(page, ielement);
            return new CachedElement(ielement, page);
        });
    }

    @DELETE
    @Path("/menu/{mid}/{page}/{y}/{x}")
    @Permission({"megamenus","menu","edit"})
    @ApiOperation(value = "Delete menu", notes = "Deletes a menu element")
    public CachedElement deleteElement(@PathParam("mid") UUID mid, @PathParam("page") int page, @PathParam("x") int x, @PathParam("y") int y)  {
        if (!menus.containsKey(mid))
            throw new NotFoundException("No menu with this ID was found: "+mid.toString());
        IMenu menu = menus.get(mid);

        return WebAPI.runOnMain(()->{
            CachedElement element = MenuUtil.getAllElementsAt(menu, page, new SlotPos(x,y)).stream()
                    .findFirst()
                    .map(e->new CachedElement(e, page))
                    .orElseThrow(()->
                            new NotFoundException("No Element found at specified position"));
            menu.remove(page, x, y);
            return element;
        });
    }
    //endregion

    //region renderer
    @GET
    @Path("/render")
    @Permission({"megamenus","renderer","list"})
    @ApiOperation(value = "List renderer", notes = "Returns a list of all renderer for menus created with WebAPI")
    public Collection<CachedRenderer> listRenderer() {
        return WebAPI.runOnMain(()-> renderer.values().stream()
                .map(CachedRenderer::new)
                .collect(Collectors.toSet())
        );
    }

    @POST
    @Path("/render/{mid}")
    @Permission({"megamenus","renderer","create"})
    @ApiOperation(value = "Create menu", notes = "Creates a new menu",
            response = CachedRenderer.class
    )
    public Response createRenderer(@PathParam("mid") UUID menuID, CachedRenderer req) throws URISyntaxException, BadRequestException {
        if (renderer.containsKey(menuID))
            throw new BadRequestException("A renderer for this menu already exists");
        if (!menus.containsKey(menuID))
            throw new BadRequestException("A menu with this id does not exist");
        IMenu menu = menus.get(menuID);

        if (req == null)
            throw new BadRequestException("Missing request body");

        CachedRenderer cachedRenderer = WebAPI.runOnMain(()->{
            GuiRenderer guiRender = (GuiRenderer) menu.createGuiRenderer(req.getHeight(), false);
            guiRender.setRenderListener(new OnRenderStateListener() {
                @Override
                public boolean closed(MenuRenderer render, IMenu menu, Player viewer) {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.addContext(ContextKeys.menuID, menu.getUniqueId());
                        Sponge.getEventManager().post(new MenuRendererEvent.Close(render, viewer, frame.getCurrentCause()));
                    }
                    return false;
                }
            });
            renderer.put(menuID, guiRender);
            return new CachedRenderer(guiRender);
        });

        return Response.created(new URI(null, null, cachedRenderer.getLink(), null)).entity(cachedRenderer).build();
    }

    @GET
    @Path("/render/{mid}")
    @Permission({"megamenus","renderer","get"})
    @ApiOperation(value = "Get the renderer for this menu")
    public CachedRenderer getRenderer(@PathParam("mid") UUID mid)  {
        if (!renderer.containsKey(mid))
            throw new BadRequestException("There's no renderer for a menu with this id");
        return WebAPI.runOnMain(()->{
            GuiRenderer r = renderer.get(mid);
            return new CachedRenderer(r);
        });
    }
    @GET
    @Path("/render/find/{viewer}")
    @Permission({"megamenus","renderer","get"})
    @ApiOperation(value = "Get the renderer for viewer", notes = "Returns the renderer the viewer is currently subject to")
    public CachedRenderer findRenderer(@PathParam("viewer") UUID viewer)  {
        return WebAPI.runOnMain(()->{
            Player player = Sponge.getServer().getOnlinePlayers().stream()
                    .filter(p->p.getUniqueId().equals(viewer))
                    .findFirst()
                    .orElseThrow(()->
                            new BadRequestException("Player not online"));
            MenuRenderer render = RenderManager.getRenderFor(player).orElseThrow(()->
                    new NotFoundException("Player is not viewing any menu"));
            if (!(render instanceof GuiRenderer) || !renderer.containsKey(render.getMenu().getUniqueId()))
                throw new NotFoundException("Player is not viewing any menu"); //at least none of the WebAPI menus
            return new CachedRenderer((GuiRenderer)render);
        });
    }

    @PUT
    @Path("/render/{mid}/{viewer}")
    @Permission({"megamenus","renderer","open"})
    @ApiOperation(value = "Open renderer", notes = "Opens the renderer to viewer, effectively opening the menu")
    public CachedRenderer openRenderer(@PathParam("mid") UUID mid, @PathParam("viewer") UUID viewer)  {
        if (!renderer.containsKey(mid))
            throw new BadRequestException("Not renderer for a menu with this id was created");
        GuiRenderer guiRenderer = renderer.get(mid);

        return WebAPI.runOnMain(()-> {
            Player player = Sponge.getServer().getOnlinePlayers().stream()
                    .filter(p->p.getUniqueId().equals(viewer))
                    .findFirst()
                    .orElseThrow(()->
                            new BadRequestException("Player not online"));
            guiRenderer.open(player, false);
            return new CachedRenderer(guiRenderer);
        });
    }

    @DELETE
    @Path("/render/{mid}/{viewer}")
    @Permission({"megamenus","renderer","close"})
    @ApiOperation(value = "Close renderer", notes = "Close the renderer for this viewer")
    public CachedRenderer closeRenderer(@PathParam("mid") UUID mid, @PathParam("viewer") UUID viewer)  {
        if (!renderer.containsKey(mid))
            throw new BadRequestException("Not renderer for a menu with this id was created");
        GuiRenderer guiRenderer = renderer.get(mid);

        return WebAPI.runOnMain(()->{
            if (guiRenderer.getViewers().stream().noneMatch(p->p.getUniqueId().equals(viewer)))
                throw new BadRequestException("Player is currently viewing a different menu");
            Player player = Sponge.getServer().getOnlinePlayers().stream()
                    .filter(p->p.getUniqueId().equals(viewer))
                    .findFirst()
                    .orElseThrow(()->
                            new BadRequestException("Player not online"));
            guiRenderer.close(player);
            return new CachedRenderer(guiRenderer);
        });
    }

    @DELETE
    @Path("/renderer/{mid}")
    @Permission({"megamenus","renderer","close"})
    @ApiOperation(value = "Delete menu", notes = "Closes this renderer for all currently active viewers")
    public CachedRenderer deleteRenderer(@PathParam("mid") UUID mid)  {
        if (!renderer.containsKey(mid))
            throw new BadRequestException("Not renderer for a menu with this id was created");
        GuiRenderer guiRenderer = renderer.get(mid);

        return WebAPI.runOnMain(()->{
            guiRenderer.closeAll();
            renderer.remove(mid);
            return new CachedRenderer(guiRenderer);
        });
    }
    //endregion
}
