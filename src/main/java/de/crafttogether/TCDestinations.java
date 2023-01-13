package de.crafttogether;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.mysql.MySQLAdapter;
import de.crafttogether.common.mysql.MySQLConfig;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.commands.Commands;
import de.crafttogether.tcdestinations.destinations.DestinationStorage;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.listener.PlayerJoinListener;
import de.crafttogether.tcdestinations.listener.TrainEnterListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;

public final class TCDestinations extends JavaPlugin {
    public static TCDestinations plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private FileConfiguration enterMessages;

    @Override
    public void onEnable() {
        plugin = this;

        /* Check dependencies */
        if (!getServer().getPluginManager().isPluginEnabled("CTCommons")) {
            plugin.getLogger().warning("Couldn't find plugin: CTCommons");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find plugin: BKCommonLib");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find plugin: TrainCarts");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Dynmap found!");
            dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        }

        // Export resources
        PluginUtil.exportResource(this,"commands.yml");
        PluginUtil.exportResource(this,"enterMessages.yml");

        if (getDynmap() != null) {
            PluginUtil.exportResource(this,"rail.png");
            PluginUtil.exportResource(this,"minecart.png");
        }

        // Create default config
        saveDefaultConfig();

        enterMessages = new FileConfiguration(plugin.getDataFolder() + File.separator + "enterMessages.yml");
        enterMessages.load();

        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(),this);
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
        localizationManager = new LocalizationManager(this, Localization.class, getConfig().getString("Settings.Language"), "en_EN", "locales");

        localizationManager.addHeader("There are also placeholders which can be used in every message.");
        localizationManager.addHeader("StationType-Labels: {stationType} (replace 'stationType' with the name of your configured destination-types)");
        localizationManager.addHeader("Command-Names: {cmd_destination} {cmd_destinations} {cmd_destedit} {cmd_mobenter} {cmd_mobeject}");
        localizationManager.addHeader("Content: <header/> <prefix/> <footer/>");

        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());
        localizationManager.addTagResolver("header", Localization.HEADER.deserialize());
        localizationManager.addTagResolver("footer", Localization.FOOTER.deserialize());

        // Register Commands
        commands = new Commands();
        commands.enable(this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
            && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
                switch (buildType) {
                    case RELEASE -> plugin.getLogger().warning("A new full version of this plugin was released!");
                    case SNAPSHOT -> plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                }

                plugin.getLogger().warning("You can download it here: " + url);
                plugin.getLogger().warning("Version: " + version + " #" + build);
                plugin.getLogger().warning("FileName: " + fileName + " FileSize: " + UpdateChecker.humanReadableFileSize(fileSize));
                plugin.getLogger().warning("You are on version: " + currentVersion + " #" + currentBuild);

            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

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
}
