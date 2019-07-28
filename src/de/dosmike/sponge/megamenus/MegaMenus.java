package de.dosmike.sponge.megamenus;

import com.google.inject.Inject;
import de.dosmike.sponge.megamenus.api.elements.IIcon;
import de.dosmike.sponge.megamenus.impl.BaseMenuImpl;
import de.dosmike.sponge.megamenus.impl.RenderManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;

@Plugin(id="megamenus", name="Mega Menus", version="0.5", authors={"DosMike"})
final public class MegaMenus {
    public static void main(String[] args) { System.err.println("This plugin can not be run as executable!"); }

    private static MegaMenus instance;
    public static MegaMenus getInstance() {
        return instance;
    }
    private static PluginContainer container;
    public PluginContainer asContainer() {
        return container;
    }

    @Inject
    private Logger logger;
    private static final Object logMutex = new Object();
    public static Logger getLogger() { return getInstance().logger; }
    public static void l(String format, Object... args) {
        synchronized (logMutex) {
            instance.logger.info(String.format(format, args));
        }
    }
    public static void w(String format, Object... args) {
        synchronized (logMutex) {
            instance.logger.warn(String.format(format, args));
        }
    }

    @Listener
    public void onServerPreInit(GamePreInitializationEvent event) {
        instance = this;
        Sponge.getEventManager().registerListeners(this, new EventListeners());
    }

    @Listener
    public void onServerPostInit(GamePostInitializationEvent event) {
        try {
            Class.forName("valandur.webapi.WebAPI");
            l("  Registering into WebAPI...");
            new de.dosmike.sponge.megamenus.compat.webApi.Initializer().init(this);
        } catch (ClassNotFoundException ignored) { }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        container = Sponge.getPluginManager().fromInstance(this).get();
        CommandRegistra.registerCommands();

        Task.builder().intervalTicks(1).execute(()->{
            try {
                RenderManager.tickRendering();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).submit(this);

        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ItemStack[] paginationIcons = new ItemStack[3];
    /** arrray contains icons (in order) for previous, pageno, next */
    public static ItemStack[] getPaginationIcons() {
        return paginationIcons;
    }
    /** @return the iicon or null if parsing failed */
    private ItemStack parsePaginationIconString(String s) {
        String[] parts = s.split(":");
        int damage = 0;
        ItemType type = null;
        if (parts.length == 1) {
            type = Sponge.getRegistry().getType(ItemType.class, "minecraft"+s).orElse(null);
            if (type == null) MegaMenus.w("Error while parsing pagination icon: No such item type minecraft:%s", s);
        } else if (parts.length == 2) {
            type = Sponge.getRegistry().getType(ItemType.class, s).orElse(null);
            if (type == null) MegaMenus.w("Error while parsing pagination icon: No such item type %s",s);
        } else if (parts.length == 3) {
            String stype = parts[0]+":"+parts[1];
            damage = Integer.parseInt(parts[2]);
            type = Sponge.getRegistry().getType(ItemType.class, stype).orElse(null);
            if (type == null) MegaMenus.w("Error while parsing pagination icon: No such item type %s", stype);
        } else {
            MegaMenus.w("Error while parsing pagination icon: Illegal Value");
        }
        if (type == null) return null;

        ItemStack item = ItemStack.of(type);
        if (damage != 0) {
            item = ItemStack.builder()
                    .fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"), damage))
                    .build();
        }
        return item;
    }

    @Inject
    @DefaultConfig(sharedRoot = true)
    public ConfigurationLoader<CommentedConfigurationNode> loader;

    void loadConfig() throws IOException {
        boolean needsSaving = false;
        CommentedConfigurationNode root = loader.load(ConfigurationOptions.defaults());
        if (root.isVirtual()) {
            ConfigurationLoader<CommentedConfigurationNode> defaults =
                    HoconConfigurationLoader.builder()
                            .setURL(Sponge.getAssetManager()
                                    .getAsset(this, "defaults.conf").get()
                                    .getUrl())
                            .build();
            root.mergeValuesFrom(defaults.load(ConfigurationOptions.defaults()));
            needsSaving = true;
        }


        ConfigurationNode group = root.getNode("antiglitch");
        if (group.isVirtual()) {
            ConfigurationLoader<CommentedConfigurationNode> defaults =
                    HoconConfigurationLoader.builder()
                            .setURL(Sponge.getAssetManager()
                                    .getAsset(this, "defaults.conf").get()
                                    .getUrl())
                            .build();
            CommentedConfigurationNode defgroup = defaults.load(ConfigurationOptions.defaults()).getNode("antiglitch");
            group.mergeValuesFrom(defgroup);
            //set the value (i assume mergin values does not clear the virtual flag)
            root.getNode("antiglitch").setValue(group);
            needsSaving = true;
        }

        AntiGlitch.setup(
                group.getNode("enabled").getBoolean(true),
                group.getNode("notifyAdmins").getBoolean(true),
                group.getNode("verboseLogging").getBoolean(true)
        );

        group = root.getNode("pagination");
        if (group.isVirtual()) {
            ConfigurationLoader<CommentedConfigurationNode> defaults =
                    HoconConfigurationLoader.builder()
                            .setURL(Sponge.getAssetManager()
                                    .getAsset(this, "defaults.conf").get()
                                    .getUrl())
                            .build();
            CommentedConfigurationNode defgroup = defaults.load(ConfigurationOptions.defaults()).getNode("pagination");
            group.mergeValuesFrom(defgroup);
            //set the value (i assume mergin values does not clear the virtual flag)
            root.getNode("pagination").setValue(group);
            needsSaving = true;
        }
        ItemStack icon = parsePaginationIconString(group.getNode("previous").getString("minecraft:arrow"));
        paginationIcons[0] = icon == null ? ItemStack.of(ItemTypes.ARROW) : icon;
        icon = parsePaginationIconString(group.getNode("current").getString("minecraft:paper"));
        paginationIcons[1] = icon == null ? ItemStack.of(ItemTypes.PAPER) : icon;
        icon = parsePaginationIconString(group.getNode("next").getString("minecraft:arrow"));
        paginationIcons[2] = icon == null ? ItemStack.of(ItemTypes.ARROW) : icon;

        if (needsSaving)
            loader.save(root);
    }

    /**
     * This is a constructor wrapper for the API side interface. This creates a new
     * {@link BaseMenuImpl}. Can be cast to IMenu if needed.
     */
    public static BaseMenuImpl createMenu() {
        return new BaseMenuImpl();
    }

}
