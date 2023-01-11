package de.crafttogether.tcdestinations.listener;

import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.localization.PlaceholderResolver;
import de.crafttogether.tcdestinations.util.Updater;
import de.crafttogether.tcdestinations.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private static TCDestinations plugin = TCDestinations.plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("tcdestinations.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
            || !config.getBoolean("Settings.Updates.Notify.InGame"))
            return;

        Updater.checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, Updater.BuildType buildType) -> {
            List<PlaceholderResolver> resolvers = new ArrayList<>();
            Component message;

            resolvers.add(PlaceholderResolver.resolver("version", version));
            resolvers.add(PlaceholderResolver.resolver("build", build));
            resolvers.add(PlaceholderResolver.resolver("fileName", fileName));
            resolvers.add(PlaceholderResolver.resolver("fileSize", Updater.humanReadableFileSize(fileSize)));
            resolvers.add(PlaceholderResolver.resolver("url", url));
            resolvers.add(PlaceholderResolver.resolver("currentVersion", currentVersion));
            resolvers.add(PlaceholderResolver.resolver("currentBuild", currentBuild));

            if (buildType.equals(Updater.BuildType.RELEASE))
                message = Localization.UPDATE_RELEASE.deserialize(resolvers);
            else
                message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);

            plugin.adventure().player(event.getPlayer()).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"), 40L);
    }
}
