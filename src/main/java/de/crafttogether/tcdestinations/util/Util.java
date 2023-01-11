package de.crafttogether.tcdestinations.util;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.google.common.io.ByteStreams;
import com.google.gson.*;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.localization.PlaceholderResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.io.*;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Objects;

public class Util {
    private static final TCDestinations plugin = TCDestinations.plugin;

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

    public static Component checkUpdates() {
        Gson gson = new Gson();
        String json;

        try {
            json = readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + plugin.getDescription().getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to retrieve update-informations from ci.craft-together-mc.de");
            e.printStackTrace();
            return null;
        }

        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            JsonObject lastDevBuild = response.getAsJsonObject("lastDevBuild");
            JsonObject lastRelease = response.getAsJsonObject("lastRelease");

            int installedBuildNumber = 0;
            FileConfiguration pluginDescription = getPluginFile();
            String buildNumber = (String) pluginDescription.get("build");

            if (!buildNumber.equals("NO-CI"))
                installedBuildNumber = Integer.parseInt(buildNumber);

            String lastDevBuildVersion = stringOrNull(lastDevBuild, "version");
            int lastDevBuildNumber = intOrZero(lastDevBuild, "build");
            String lastDevBuildFileName = stringOrNull(lastDevBuild, "fileName");
            int lastDevBuildFileSize = intOrZero(lastDevBuild, "fileSize");
            String lastDevBuildUrl = stringOrNull(lastDevBuild, "url");

            String lastReleaseVersion = stringOrNull(lastRelease, "version");
            String lastReleaseFileName = null, lastReleaseUrl = null;
            int lastReleaseNumber = 0, lastReleaseFileSize = 0;

            if (lastReleaseVersion != null) {
                lastReleaseNumber = intOrZero(lastRelease, "build");
                lastReleaseFileName = stringOrNull(lastRelease, "fileName");
                lastReleaseFileSize = intOrZero(lastRelease, "fileSize");
                lastReleaseUrl = stringOrNull(lastRelease, "url");
            }

            if (lastReleaseVersion != null && lastReleaseNumber > installedBuildNumber) {
                plugin.getLogger().warning("A new full version of this plugin was released!");
                plugin.getLogger().warning("You can download it here: " + lastReleaseUrl);
                plugin.getLogger().warning("Version: " + lastReleaseVersion + " #" + lastReleaseNumber);
                plugin.getLogger().warning("FileName: " + lastReleaseFileName + " FileSize: " + humanReadableFileSize(lastReleaseFileSize));
                plugin.getLogger().warning("You are on version: " + plugin.getDescription().getVersion() + " #" + installedBuildNumber);

                return Localization.UPDATE_RELEASE.deserialize(
                        PlaceholderResolver.resolver("build", lastReleaseNumber),
                        PlaceholderResolver.resolver("version", lastReleaseVersion),
                        PlaceholderResolver.resolver("fileName", lastReleaseFileName),
                        PlaceholderResolver.resolver("fileSize", humanReadableFileSize(lastReleaseFileSize)),
                        PlaceholderResolver.resolver("url", lastReleaseUrl),
                        PlaceholderResolver.resolver("currentBuild", installedBuildNumber),
                        PlaceholderResolver.resolver("currentVersion", plugin.getDescription().getVersion()));
            }

            else if (lastDevBuildNumber > installedBuildNumber) {
                plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                plugin.getLogger().warning("You can download it here: " + lastDevBuildUrl);
                plugin.getLogger().warning("Version: " + lastDevBuildVersion + " #" + lastDevBuildNumber);
                plugin.getLogger().warning("FileName: " + lastDevBuildFileName + " FileSize: " + humanReadableFileSize(lastDevBuildFileSize));
                plugin.getLogger().warning("You are on version: " + plugin.getDescription().getVersion() + " #" + installedBuildNumber);

                return Localization.UPDATE_DEVBUILD.deserialize(
                        PlaceholderResolver.resolver("build", lastDevBuildNumber),
                        PlaceholderResolver.resolver("version", lastDevBuildVersion),
                        PlaceholderResolver.resolver("fileName", lastDevBuildFileName),
                        PlaceholderResolver.resolver("fileSize", humanReadableFileSize(lastDevBuildFileSize)),
                        PlaceholderResolver.resolver("url", lastDevBuildUrl),
                        PlaceholderResolver.resolver("currentBuild", installedBuildNumber),
                        PlaceholderResolver.resolver("currentVersion", plugin.getDescription().getVersion()));
            }

            else
                return Localization.UPDATE_LASTBUILD.deserialize();
        }
        catch (Exception e) {
            plugin.getLogger().warning("An error occured while parsing update-informations from ci.craft-together-mc.de");
            e.printStackTrace();
        }

        return null;
    }

    private static int intOrZero(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement instanceof JsonNull ? 0 : jsonElement.getAsInt();
    }

    private static String stringOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement instanceof JsonNull ? null : jsonElement.getAsString();
    }

    public static String humanReadableFileSize(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public static FileConfiguration getPluginFile() {
        InputStream inputStream = plugin.getResource("plugin.yml");
        FileConfiguration config = new FileConfiguration(plugin);
        config.loadFromStream(inputStream);
        return config;
    }
}
