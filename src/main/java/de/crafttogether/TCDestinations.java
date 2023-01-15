package de.crafttogether;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import de.crafttogether.common.dep.org.bstats.bukkit.Metrics;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.commands.Commands;
import de.crafttogether.tcdestinations.destinations.DestinationStorage;
import de.crafttogether.tcdestinations.listener.PlayerJoinListener;
import de.crafttogether.tcdestinations.listener.TrainEnterListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;

public final class TCDestinations extends JavaPlugin {
    public static TCDestinations plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private FileConfiguration enterMessages;

    @Override
    public void onEnable() {
        plugin = this;

        PluginManager pluginManager = Bukkit.getPluginManager();

        /* Check dependencies */
        if (!pluginManager.isPluginEnabled("CTCommons")) {
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
        PluginUtil.exportResource(this,"commands.yml");
        PluginUtil.exportResource(this,"enterMessages.yml");

        if (getDynmap() != null) {
            PluginUtil.exportResource(this,"rail.png");
            PluginUtil.exportResource(this,"minecart.png");
        }

        // Create default config
        saveDefaultConfig();

        // Set serverName
        serverName = getConfig().getString("Settings.ServerName");

        enterMessages = new FileConfiguration(plugin.getDataFolder() + File.separator + "enterMessages.yml");
        enterMessages.load();

        // Register Events
        pluginManager.registerEvents(new PlayerJoinListener(),this);
        pluginManager.registerEvents(new TrainEnterListener(),this);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class,
                getConfig().getString("Settings.Language"), "en_EN", "locales");

        localizationManager.addHeader("");
        localizationManager.addHeader("There are also 'global' placeholders which can be used in every message.");
        localizationManager.addHeader("StationType-Labels: {stationType} (replace 'stationType' with the name of your configured destination-types)");
        localizationManager.addHeader("Command-Names: {cmd_destination} {cmd_destinations} {cmd_destedit} {cmd_mobenter} {cmd_mobeject}");
        localizationManager.addHeader("Content: <header/> <prefix/> <footer/>");
        localizationManager.writeHeaders();

        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());
        localizationManager.addTagResolver("header", Localization.HEADER.deserialize());
        localizationManager.addTagResolver("footer", Localization.FOOTER.deserialize());

        // Initialize Storage
        destinationStorage = new DestinationStorage();
        if (!destinationStorage.isActive()) {
            pluginManager.disablePlugin(plugin);
            return;
        }

        // Register Commands
        commands = new Commands();
        commands.enable(this);

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
            && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null)
                    err.printStackTrace();

                if (build == null)
                    return;

                // Go sync again to avoid mixing output with other plugins
                Bukkit.getScheduler().runTask(plugin, () -> {
                    switch (build.getType()) {
                        case RELEASE -> plugin.getLogger().warning("A new full version of this plugin was released!");
                        case SNAPSHOT -> plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                    }

                    plugin.getLogger().warning("You can download it here: " + build.getUrl());
                    plugin.getLogger().warning("Version: " + build.getVersion() + " #" + build.getNumber());
                    plugin.getLogger().warning("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                    plugin.getLogger().warning("You are on version: " + currentVersion + " #" + currentBuild);
                });
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        // bStats
        new Metrics(this, 17416);

        getLogger().info(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(destinationStorage != null)
            destinationStorage.close();
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
