package de.crafttogether;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.plugin.BukkitPlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.shaded.org.bstats.bukkit.Metrics;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.commands.Commands;
import de.crafttogether.tcdestinations.destinations.DestinationStorage;
import de.crafttogether.tcdestinations.listener.PlayerJoinListener;
import de.crafttogether.tcdestinations.listener.TrainEnterListener;
import de.crafttogether.tcdestinations.speedometer.Speedometer;
import de.crafttogether.tcdestinations.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;

public final class TCDestinations extends JavaPlugin {
    public static TCDestinations plugin;
    public static PlatformAbstractionLayer platformLayer;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private Speedometer speedometer;
    private FileConfiguration enterMessages;

    @Override
    public void onEnable() {
        plugin = this;
        platformLayer = new BukkitPlatformLayer(this);

        PluginManager pluginManager = Bukkit.getPluginManager();

        /* Check dependencies */
        if (!pluginManager.isPluginEnabled("CTCommonsBukkit")) {
            plugin.getLogger().warning("Couldn't find plugin: CTCommons");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (!pluginManager.isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find plugin: BKCommonLib");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (!pluginManager.isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find plugin: TrainCarts");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (pluginManager.isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Dynmap found!");
            dynmap = (DynmapAPI) pluginManager.getPlugin("dynmap");
        }

        // Export resources
        Util.exportResource(this,"commands.yml");
        Util.exportResource(this,"enterMessages.yml");

        if (getDynmap() != null) {
            Util.exportResource(this,"rail.png");
            Util.exportResource(this,"minecart.png");
        }

        // Create default config
        saveDefaultConfig();

        // Set serverName
        serverName = getConfig().getString("Settings.ServerName");

        // Register Events
        pluginManager.registerEvents(new PlayerJoinListener(),this);
        pluginManager.registerEvents(new TrainEnterListener(),this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platformLayer, Localization.class, "en_EN", "locales");

        localizationManager.addHeader("");
        localizationManager.addHeader("There are also 'global' placeholders which can be used in every message.");
        localizationManager.addHeader("StationType-Labels: {stationType} (replace 'stationType' with the name of your configured destination-types)");
        localizationManager.addHeader("Command-Names: {cmd_destination} {cmd_destinations} {cmd_destedit} {cmd_mobenter} {cmd_mobeject}");
        localizationManager.addHeader("Content: <header/> <prefix/> <footer/>");

        // Load localization
        localizationManager.loadLocalization(getConfig().getString("Settings.Language"));

        // Add global MiniMessage tags
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());
        localizationManager.addTagResolver("header", Localization.HEADER.deserialize());
        localizationManager.addTagResolver("footer", Localization.FOOTER.deserialize());

        // Register Commands
        commands = new Commands();
        commands.enable(this);

        enterMessages = new FileConfiguration(plugin.getDataFolder() + File.separator + "enterMessages.yml");
        enterMessages.load();

        // Initialize Storage
        destinationStorage = new DestinationStorage();
        if (!destinationStorage.isActive()) {
            pluginManager.disablePlugin(plugin);
            return;
        }

        // Start Speedometer-Task
        if (getConfig().getBoolean("Speedometer.Enabled"))
            speedometer = new Speedometer();

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
            && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(platformLayer).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
                if (err != null) {
                    plugin.getLogger().warning("An error occurred while receiving update information.");
                    plugin.getLogger().warning("Error: " + err.getMessage());
                }

                if (build == null)
                    return;

                // Go sync again to avoid mixing output with other plugins
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (build.getType().equals(BuildType.RELEASE))
                        plugin.getLogger().warning("A new full version of this plugin was released!");
                    else
                        plugin.getLogger().warning("A new development version of this plugin is available!");

                    plugin.getLogger().warning("You can download it here: " + build.getUrl());
                    plugin.getLogger().warning("Version: " + build.getVersion() + " (build: " + build.getNumber() + ")");
                    plugin.getLogger().warning("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                    plugin.getLogger().warning("You are on version: " + installedVersion + " (build: " + installedBuild + ")");
                });
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        // bStats
        new Metrics(this, 17416);

        PluginInformation pluginInformation = platformLayer.getPluginInformation();
        getLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (build: " + pluginInformation.getBuild() + ") enabled.");
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(destinationStorage != null)
            destinationStorage.disconnect();
    }

    public String getServerName() { return serverName; }

    public DynmapAPI getDynmap() { return dynmap; }

    public Commands getCommandManager() {
        return commands;
    }

    public LocalizationManager getLocalizationManager() { return localizationManager; }

    public DestinationStorage getDestinationStorage() { return destinationStorage; }

    public Speedometer getSpeedometer() { return speedometer; }

    public FileConfiguration getEnterMessages() {
        return enterMessages;
    }
}
