package de.dosmike.sponge.megamenus;

import com.google.inject.ConfigurationException;
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
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.net.ConnectException;

@Plugin(id="megamenus", name="Mega Menus", version="0.1", authors={"DosMike"})
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
        ConfigurationLoader<CommentedConfigurationNode> defaults =
                HoconConfigurationLoader.builder()
                        .setURL(Sponge.getAssetManager()
                                .getAsset(this, "defaults.conf").get()
                                .getUrl())
                        .build();
        root.mergeValuesFrom(defaults.load(ConfigurationOptions.defaults()));
        loader.save(root);

        ConfigurationNode group = root.getNode("antiglitch");
        AntiGlitch.setup(
                group.getNode("enabled").getBoolean(true),
                group.getNode("maxAPS").getInt(10),
                group.getNode("timePeriod").getInt(250)
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
