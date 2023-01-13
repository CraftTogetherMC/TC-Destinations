package de.crafttogether.tcdestinations.util;

import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.dep.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.crafttogether.common.dep.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import de.crafttogether.common.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Util {
    private static final TCDestinations plugin = TCDestinations.plugin;

    public static void debug(String message, boolean broadcast) {
        Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l[Debug]: &r" + message);

        // Broadcast to online players with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfig().getBoolean("Settings.Debug")) continue;
            PluginUtil.adventure().player(player).sendMessage(messageComponent);
        }

        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(messageComponent));
    }

    public static void debug(Component message, boolean broadcast) {
        debug(LegacyComponentSerializer.legacyAmpersand().serialize(message), broadcast);
    }

    public static void debug(String message) {
        debug(Component.text(message), false);
    }

    public static void debug(Component message) {
        debug(message, false);
    }
}
