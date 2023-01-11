package de.crafttogether.tcdestinations.listener;

import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private static TCDestinations plugin = TCDestinations.plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (event.getPlayer().hasPermission("tcdestinations.notify.updates")) {

            Bukkit.getScheduler().runTaskAsynchronously(TCDestinations.plugin, () -> {
                Configuration config = plugin.getConfig();
                if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
                        || !config.getBoolean("Settings.Updates.Notify.InGame"))
                    return;

                Component message = Util.checkUpdates();

                if (message != null)
                    plugin.adventure().sender(event.getPlayer()).sendMessage(message);
            });
        }
    }
}
