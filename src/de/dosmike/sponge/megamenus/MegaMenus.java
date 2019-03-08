package de.dosmike.sponge.megamenus;

import com.google.inject.Inject;
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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;

@Plugin(id="megamenus", name="Mega Menus", version="0.3", authors={"DosMike"})
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

    @Inject
    @DefaultConfig(sharedRoot = true)
    public ConfigurationLoader<CommentedConfigurationNode> loader;

    void loadConfig() throws IOException {
        CommentedConfigurationNode root = loader.load(ConfigurationOptions.defaults());

        ConfigurationNode group = root.getNode("antiglitch");
        if (group.isVirtual()) {
            ConfigurationLoader<CommentedConfigurationNode> defaults =
                    HoconConfigurationLoader.builder()
                            .setURL(Sponge.getAssetManager()
                                    .getAsset(this, "defaults.conf").get()
                                    .getUrl())
                            .build();
            root.mergeValuesFrom(defaults.load(ConfigurationOptions.defaults()));
            loader.save(root);
        }

        AntiGlitch.setup(
                group.getNode("enabled").getBoolean(true),
                group.getNode("notifyAdmins").getBoolean(true),
                group.getNode("verboseLogging").getBoolean(true)
        );
    }

    /**
     * This is a constructor wrapper for the API side interface. This creates a new
     * {@link BaseMenuImpl}. Can be cast to IMenu if needed.
     */
    public static BaseMenuImpl createMenu() {
        return new BaseMenuImpl();
    }

}
