package de.crafttogether;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.commands.Commands;
import de.crafttogether.tcdestinations.destinations.DestinationStorage;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.listener.TrainEnterListener;
import de.crafttogether.tcdestinations.localization.LocalizationManager;
import de.crafttogether.tcdestinations.util.Util;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;
import java.util.Objects;

public final class TCDestinations extends JavaPlugin {
    public static TCDestinations plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private FileConfiguration enterMessages;
    private MiniMessage miniMessageParser;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

        /* Check dependencies */
        if (!getServer().getPluginManager().isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find BKCommonLib");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find TrainCarts");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Dynmap found!");
            dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        }

        // Export resources
        Util.exportResource("commands.yml");
        Util.exportResource("enterMessages.yml");

        if (getDynmap() != null) {
            Util.exportResource("rail.png");
            Util.exportResource("minecart.png");
        }

        // Create default config
        saveDefaultConfig();

        enterMessages = new FileConfiguration(plugin.getDataFolder() + File.separator + "enterMessages.yml");
        enterMessages.load();

        // Register Events
        getServer().getPluginManager().registerEvents(new TrainEnterListener(),this);

        // Setup MySQLConfig
        MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(getConfig().getString("MySQL.Host"));
        myCfg.setPort(getConfig().getInt("MySQL.Port"));
        myCfg.setUsername(getConfig().getString("MySQL.Username"));
        myCfg.setPassword(getConfig().getString("MySQL.Password"));
        myCfg.setDatabase(getConfig().getString("MySQL.Database"));
        myCfg.setTablePrefix(getConfig().getString("MySQL.TablePrefix"));

        // Validate configuration
        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        serverName = getConfig().getString("Settings.ServerName");

        // Initialize MySQLAdapter
        mySQLAdapter = new MySQLAdapter(this, myCfg);

        // Register DestinationTypes from config.yml
        DestinationType.registerTypes(getConfig());

        // Initialize Storage
        destinationStorage = new DestinationStorage();

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager();

        // Register Commands
        commands = new Commands();
        commands.enable(this);

        // Register Tags/Placeholder for MiniMessage
        miniMessageParser = MiniMessage.builder()
                .editTags(t -> t.resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(Localization.PREFIX.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("header", Tag.selfClosingInserting(Localization.HEADER.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("footer", Tag.selfClosingInserting(Localization.FOOTER.deserialize()))))
                .build();

        getLogger().info(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(mySQLAdapter != null)
            mySQLAdapter.disconnect();
    }

    public String getServerName() { return serverName; }

    public DynmapAPI getDynmap() { return dynmap; }

    public Commands getCommandManager() {
        return commands;
    }

    public LocalizationManager getLocalizationManager() { return localizationManager; }

    public DestinationStorage getDestinationStorage() { return destinationStorage; }

    public FileConfiguration getEnterMessages() {
        return enterMessages;
    }

    public MiniMessage getMiniMessageParser() {
        return Objects.requireNonNullElseGet(miniMessageParser, MiniMessage::miniMessage);
    }

    public BukkitAudiences adventure() {
        return adventure;
    }
}
