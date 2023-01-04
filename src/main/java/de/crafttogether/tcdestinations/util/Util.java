package de.crafttogether.tcdestinations.util;

import com.google.common.io.ByteStreams;
import de.crafttogether.TCDestinations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class Util {
    private static TCDestinations plugin = TCDestinations.plugin;

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(name)).toList().get(0);
    }

    public static void exportResource(String fileName) {
        TCDestinations plugin = TCDestinations.plugin;

        File outputFile = new File(plugin.getDataFolder() + File.separator + fileName);
        if (outputFile.exists())
            return;

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream == null) {
            plugin.getLogger().warning("Could not read resource '" + fileName + "'");
            return;
        }

        try {
            outputFile.createNewFile();
            OutputStream os = new FileOutputStream(outputFile);
            ByteStreams.copy(inputStream, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void debug(String message, boolean broadcast) {
        Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l[Debug]: &r" + message);

        // Broadcast to online players with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (TCDestinations.plugin.getConfig().getBoolean("Settings.Debug")) continue;
            plugin.adventure().player(player).sendMessage(messageComponent);
        }

        TCDestinations.plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(messageComponent));
    }

    public static void debug(String message) {
        debug(Component.text(message), false);
    }
    public static void debug(Component message, boolean broadcast) {
        debug(LegacyComponentSerializer.legacyAmpersand().serialize(message), broadcast);
    }
    public static void debug(Component message) {
        debug(message, false);
    }
}
