package de.crafttogether.tcdestinations.listener;

import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.Localization;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private static final TCDestinations plugin = TCDestinations.plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("tcdestinations.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if ((config.getBoolean("Settings.Updates.Notify.DisableNotifications")
            || !config.getBoolean("Settings.Updates.Notify.InGame")))
            return;

        new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
            if (err != null) {
                plugin.getLogger().warning("An error occurred while receiving update information.");
                plugin.getLogger().warning("Error: " + err.getMessage());
            }

            if (build == null)
                return;

            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            resolvers.add(Placeholder.set("version", build.getVersion()));
            resolvers.add(Placeholder.set("build", build.getNumber()));
            resolvers.add(Placeholder.set("fileName", build.getFileName()));
            resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
            resolvers.add(Placeholder.set("url", build.getUrl()));
            resolvers.add(Placeholder.set("currentVersion", currentVersion));
            resolvers.add(Placeholder.set("currentBuild", currentBuild));

            if (build.getType().equals(BuildType.RELEASE))
                message = Localization.UPDATE_RELEASE.deserialize(resolvers);
            else
                message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);

            PluginUtil.adventure().player(event.getPlayer()).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"), 40L);
    }
}
