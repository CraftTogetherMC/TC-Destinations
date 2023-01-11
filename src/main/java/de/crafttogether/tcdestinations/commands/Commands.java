package de.crafttogether.tcdestinations.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.*;
import cloud.commandframework.annotations.specifier.Range;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.google.common.collect.ImmutableMap;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.Destination;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.localization.PlaceholderResolver;
import de.crafttogether.tcdestinations.util.Updater;
import de.crafttogether.tcdestinations.util.Util;
import de.crafttogether.tcdestinations.util.TCHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {
    private static final TCDestinations plugin = TCDestinations.plugin;
    private static final CloudSimpleHandler cloud = new CloudSimpleHandler();

    private FileConfiguration config;

    public void enable(TCDestinations plugin) {
        cloud.enable(plugin);

        // Command handlers
        DestinationCommands commands_destination = new DestinationCommands();

        config = new FileConfiguration(plugin.getDataFolder() + File.separator + "commands.yml");
        config.load();

        cloud.getParser().stringProcessor(
            new PropertyReplacingStringProcessor(
                s -> ImmutableMap.of(
                    "command.destination", (String) config.get("commands.destination"),
                    "command.destinations", (String) config.get("commands.destinations"),
                    "command.destedit", (String) config.get("commands.destedit"),
                    "command.mobenter", (String) config.get("commands.mobenter"),
                    "command.mobeject", (String) config.get("commands.mobeject")
                ).get(s)
            )
        );

        // Suggestions
        cloud.suggest("onlinePlayers", (context, input) -> Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));

        cloud.suggest("serverName", (context, input) -> plugin.getDestinationStorage().getDestinations().stream().distinct()
                .map(Destination::getServer)
                .collect(Collectors.toList()));

        cloud.suggest("destinationName", (context, input) -> plugin.getDestinationStorage().getDestinations().stream()
                .map(Destination::getName)
                .collect(Collectors.toList()));

        cloud.suggest("destinationType", (context, input) -> {
            List<String> result = DestinationType.getTypes().stream()
                    .map(DestinationType::getDisplayName)
                    .collect(Collectors.toList());
            result.add(plugin.getConfig().getString("DestinationTypeAll.DisplayName"));
            return result;
        });

        // Register Annotations
        cloud.annotations(this);
        cloud.annotations(commands_destination);
    }

    @CommandMethod("tcdestinations")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void tcdestinations(
            final CommandSender sender
    ) {
        Updater.checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, Updater.BuildType buildType) -> {
            List<PlaceholderResolver> resolvers = new ArrayList<>();
            Component message = null;

            switch (buildType) {
                case RELEASE, SNAPSHOT -> {
                    resolvers.add(PlaceholderResolver.resolver("version", version));
                    resolvers.add(PlaceholderResolver.resolver("build", build));
                    resolvers.add(PlaceholderResolver.resolver("fileName", fileName));
                    resolvers.add(PlaceholderResolver.resolver("fileSize", Updater.humanReadableFileSize(fileSize)));
                    resolvers.add(PlaceholderResolver.resolver("url", url));
                    resolvers.add(PlaceholderResolver.resolver("currentVersion", currentVersion));
                    resolvers.add(PlaceholderResolver.resolver("currentBuild", currentBuild));

                    switch (buildType) {
                        case RELEASE -> Localization.UPDATE_RELEASE.deserialize(resolvers);
                        case SNAPSHOT -> Localization.UPDATE_DEVBUILD.deserialize(resolvers);
                    }
                }

                case UP2DATE -> {
                    resolvers.add(PlaceholderResolver.resolver("currentVersion", currentVersion));
                    resolvers.add(PlaceholderResolver.resolver("currentBuild", currentBuild));

                    FileConfiguration pluginDescription = Util.getPluginFile();
                    String buildNumber = (String) pluginDescription.get("build");
                    sender.sendMessage(ChatColor.GREEN + "TCDestinations version: " + TCDestinations.plugin.getDescription().getVersion() + " #" + buildNumber);

                    message = plugin.getMiniMessageParser().deserialize("<prefix/><gold>TCPortals version: </gold><yellow>" + currentVersion + " #" + currentBuild + "</yellow><newLine/>")
                            .append(Localization.UPDATE_LASTBUILD.deserialize(resolvers));
                }
            }

            if (message != null)
                plugin.adventure().sender(sender).sendMessage(message);

        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
    }

    @CommandMethod(value="${command.mobenter} [radius]", requiredSender=Player.class)
    @CommandDescription("Lässt Tiere in der nahen Umgebung in den ausgewählten Zug einsteigen.")
    @CommandPermission("craftbahn.command.mobenter")
    public void mobenter(
            final Player sender,
            @Argument(value="radius") @Range(min = "1", max = "16") Integer radius
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        if (radius == null)
            radius = 8;

        Location center = sender.getLocation();
        int entered = 0;
        for (Entity entity : WorldUtil.getNearbyEntities(center, radius, radius, radius)) {
            if (entity.getVehicle() != null)
                continue;

            if (EntityUtil.isMob(entity)) {
                for (MinecartMember<?> member : group) {
                    if (member.getAvailableSeatCount(entity) > 0 && member.addPassengerForced(entity)) {
                        entered++;
                        break;
                    }
                }
            }
        }

        if (entered > 0)
            Localization.COMMAND_MOBENTER_SUCCESS.message(sender,
                    PlaceholderResolver.resolver("amount", String.valueOf(entered)));
        else
            Localization.COMMAND_MOBENTER_FAILED.message(sender,
                    PlaceholderResolver.resolver("radius", String.valueOf(radius)));
    }

    @CommandMethod(value="${command.mobeject}", requiredSender=Player.class)
    @CommandDescription("Lässt alle Tiere aus dem ausgewählten Zug aussteigen.")
    @CommandPermission("craftbahn.command.mobeject")
    public void mobeject(
            final Player sender
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        for (MinecartMember<?> member : group) {
            if (!member.getEntity().hasPassenger()) continue;

            for (Entity passenger : member.getEntity().getPassengers()) {
                if (EntityUtil.isMob(passenger))
                    member.eject();
            }
        }

        Localization.COMMAND_MOBEJECT_SUCCESS.message(sender);
    }

    public static CloudSimpleHandler getCloud() {
        return cloud;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public CommandManager<?> getManager() {
        return cloud.getManager();
    }
}