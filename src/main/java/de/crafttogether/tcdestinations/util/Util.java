package de.crafttogether.tcdestinations.util;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.google.common.io.ByteStreams;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.util.AudienceUtil;

import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.shaded.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.crafttogether.common.shaded.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public class Util {
    private static final TCDestinations plugin = TCDestinations.plugin;

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(name)).toList().get(0);
    }

    public static void exportResource(Plugin plugin, String fileName) {
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
            if (!player.hasPermission("tcdestinations.debug"))
                continue;

            if (plugin.getConfig().getBoolean("Settings.Debug"))
                continue;

            AudienceUtil.Bukkit.audiences.player(player).sendMessage(messageComponent);
        }

        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(messageComponent));
    }

    public static void debug(String trainName, Component message) {
        if (!plugin.getConfig().getBoolean("Settings.Debug"))
            return;

        MinecartGroup group = TCHelper.getTrain(trainName);
        if (group == null)
            return;

        Component prefix = LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l[Debug]: &r");
        message = prefix.append(message);

        for (Player player : TCHelper.getPlayerPassengers(group)) {
            if (!player.hasPermission("tcdestinations.debug"))
                continue;

            AudienceUtil.Bukkit.audiences.player(player).sendMessage(message);
        }

        TCDestinations.plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }
    public static void debug(String trainName, String message) {
        debug(trainName, LegacyComponentSerializer.legacyAmpersand().deserialize(message));
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
