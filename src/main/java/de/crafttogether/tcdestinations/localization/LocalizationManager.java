package de.crafttogether.tcdestinations.localization;

/*
  Copyright (C) 2013-2022 bergerkiller
 */

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.DestinationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalizationManager {
    private static TCDestinations plugin = TCDestinations.plugin;

    private final FileConfiguration localizationconfig;

    public LocalizationManager() {
        // Load localization configuration
        this.localizationconfig = new FileConfiguration(TCDestinations.plugin, "localization.yml");

        // load
        if (this.localizationconfig.exists()) {
            this.loadLocalization();
        }

        // header
        this.localizationconfig.setHeader("Below are the localization nodes set for plugin '" + plugin.getName() + "'.");
        this.localizationconfig.addHeader("For colors and text-formatting use the MiniMessage format.");
        this.localizationconfig.addHeader("https://docs.adventure.kyori.net/minimessage/format.html");
        this.localizationconfig.addHeader("");
        this.localizationconfig.addHeader("There are also placeholders which can be used in every message.");
        this.localizationconfig.addHeader("StationType-Labels: {stationType} (replace 'stationType' with the name of your configured destination-types)");
        this.localizationconfig.addHeader("Command-Names: {cmd_destination} {cmd_destinations} {cmd_destedit} {cmd_mobenter} {cmd_mobeject}");
        this.localizationconfig.addHeader("Content: <header/> <prefix/> <footer/>");

        // load
        this.loadLocales(Localization.class);

        if (!this.localizationconfig.isEmpty()) {
            this.saveLocalization();
        }
    }

    public static List<PlaceholderResolver> getGlobalPlaceholders() {
        List<PlaceholderResolver> resolvers = new ArrayList<>();
        resolvers.add(PlaceholderResolver.resolver("cmd_destination", "/" + plugin.getCommandManager().getConfig().get("commands.destination")));
        resolvers.add(PlaceholderResolver.resolver("cmd_destinations", "/" + plugin.getCommandManager().getConfig().get("commands.destinations")));
        resolvers.add(PlaceholderResolver.resolver("cmd_destedit", "/" + plugin.getCommandManager().getConfig().get("commands.destedit")));
        resolvers.add(PlaceholderResolver.resolver("cmd_mobenter", "/" + plugin.getCommandManager().getConfig().get("commands.mobenter")));
        resolvers.add(PlaceholderResolver.resolver("cmd_mobeject", "/" + plugin.getCommandManager().getConfig().get("commands.mobeject")));

        // Add DestinationTypes
        String displayName = plugin.getConfig().getString("DestinationTypeAll.DisplayName");
        resolvers.add(PlaceholderResolver.resolver("all", displayName));

        for (DestinationType destinationType : DestinationType.getTypes())
            resolvers.add(PlaceholderResolver.resolver(destinationType.getName(), destinationType.getDisplayName()));

        return resolvers;
    }

    /**
     * Loads all the localization defaults from a Localization container
     * class<br>
     * If the class is not an enumeration, the static constants in the class are
     * used instead
     *
     * @param localizationDefaults class
     */
    public void loadLocales(Class<? extends ILocalizationDefault> localizationDefaults) {
        for (ILocalizationDefault def : CommonUtil.getClassConstants(localizationDefaults)) {
            this.loadLocale(def);
        }
    }

    /**
     * Loads a localization using a localization default
     *
     * @param localizationDefault to load from
     */
    public void loadLocale(ILocalizationDefault localizationDefault) {
        localizationDefault.initDefaults(this.localizationconfig);
    }

    /**
     * Loads a single Localization value<br>
     * Adds this node to the localization configuration if it wasn't added
     *
     * @param path to the value (case-insensitive, can not be null)
     * @param defaultValue for the value
     */
    public void loadLocale(String path, String defaultValue) {
        path = path.toLowerCase(Locale.ENGLISH);
        if (!this.localizationconfig.contains(path)) {
            this.localizationconfig.set(path, defaultValue);
        }
    }

    /**
     * Gets a localization value
     *
     * @param path to the localization value (case-insensitive, can not be null)
     * @return Localization value
     */
    public String getLocale(String path) {
        path = path.toLowerCase(Locale.ENGLISH);
        // First check if the path leads to a node
        if (this.localizationconfig.isNode(path)) {
            // Redirect to the proper sub-node
            // Check recursively if the arguments are contained
            // Update path to lead to the new path
            path = path + ".default";
        }

        return this.localizationconfig.get(path, String.class, "");
    }

    public final void loadLocalization() {
        this.localizationconfig.load();
    }
    public final void saveLocalization() {
        this.localizationconfig.save();
    }
}
